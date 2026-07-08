import { Server, Socket } from 'socket.io';
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
      socket.broadcast.emit('presence-change', { phone, isOnline: true, lastSeen: Date.now() });

      // 2. Fetch and deliver offline messages
      const offlineMessages = await dbOperations.all<MessagePayload>(
        'SELECT * FROM messages WHERE recipient = ? ORDER BY timestamp ASC',
        [phone]
      );

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
    } catch (err) {
      console.error('Error handling user connection state:', err);
    }

    // 3. Handle sending real-time messages
    socket.on('send-message', async (message: MessagePayload, ackCallback?: (res: { success: boolean }) => void) => {
      console.log(`Relaying message ${message.id} from ${message.sender} to ${message.recipient}`);
      
      try {
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
    socket.on('message-receipt', async (receipt: { messageId: string; recipient: string; status: 'DELIVERED' | 'READ'; timestamp: number }) => {
      // Forward the receipt to the original sender
      const senderSocketId = activeSockets.get(receipt.recipient);
      if (senderSocketId) {
        io.to(senderSocketId).emit('message-receipt', receipt);
      }
    });

    // 5. Handle typing indicators
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
        socket.broadcast.emit('presence-change', { phone, isOnline: false, lastSeen: lastSeenTime });
      } catch (err) {
        console.error('Error handling disconnect state:', err);
      }
    });
  });
}
