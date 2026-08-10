import { Router, Request, Response } from 'express';
import multer from 'multer';
import path from 'path';
import fs from 'fs';
import jwt from 'jsonwebtoken';
import { v2 as cloudinary } from 'cloudinary';
import { dbOperations } from './database';
import { User, PreKeyBundle } from './types';
import { verifyFirebaseIdToken } from './firebase';

const router = Router();
const JWT_SECRET = process.env.JWT_SECRET || 'quickchat_jwt_secret_key_2026';

// Cloudinary configuration
cloudinary.config({
  cloud_name: process.env.CLOUDINARY_CLOUD_NAME || 'quickchat',
  api_key: process.env.CLOUDINARY_API_KEY || '1234567890',
  api_secret: process.env.CLOUDINARY_API_SECRET || 'secret'
});

// Multer setup for media upload
const storage = multer.diskStorage({
  destination: (req, file, cb) => {
    const uploadPath = process.env.UPLOADS_PATH || path.resolve(__dirname, '../uploads');
    if (!fs.existsSync(uploadPath)) {
      fs.mkdirSync(uploadPath, { recursive: true });
    }
    cb(null, uploadPath);
  },
  filename: (req, file, cb) => {
    const uniqueSuffix = Date.now() + '-' + Math.round(Math.random() * 1e9);
    cb(null, file.fieldname + '-' + uniqueSuffix + path.extname(file.originalname));
  }
});
const upload = multer({ storage });

// Rate limits map for username search: requesterId -> timestamps[]
const searchLimits = new Map<string, number[]>();

function checkSearchRateLimit(userId: string): boolean {
  const now = Date.now();
  const timestamps = searchLimits.get(userId) || [];
  const recent = timestamps.filter(t => now - t < 60000);
  if (recent.length >= 5) {
    return false;
  }
  recent.push(now);
  searchLimits.set(userId, recent);
  return true;
}

// 0. Health check endpoint
router.get('/health', async (req: Request, res: Response) => {
  try {
    const userCount = await dbOperations.get<{ count: number }>('SELECT COUNT(*) as count FROM users');
    const msgCount = await dbOperations.get<{ count: number }>('SELECT COUNT(*) as count FROM messages');
    return res.json({
      status: 'ok',
      service: 'Quick Chat Backend API',
      timestamp: new Date().toISOString(),
      database: 'connected',
      activeUsers: userCount?.count || 0,
      totalMessages: msgCount?.count || 0
    });
  } catch (err: any) {
    return res.status(500).json({ status: 'error', error: err.message });
  }
});

// 1. Mock OTP verification
router.post('/auth/verify-otp', async (req: Request, res: Response) => {
  const { phone, code } = req.body;
  if (!phone || !code) {
    return res.status(400).json({ error: 'Phone and code are required' });
  }

  // Accept any 6 digit code for local verification
  if (code.length === 6) {
    // Check if user exists (by phone identifier or phoneNumber)
    const user = await dbOperations.get<User>('SELECT * FROM users WHERE phone = ? OR phoneNumber = ?', [phone, phone]);
    return res.json({ success: true, isNewUser: !user, user });
  }

  return res.status(400).json({ error: 'Invalid verification code' });
});

// 2. Register/Update profile
router.post('/auth/register-profile', async (req: Request, res: Response) => {
  const { phone, displayName, avatarUrl, about } = req.body;
  if (!phone || !displayName) {
    return res.status(400).json({ error: 'Phone and displayName are required' });
  }

  try {
    const existing = await dbOperations.get<User>('SELECT * FROM users WHERE phone = ?', [phone]);
    if (existing) {
      await dbOperations.run(
        'UPDATE users SET displayName = ?, avatarUrl = ?, about = ?, lastSeen = ?, isOnline = ? WHERE phone = ?',
        [displayName, avatarUrl || existing.avatarUrl, about || existing.about, Date.now(), 1, phone]
      );
    } else {
      // New user registered via phone OTP
      await dbOperations.run(
        'INSERT INTO users (phone, phoneNumber, email, authProviders, displayName, avatarUrl, about, lastSeen, isOnline, usernameSearchEnabled) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)',
        [phone, phone, null, 'phone', displayName, avatarUrl || null, about || 'Hey there! I am using Quick Chat.', Date.now(), 1, 1]
      );
    }
    const user = await dbOperations.get<User>('SELECT * FROM users WHERE phone = ?', [phone]);
    res.json({ success: true, user });
  } catch (err: any) {
    res.status(500).json({ error: err.message });
  }
});

// 3. Contact Sync (Uses phoneNumber instead of stable internal ID phone)
router.post('/contacts/sync', async (req: Request, res: Response) => {
  const { phones } = req.body; // Array of phone numbers
  if (!Array.isArray(phones)) {
    return res.status(400).json({ error: 'Phones array is required' });
  }

  try {
    if (phones.length === 0) {
      return res.json({ contacts: [] });
    }
    const placeholders = phones.map(() => '?').join(',');
    const contacts = await dbOperations.all<User>(
      `SELECT * FROM users WHERE phoneNumber IN (${placeholders})`,
      phones
    );
    res.json({ contacts });
  } catch (err: any) {
    res.status(500).json({ error: err.message });
  }
});

// 4. Upload PreKeys
router.post('/prekeys', async (req: Request, res: Response) => {
  const { phone, identityKey, signedPreKey, signedPreKeySignature, oneTimePreKeys } = req.body;

  if (!phone || !identityKey || !signedPreKey || !signedPreKeySignature || !Array.isArray(oneTimePreKeys)) {
    return res.status(400).json({ error: 'Invalid prekey bundle data' });
  }

  try {
    const prekeysJson = JSON.stringify(oneTimePreKeys);
    const existing = await dbOperations.get('SELECT phone FROM prekeys WHERE phone = ?', [phone]);

    if (existing) {
      await dbOperations.run(
        'UPDATE prekeys SET identityKey = ?, signedPreKey = ?, signedPreKeySignature = ?, oneTimePreKeys = ? WHERE phone = ?',
        [identityKey, signedPreKey, signedPreKeySignature, prekeysJson, phone]
      );
    } else {
      await dbOperations.run(
        'INSERT INTO prekeys (phone, identityKey, signedPreKey, signedPreKeySignature, oneTimePreKeys) VALUES (?, ?, ?, ?, ?)',
        [phone, identityKey, signedPreKey, signedPreKeySignature, prekeysJson]
      );
    }
    res.json({ success: true });
  } catch (err: any) {
    res.status(500).json({ error: err.message });
  }
});

// 5. Get PreKey Bundle
router.get('/prekeys/:phone', async (req: Request, res: Response) => {
  const { phone } = req.params;

  try {
    const bundle = await dbOperations.get<PreKeyBundle>('SELECT * FROM prekeys WHERE phone = ?', [phone]);
    if (!bundle) {
      return res.status(404).json({ error: 'Prekey bundle not found for user' });
    }

    const oneTimeKeys: string[] = JSON.parse(bundle.oneTimePreKeys);
    let poppedKey: string | null = null;

    if (oneTimeKeys.length > 0) {
      poppedKey = oneTimeKeys.shift() || null;
      await dbOperations.run(
        'UPDATE prekeys SET oneTimePreKeys = ? WHERE phone = ?',
        [JSON.stringify(oneTimeKeys), phone]
      );
    }

    res.json({
      phone: bundle.phone,
      identityKey: bundle.identityKey,
      signedPreKey: bundle.signedPreKey,
      signedPreKeySignature: bundle.signedPreKeySignature,
      oneTimePreKey: poppedKey
    });
  } catch (err: any) {
    res.status(500).json({ error: err.message });
  }
});

// 6. Media upload (Local Fallback & Cloudinary Direct)
router.post('/media/upload', upload.single('file'), (req: Request, res: Response) => {
  if (!req.file) {
    return res.status(400).json({ error: 'No file uploaded' });
  }

  const fileUrl = `${req.protocol}://${req.get('host')}/uploads/${req.file.filename}`;
  res.json({ success: true, fileUrl });
});

router.post('/media/upload-cloudinary', upload.single('file'), async (req: Request, res: Response) => {
  if (!req.file) {
    return res.status(400).json({ error: 'No file uploaded' });
  }

  try {
    const result = await cloudinary.uploader.upload(req.file.path, {
      resource_type: 'auto',
      folder: 'quickchat_media'
    });

    try { fs.unlinkSync(req.file.path); } catch (e) {}

    return res.json({
      success: true,
      secure_url: result.secure_url,
      public_id: result.public_id,
      resource_type: result.resource_type,
      duration: result.duration || null,
      format: result.format || null,
      bytes: result.bytes || req.file.size
    });
  } catch (err: any) {
    console.error('Cloudinary upload error:', err);
    const fileUrl = `${req.protocol}://${req.get('host')}/uploads/${req.file.filename}`;
    const ext = path.extname(req.file.filename).replace('.', '');
    return res.json({
      success: true,
      secure_url: fileUrl,
      public_id: req.file.filename,
      resource_type: 'auto',
      duration: null,
      format: ext,
      bytes: req.file.size
    });
  }
});

// 6b. Firebase Auth Token Verification & Account Linking
router.post('/auth/verify', async (req: Request, res: Response) => {
  const { idToken, provider } = req.body;
  if (!idToken) {
    return res.status(400).json({ error: 'idToken is required' });
  }

  try {
    const decoded = await verifyFirebaseIdToken(idToken);
    const firebaseUid = decoded.uid;
    const email = decoded.email || req.body.email || null;
    const phoneNumber = decoded.phone_number || req.body.phone || null;
    const displayName = decoded.name || req.body.displayName || (email ? email.split('@')[0] : 'User');
    const avatarUrl = decoded.picture || req.body.avatarUrl || null;

    let user = null;
    if (firebaseUid) {
      user = await dbOperations.get<User>(
        'SELECT * FROM users WHERE phone = ? OR (email IS NOT NULL AND email = ?) OR (phoneNumber IS NOT NULL AND phoneNumber = ?)',
        [firebaseUid, email || 'NO_MATCH', phoneNumber || 'NO_MATCH']
      );
    }

    let isNewUser = false;
    if (!user) {
      isNewUser = true;
      const internalId = firebaseUid || `user_${Date.now()}`;
      const authProviderName = provider || (email ? 'google' : 'phone');

      await dbOperations.run(
        'INSERT INTO users (phone, phoneNumber, email, authProviders, displayName, avatarUrl, about, lastSeen, isOnline, usernameSearchEnabled) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)',
        [internalId, phoneNumber, email, authProviderName, displayName, avatarUrl, 'Hey there! I am using Quick Chat.', Date.now(), 1, 1]
      );
      user = await dbOperations.get<User>('SELECT * FROM users WHERE phone = ?', [internalId]);
    } else {
      // Account Linking
      const currentProviders = (user.authProviders || '').split(',').filter(Boolean);
      const newProvider = provider || (email ? 'google' : 'phone');
      let updated = false;

      if (!currentProviders.includes(newProvider)) {
        currentProviders.push(newProvider);
        updated = true;
      }
      if (email && !user.email) {
        user.email = email;
        updated = true;
      }
      if (phoneNumber && !user.phoneNumber) {
        user.phoneNumber = phoneNumber;
        updated = true;
      }

      if (updated) {
        await dbOperations.run(
          'UPDATE users SET authProviders = ?, email = ?, phoneNumber = ? WHERE phone = ?',
          [currentProviders.join(','), user.email, user.phoneNumber, user.phone]
        );
        user.authProviders = currentProviders.join(',');
      }
    }

    const token = jwt.sign(
      { userId: user?.phone, email: user?.email, phone: user?.phoneNumber },
      JWT_SECRET,
      { expiresIn: '30d' }
    );

    return res.json({
      success: true,
      isNewUser,
      token,
      user
    });
  } catch (err: any) {
    console.error('Firebase Auth verification error:', err);
    return res.status(401).json({ error: 'Authentication failed: ' + err.message });
  }
});

// 7. Google Login
router.post('/auth/google', async (req: Request, res: Response) => {
  const { googleUid, email, displayName, avatarUrl } = req.body;
  if (!googleUid || !email) {
    return res.status(400).json({ error: 'googleUid and email are required' });
  }

  try {
    // Check if user exists by internal ID (phone) or email
    let user = await dbOperations.get<User>('SELECT * FROM users WHERE phone = ? OR email = ?', [googleUid, email]);
    let isNewUser = false;

    if (!user) {
      isNewUser = true;
      await dbOperations.run(
        'INSERT INTO users (phone, phoneNumber, email, authProviders, displayName, avatarUrl, about, lastSeen, isOnline, usernameSearchEnabled) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)',
        [googleUid, null, email, 'google', displayName || email.split('@')[0], avatarUrl || null, 'Hey there! I am using Quick Chat.', Date.now(), 1, 1]
      );
      user = await dbOperations.get<User>('SELECT * FROM users WHERE phone = ?', [googleUid]);
    } else {
      // If found but doesn't have google provider in list, add it (linking)
      const providers = user.authProviders.split(',');
      if (!providers.includes('google')) {
        providers.push('google');
        await dbOperations.run(
          'UPDATE users SET authProviders = ?, email = ? WHERE phone = ?',
          [providers.join(','), email, user.phone]
        );
        user.authProviders = providers.join(',');
        user.email = email;
      }
    }

    res.json({ success: true, isNewUser, user });
  } catch (err: any) {
    res.status(500).json({ error: err.message });
  }
});

// 8. Link Phone to existing Google account
router.post('/auth/link-phone', async (req: Request, res: Response) => {
  const { userId, phone, code } = req.body;
  if (!userId || !phone || !code) {
    return res.status(400).json({ error: 'userId, phone, and code are required' });
  }

  if (code.length !== 6) {
    return res.status(400).json({ error: 'Invalid verification code' });
  }

  try {
    const user = await dbOperations.get<User>('SELECT * FROM users WHERE phone = ?', [userId]);
    if (!user) {
      return res.status(404).json({ error: 'User not found' });
    }

    // Check if phone number is already linked to another account
    const conflict = await dbOperations.get<User>('SELECT * FROM users WHERE phoneNumber = ? AND phone != ?', [phone, userId]);
    if (conflict) {
      return res.status(400).json({ error: 'Phone number is already linked to another account' });
    }

    const providers = user.authProviders.split(',');
    if (!providers.includes('phone')) {
      providers.push('phone');
    }

    await dbOperations.run(
      'UPDATE users SET phoneNumber = ?, authProviders = ? WHERE phone = ?',
      [phone, providers.join(','), userId]
    );

    const updatedUser = await dbOperations.get<User>('SELECT * FROM users WHERE phone = ?', [userId]);
    res.json({ success: true, user: updatedUser });
  } catch (err: any) {
    res.status(500).json({ error: err.message });
  }
});

function validateUsernameRules(username: string): string | null {
  const clean = username.toLowerCase().trim();
  if (clean.length < 3 || clean.length > 30) {
    return 'Username must be between 3 and 30 characters';
  }
  const allowedRegex = /^[a-z0-9_.]+$/;
  if (!allowedRegex.test(clean)) {
    return 'Username can only contain lowercase letters, numbers, underscores, and periods';
  }
  if (clean.startsWith('.') || clean.startsWith('_')) {
    return 'Username cannot start with a period or underscore';
  }
  if (clean.endsWith('.') || clean.endsWith('_')) {
    return 'Username cannot end with a period or underscore';
  }
  if (clean.includes('..')) {
    return 'Username cannot contain consecutive periods';
  }
  if (clean.includes('._') || clean.includes('_.')) {
    return 'Username cannot contain an underscore next to a period';
  }
  const reserved = ['admin', 'administrator', 'support', 'root', 'quickchat', 'moderator', 'help', 'security', 'system', 'null', 'undefined'];
  if (reserved.includes(clean)) {
    return 'This username is reserved and cannot be used';
  }
  return null;
}

// 8.5 Check Username Availability
router.get('/auth/check-username', async (req: Request, res: Response) => {
  const username = req.query.username as string;
  const userId = req.query.userId as string;
  if (!username) {
    return res.status(400).json({ error: 'Username is required' });
  }

  const cleanUsername = username.toLowerCase().trim();
  const validationError = validateUsernameRules(cleanUsername);
  if (validationError) {
    return res.json({ available: false, error: validationError });
  }

  try {
    const conflict = await dbOperations.get<User>(
      'SELECT * FROM users WHERE LOWER(username) = ? AND phone != ?',
      [cleanUsername, userId || '']
    );
    if (conflict) {
      return res.json({ available: false, error: 'Username is already taken' });
    }
    return res.json({ available: true });
  } catch (err: any) {
    return res.status(500).json({ error: err.message });
  }
});

// 9. Update/Set Unique Username
router.post('/auth/update-username', async (req: Request, res: Response) => {
  const { userId, username } = req.body;
  if (!userId || !username) {
    return res.status(400).json({ error: 'userId and username are required' });
  }

  const cleanUsername = username.toLowerCase().trim();
  const validationError = validateUsernameRules(cleanUsername);
  if (validationError) {
    return res.status(400).json({ error: validationError });
  }

  try {
    const user = await dbOperations.get<User>('SELECT * FROM users WHERE phone = ?', [userId]);
    if (!user) {
      return res.status(404).json({ error: 'User not found' });
    }

    // 30-day cooldown check (except if setting for the first time, i.e., current username is null or empty)
    if (user.username && user.usernameLastChanged) {
      const cooldownMs = 30 * 24 * 60 * 60 * 1000;
      const elapsed = Date.now() - user.usernameLastChanged;
      if (elapsed < cooldownMs) {
        const remainingDays = Math.ceil((cooldownMs - elapsed) / (24 * 60 * 60 * 1000));
        return res.status(400).json({ error: `Username can only be changed once every 30 days. Try again in ${remainingDays} days.` });
      }
    }

    // Uniqueness check (case-insensitive)
    const conflict = await dbOperations.get<User>('SELECT * FROM users WHERE LOWER(username) = ? AND phone != ?', [cleanUsername, userId]);
    if (conflict) {
      return res.status(400).json({ error: 'Username is already taken' });
    }

    await dbOperations.run(
      'UPDATE users SET username = ?, usernameLastChanged = ? WHERE phone = ?',
      [cleanUsername, Date.now(), userId]
    );

    const updatedUser = await dbOperations.get<User>('SELECT * FROM users WHERE phone = ?', [userId]);
    res.json({ success: true, user: updatedUser });
  } catch (err: any) {
    res.status(500).json({ error: err.message });
  }
});

// 10. Search by Username (Rate-limited, Privacy-sensitive)
router.get('/users/search', async (req: Request, res: Response) => {
  const username = req.query.username as string;
  const requesterId = (req.query.requesterId as string) || (req.headers['x-user-id'] as string);

  if (!username) {
    return res.status(400).json({ error: 'Username parameter is required' });
  }

  let cleanUsername = username.toLowerCase().trim();
  if (cleanUsername.startsWith('@')) {
    cleanUsername = cleanUsername.substring(1);
  }

  if (requesterId) {
    const underLimit = checkSearchRateLimit(requesterId);
    if (!underLimit) {
      return res.status(429).json({ error: 'Search rate limit exceeded. Please wait a minute before searching again.' });
    }
  }

  try {
    const user = await dbOperations.get<User>('SELECT * FROM users WHERE LOWER(username) = ?', [cleanUsername]);
    if (!user || user.usernameSearchEnabled === 0) {
      return res.status(404).json({ error: 'User not found' });
    }

    res.json({ success: true, user });
  } catch (err: any) {
    res.status(500).json({ error: err.message });
  }
});

// 11. Update Search privacy
router.post('/users/privacy', async (req: Request, res: Response) => {
  const { userId, usernameSearchEnabled } = req.body;
  if (!userId || usernameSearchEnabled === undefined) {
    return res.status(400).json({ error: 'userId and usernameSearchEnabled are required' });
  }

  try {
    const val = usernameSearchEnabled ? 1 : 0;
    await dbOperations.run('UPDATE users SET usernameSearchEnabled = ? WHERE phone = ?', [val, userId]);
    res.json({ success: true });
  } catch (err: any) {
    res.status(500).json({ error: err.message });
  }
});

// 12. Delete account & purge data
router.delete('/auth/account/:userId', async (req: Request, res: Response) => {
  const { userId } = req.params;
  if (!userId) {
    return res.status(400).json({ error: 'userId is required' });
  }

  try {
    const user = await dbOperations.get<User>('SELECT * FROM users WHERE phone = ? OR phoneNumber = ?', [userId, userId]);
    if (user) {
      const targetPhone = user.phone;
      await dbOperations.run('DELETE FROM users WHERE phone = ?', [targetPhone]);
      await dbOperations.run('DELETE FROM prekeys WHERE phone = ?', [targetPhone]);
      await dbOperations.run('DELETE FROM messages WHERE sender = ? OR recipient = ?', [targetPhone, targetPhone]);
      await dbOperations.run('DELETE FROM status WHERE sender = ?', [targetPhone]);
      await dbOperations.run('DELETE FROM status_views WHERE viewer = ?', [targetPhone]);
      await dbOperations.run('DELETE FROM blocked_contacts WHERE blocker = ? OR blocked = ?', [targetPhone, targetPhone]);
      await dbOperations.run('DELETE FROM pending_events WHERE recipient = ?', [targetPhone]);
    }
    res.json({ success: true });
  } catch (err: any) {
    res.status(500).json({ error: err.message });
  }
});

// 13. Block Contact
router.post('/users/block', async (req: Request, res: Response) => {
  const { blockerPhone, blockedPhone } = req.body;
  if (!blockerPhone || !blockedPhone) {
    return res.status(400).json({ error: 'blockerPhone and blockedPhone are required' });
  }
  try {
    await dbOperations.run(
      'INSERT OR REPLACE INTO blocked_contacts (blocker, blocked) VALUES (?, ?)',
      [blockerPhone, blockedPhone]
    );
    res.json({ success: true });
  } catch (err: any) {
    res.status(500).json({ error: err.message });
  }
});

// 14. Unblock Contact
router.post('/users/unblock', async (req: Request, res: Response) => {
  const { blockerPhone, blockedPhone } = req.body;
  if (!blockerPhone || !blockedPhone) {
    return res.status(400).json({ error: 'blockerPhone and blockedPhone are required' });
  }
  try {
    await dbOperations.run(
      'DELETE FROM blocked_contacts WHERE blocker = ? AND blocked = ?',
      [blockerPhone, blockedPhone]
    );
    res.json({ success: true });
  } catch (err: any) {
    res.status(500).json({ error: err.message });
  }
});

// 15. Get Blocked Contacts list
router.get('/users/blocked/:phone', async (req: Request, res: Response) => {
  const { phone } = req.params;
  if (!phone) {
    return res.status(400).json({ error: 'phone is required' });
  }
  try {
    const blocked = await dbOperations.all<{ blocked: string }>(
      'SELECT blocked FROM blocked_contacts WHERE blocker = ?',
      [phone]
    );
    res.json({ success: true, blocked: blocked.map(b => b.blocked) });
  } catch (err: any) {
    res.status(500).json({ error: err.message });
  }
});

// 16. Report User Moderation
router.post('/users/report', async (req: Request, res: Response) => {
  const { reporterPhone, reportedPhone, reason, description, attachMessages, messages } = req.body;
  if (!reporterPhone || !reportedPhone || !reason) {
    return res.status(400).json({ error: 'reporterPhone, reportedPhone, and reason are required' });
  }
  try {
    const id = Date.now().toString() + '-' + Math.round(Math.random() * 1e9);
    const messagesJson = attachMessages ? JSON.stringify(messages) : '[]';
    await dbOperations.run(
      `INSERT INTO moderation_reports (id, reporter, reported, reason, description, messagesJson, timestamp)
       VALUES (?, ?, ?, ?, ?, ?, ?)`,
      [id, reporterPhone, reportedPhone, reason, description || null, messagesJson, Date.now()]
    );
    res.json({ success: true });
  } catch (err: any) {
    res.status(500).json({ error: err.message });
  }
});

// 17. Message Deletion (REST fallback)
router.post('/messages/:id/delete', async (req: Request, res: Response) => {
  const messageId = req.params.id;
  const { requesterPhone, recipientPhone, mode } = req.body;
  if (!requesterPhone || !mode) {
    return res.status(400).json({ error: 'requesterPhone and mode are required' });
  }

  try {
    if (mode === 'everyone') {
      const queued = await dbOperations.get<{ sender: string }>('SELECT sender FROM messages WHERE id = ?', [messageId]);
      if (queued && queued.sender !== requesterPhone) {
        return res.status(403).json({ error: 'Unauthorized: Only sender can delete for everyone' });
      }
      await dbOperations.run('UPDATE messages SET ciphertext = ?, isDeleted = 1 WHERE id = ?', ['', messageId]);
      if (recipientPhone) {
        const delPayload = { messageId, deletedBy: requesterPhone, mode: 'everyone' };
        await dbOperations.run(
          'INSERT INTO pending_events (recipient, event, payloadText, timestamp) VALUES (?, ?, ?, ?)',
          [recipientPhone, 'message-deleted', JSON.stringify(delPayload), Date.now()]
        );
      }
      return res.json({ success: true });
    } else if (mode === 'me') {
      const queued = await dbOperations.get<{ deletedFor: string }>('SELECT deletedFor FROM messages WHERE id = ?', [messageId]);
      if (queued) {
        let list: string[] = [];
        try { list = JSON.parse(queued.deletedFor || '[]'); } catch (e) {}
        if (!list.includes(requesterPhone)) {
          list.push(requesterPhone);
          await dbOperations.run('UPDATE messages SET deletedFor = ? WHERE id = ?', [JSON.stringify(list), messageId]);
        }
      }
      return res.json({ success: true });
    }
    return res.status(400).json({ error: 'Invalid mode' });
  } catch (err: any) {
    return res.status(500).json({ error: err.message });
  }
});

export default router;
