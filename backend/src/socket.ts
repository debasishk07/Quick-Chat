import { Server, Socket } from 'socket.io';
import { v2 as cloudinary } from 'cloudinary';
import { dbOperations } from './database';
import { MessagePayload, User } from './types';

// Map to track active socket connections: phone -> socketId
const activeSockets = new Map<string, string>();

export function setupSocketIO(io: Server) {
  io.use((socket, next) => {
    const phone = socket.handshake.query.phone as string;
    if (!phone) {
      return next(new Error('Authentication error: Phone is required'));
    }
    socket.data.phone = phone;
    next();
  });

  io.on('connection', async (socket: Socket) => {
    const phone = socket.data.phone as string;
    activeSockets.set(phone, socket.id);
    console.log(`User connected: ${phone} (${socket.id})`);

    // 1. Mark user as online in DB & broadcast presence
    try {
      await dbOperations.run('UPDATE users SET isOnline = 1, lastSeen = ? WHERE phone = ?', [Date.now(), phone]);
      
      // Broadcast presence changes: notify existing clients & inform newly connected client of online users
      for (const [recipientPhone, socketId] of activeSockets.entries()) {
        if (recipientPhone === phone) continue;
        const isBlocked = await dbOperations.get(
          'SELECT 1 FROM blocked_contacts WHERE (blocker = ? AND blocked = ?) OR (blocker = ? AND blocked = ?)',
          [phone, recipientPhone, recipientPhone, phone]
        );
        if (!isBlocked) {
          io.to(socketId).emit('presence-change', { phone, isOnline: true, lastSeen: Date.now() });
          socket.emit('presence-change', { phone: recipientPhone, isOnline: true, lastSeen: Date.now() });
        }
      }

      // 2. Fetch and deliver offline messages
      const rawOffline = await dbOperations.all<MessagePayload & { deletedFor?: string }>(
        'SELECT * FROM messages WHERE recipient = ? ORDER BY timestamp ASC',
        [phone]
      );

      const offlineMessages = rawOffline.filter(msg => {
        if (!msg.deletedFor) return true;
        try {
          const list: string[] = JSON.parse(msg.deletedFor);
          return !list.includes(phone);
        } catch (e) {
          return true;
        }
      });

      if (offlineMessages.length > 0) {
        console.log(`Delivering ${offlineMessages.length} offline messages to ${phone}`);
        socket.emit('offline-messages', offlineMessages);

        // Delete them from server after delivering to maintain E2EE privacy
        await dbOperations.run('DELETE FROM messages WHERE recipient = ?', [phone]);

        // Broadcast delivery receipts to the original senders
        for (const msg of offlineMessages) {
          const senderSocketId = activeSockets.get(msg.sender);
          if (senderSocketId) {
            io.to(senderSocketId).emit('message-receipt', {
              messageId: msg.id,
              recipient: phone,
              status: 'DELIVERED',
              timestamp: Date.now()
            });
          }
        }
      }

      // 3. Deliver pending events (e.g. offline deletions)
      const pendingEvents = await dbOperations.all<{ id: number; event: string; payloadText: string }>(
        'SELECT * FROM pending_events WHERE recipient = ? ORDER BY timestamp ASC',
        [phone]
      );
      if (pendingEvents.length > 0) {
        console.log(`Delivering ${pendingEvents.length} pending events to ${phone}`);
        for (const pe of pendingEvents) {
          try {
            socket.emit(pe.event, JSON.parse(pe.payloadText));
          } catch (e) {}
        }
        await dbOperations.run('DELETE FROM pending_events WHERE recipient = ?', [phone]);
      }
    } catch (err) {
      console.error('Error handling user connection state:', err);
    }

    // 3. Handle sending real-time messages
    socket.on('send-message', async (message: MessagePayload, ackCallback?: (res: { success: boolean }) => void) => {
      console.log(`Relaying message ${message.id} from ${message.sender} to ${message.recipient}`);
      
      try {
        // Enforce block check at the backend
        const blockExists = await dbOperations.get(
          'SELECT 1 FROM blocked_contacts WHERE blocker = ? AND blocked = ?',
          [message.recipient, message.sender]
        );
        if (blockExists) {
          console.log(`Message from ${message.sender} to blocker ${message.recipient} dropped.`);
          if (ackCallback) ackCallback({ success: false });
          return;
        }

        const recipientSocketId = activeSockets.get(message.recipient);

        if (recipientSocketId) {
          // Recipient is online, send message immediately
          io.to(recipientSocketId).emit('receive-message', { ...message, status: 'DELIVERED' });
          if (ackCallback) ackCallback({ success: true });

          // Notify sender that it is delivered
          socket.emit('message-receipt', {
            messageId: message.id,
            recipient: message.recipient,
            status: 'DELIVERED',
            timestamp: Date.now()
          });
        } else {
          // Recipient is offline, queue message in server DB
          await dbOperations.run(
            `INSERT INTO messages (id, sender, recipient, isGroup, ciphertext, iv, ephemeralPublicKey, messageType, timestamp, status)
             VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)`,
            [
              message.id,
              message.sender,
              message.recipient,
              message.isGroup,
              message.ciphertext,
              message.iv,
              message.ephemeralPublicKey || null,
              message.messageType,
              message.timestamp,
              'SENT'
            ]
          );
          if (ackCallback) ackCallback({ success: true });

          // Notify sender that it is sent (but not yet delivered)
          socket.emit('message-receipt', {
            messageId: message.id,
            recipient: message.recipient,
            status: 'SENT',
            timestamp: Date.now()
          });
        }
      } catch (err) {
        console.error('Error sending message:', err);
        if (ackCallback) ackCallback({ success: false });
      }
    });

    // 4. Handle delivery & read receipts from client
    socket.on('message-receipt', async (receipt: { messageId?: string; messageIds?: string[]; recipient: string; status: 'DELIVERED' | 'READ'; timestamp: number }) => {
      try {
        const ids = receipt.messageIds || (receipt.messageId ? [receipt.messageId] : []);
        for (const id of ids) {
          await dbOperations.run('UPDATE messages SET status = ? WHERE id = ?', [receipt.status, id]);
        }

        const senderSocketId = activeSockets.get(receipt.recipient);
        if (senderSocketId) {
          io.to(senderSocketId).emit('message-receipt', receipt);
        } else {
          // Queue receipt for offline sender
          await dbOperations.run(
            'INSERT INTO pending_events (recipient, event, payloadText, timestamp) VALUES (?, ?, ?, ?)',
            [receipt.recipient, 'message-receipt', JSON.stringify(receipt), Date.now()]
          );
        }
      } catch (err) {
        console.error('Error handling message-receipt event:', err);
      }
    });

    // 5. Handle message deletion
    socket.on('delete-message', async (data: { messageId: string; recipient: string; mode: 'me' | 'everyone'; publicId?: string; resourceType?: string }, ackCallback?: (res: { success: boolean; error?: string }) => void) => {
      console.log(`Delete message request from ${phone} for msg ${data.messageId} (mode: ${data.mode})`);
      try {
        if (data.mode === 'everyone') {
          // Check if message exists in server offline queue and verify sender
          const queued = await dbOperations.get<{ sender: string }>('SELECT sender FROM messages WHERE id = ?', [data.messageId]);
          if (queued && queued.sender !== phone) {
            if (ackCallback) ackCallback({ success: false, error: 'Unauthorized: Only the sender can delete for everyone' });
            return;
          }

          // Mark offline message as deleted if stored
          await dbOperations.run('UPDATE messages SET ciphertext = ?, isDeleted = 1 WHERE id = ?', ['', data.messageId]);

          // Trigger Cloudinary destroy API if publicId is present
          if (data.publicId) {
            try {
              await cloudinary.uploader.destroy(data.publicId, { resource_type: data.resourceType || 'auto' });
              console.log(`Cloudinary destroyed asset: ${data.publicId}`);
            } catch (cErr) {
              console.error('Failed to destroy Cloudinary asset:', cErr);
            }
          }

          // Broadcast real-time update to recipient if online, or queue for offline
          const recipientSocketId = activeSockets.get(data.recipient);
          const delPayload = {
            messageId: data.messageId,
            deletedBy: phone,
            mode: 'everyone',
            publicId: data.publicId
          };
          if (recipientSocketId) {
            io.to(recipientSocketId).emit('message-deleted', delPayload);
          } else {
            await dbOperations.run(
              'INSERT INTO pending_events (recipient, event, payloadText, timestamp) VALUES (?, ?, ?, ?)',
              [data.recipient, 'message-deleted', JSON.stringify(delPayload), Date.now()]
            );
          }

          if (ackCallback) ackCallback({ success: true });
        } else if (data.mode === 'me') {
          // Update deletedFor in offline queue if message exists
          const queued = await dbOperations.get<{ deletedFor: string }>('SELECT deletedFor FROM messages WHERE id = ?', [data.messageId]);
          if (queued) {
            let list: string[] = [];
            try { list = JSON.parse(queued.deletedFor || '[]'); } catch (e) {}
            if (!list.includes(phone)) {
              list.push(phone);
              await dbOperations.run('UPDATE messages SET deletedFor = ? WHERE id = ?', [JSON.stringify(list), data.messageId]);
            }
          }
          if (ackCallback) ackCallback({ success: true });
        }
      } catch (err: any) {
        console.error('Error handling delete-message socket event:', err);
        if (ackCallback) ackCallback({ success: false, error: err.message });
      }
    });

    // 6. Handle typing indicators
    socket.on('typing', (data: { recipient: string; isTyping: boolean }) => {
      const recipientSocketId = activeSockets.get(data.recipient);
      if (recipientSocketId) {
        io.to(recipientSocketId).emit('typing', {
          sender: phone,
          isTyping: data.isTyping
        });
      }
    });

    // 6. Handle client disconnect
    socket.on('disconnect', async () => {
      console.log(`User disconnected: ${phone}`);
      activeSockets.delete(phone);
      
      try {
        const lastSeenTime = Date.now();
        await dbOperations.run('UPDATE users SET isOnline = 0, lastSeen = ? WHERE phone = ?', [lastSeenTime, phone]);
        
        for (const [recipientPhone, socketId] of activeSockets.entries()) {
          if (recipientPhone === phone) continue;
          const isBlocked = await dbOperations.get(
            'SELECT 1 FROM blocked_contacts WHERE (blocker = ? AND blocked = ?) OR (blocker = ? AND blocked = ?)',
            [phone, recipientPhone, recipientPhone, phone]
          );
          if (!isBlocked) {
            io.to(socketId).emit('presence-change', { phone, isOnline: false, lastSeen: lastSeenTime });
          }
        }
      } catch (err) {
        console.error('Error handling disconnect state:', err);
      }
    });
  });
}
