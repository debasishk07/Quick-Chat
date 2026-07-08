"use strict";
var __importDefault = (this && this.__importDefault) || function (mod) {
    return (mod && mod.__esModule) ? mod : { "default": mod };
};
Object.defineProperty(exports, "__esModule", { value: true });
const express_1 = require("express");
const multer_1 = __importDefault(require("multer"));
const path_1 = __importDefault(require("path"));
const fs_1 = __importDefault(require("fs"));
const database_1 = require("./database");
const router = (0, express_1.Router)();
// Multer setup for media upload
const storage = multer_1.default.diskStorage({
    destination: (req, file, cb) => {
        const uploadPath = path_1.default.resolve(__dirname, '../uploads');
        if (!fs_1.default.existsSync(uploadPath)) {
            fs_1.default.mkdirSync(uploadPath, { recursive: true });
        }
        cb(null, uploadPath);
    },
    filename: (req, file, cb) => {
        const uniqueSuffix = Date.now() + '-' + Math.round(Math.random() * 1e9);
        cb(null, file.fieldname + '-' + uniqueSuffix + path_1.default.extname(file.originalname));
    }
});
const upload = (0, multer_1.default)({ storage });
// 1. Mock OTP verification
router.post('/auth/verify-otp', async (req, res) => {
    const { phone, code } = req.body;
    if (!phone || !code) {
        return res.status(400).json({ error: 'Phone and code are required' });
    }
    // Accept any 6 digit code for local verification
    if (code.length === 6) {
        // Check if user exists
        const user = await database_1.dbOperations.get('SELECT * FROM users WHERE phone = ?', [phone]);
        return res.json({ success: true, isNewUser: !user, user });
    }
    return res.status(400).json({ error: 'Invalid verification code' });
});
// 2. Register/Update profile
router.post('/auth/register-profile', async (req, res) => {
    const { phone, displayName, avatarUrl, about } = req.body;
    if (!phone || !displayName) {
        return res.status(400).json({ error: 'Phone and displayName are required' });
    }
    try {
        const existing = await database_1.dbOperations.get('SELECT * FROM users WHERE phone = ?', [phone]);
        if (existing) {
            await database_1.dbOperations.run('UPDATE users SET displayName = ?, avatarUrl = ?, about = ?, lastSeen = ?, isOnline = ? WHERE phone = ?', [displayName, avatarUrl || existing.avatarUrl, about || existing.about, Date.now(), 1, phone]);
        }
        else {
            await database_1.dbOperations.run('INSERT INTO users (phone, displayName, avatarUrl, about, lastSeen, isOnline) VALUES (?, ?, ?, ?, ?, ?)', [phone, displayName, avatarUrl || null, about || 'Hey there! I am using Quick Chat.', Date.now(), 1]);
        }
        const user = await database_1.dbOperations.get('SELECT * FROM users WHERE phone = ?', [phone]);
        res.json({ success: true, user });
    }
    catch (err) {
        res.status(500).json({ error: err.message });
    }
});
// 3. Contact Sync
router.post('/contacts/sync', async (req, res) => {
    const { phones } = req.body; // Array of phone numbers
    if (!Array.isArray(phones)) {
        return res.status(400).json({ error: 'Phones array is required' });
    }
    try {
        if (phones.length === 0) {
            return res.json({ contacts: [] });
        }
        const placeholders = phones.map(() => '?').join(',');
        const contacts = await database_1.dbOperations.all(`SELECT * FROM users WHERE phone IN (${placeholders})`, phones);
        res.json({ contacts });
    }
    catch (err) {
        res.status(500).json({ error: err.message });
    }
});
// 4. Upload PreKeys
router.post('/prekeys', async (req, res) => {
    const { phone, identityKey, signedPreKey, signedPreKeySignature, oneTimePreKeys } = req.body;
    if (!phone || !identityKey || !signedPreKey || !signedPreKeySignature || !Array.isArray(oneTimePreKeys)) {
        return res.status(400).json({ error: 'Invalid prekey bundle data' });
    }
    try {
        const prekeysJson = JSON.stringify(oneTimePreKeys);
        const existing = await database_1.dbOperations.get('SELECT phone FROM prekeys WHERE phone = ?', [phone]);
        if (existing) {
            await database_1.dbOperations.run('UPDATE prekeys SET identityKey = ?, signedPreKey = ?, signedPreKeySignature = ?, oneTimePreKeys = ? WHERE phone = ?', [identityKey, signedPreKey, signedPreKeySignature, prekeysJson, phone]);
        }
        else {
            await database_1.dbOperations.run('INSERT INTO prekeys (phone, identityKey, signedPreKey, signedPreKeySignature, oneTimePreKeys) VALUES (?, ?, ?, ?, ?)', [phone, identityKey, signedPreKey, signedPreKeySignature, prekeysJson]);
        }
        res.json({ success: true });
    }
    catch (err) {
        res.status(500).json({ error: err.message });
    }
});
// 5. Get PreKey Bundle (pop one one-time prekey for X3DH session establishment)
router.get('/prekeys/:phone', async (req, res) => {
    const { phone } = req.params;
    try {
        const bundle = await database_1.dbOperations.get('SELECT * FROM prekeys WHERE phone = ?', [phone]);
        if (!bundle) {
            return res.status(404).json({ error: 'Prekey bundle not found for user' });
        }
        const oneTimeKeys = JSON.parse(bundle.oneTimePreKeys);
        let poppedKey = null;
        if (oneTimeKeys.length > 0) {
            poppedKey = oneTimeKeys.shift() || null;
            // Save remaining one-time keys back to db
            await database_1.dbOperations.run('UPDATE prekeys SET oneTimePreKeys = ? WHERE phone = ?', [JSON.stringify(oneTimeKeys), phone]);
        }
        res.json({
            phone: bundle.phone,
            identityKey: bundle.identityKey,
            signedPreKey: bundle.signedPreKey,
            signedPreKeySignature: bundle.signedPreKeySignature,
            oneTimePreKey: poppedKey // single key returned for session setup
        });
    }
    catch (err) {
        res.status(500).json({ error: err.message });
    }
});
// 6. Media upload
router.post('/media/upload', upload.single('file'), (req, res) => {
    if (!req.file) {
        return res.status(400).json({ error: 'No file uploaded' });
    }
    // Construct download URL
    const fileUrl = `${req.protocol}://${req.get('host')}/uploads/${req.file.filename}`;
    res.json({ success: true, fileUrl });
});
exports.default = router;
