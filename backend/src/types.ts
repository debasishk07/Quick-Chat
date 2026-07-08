export interface User {
  phone: string; // stable internal user ID (maps to client user ID/phone)
  phoneNumber: string | null;
  email: string | null;
  username: string | null;
  usernameLastChanged: number;
  authProviders: string; // e.g. "phone" or "google,phone"
  displayName: string;
  avatarUrl: string | null;
  about: string | null;
  lastSeen: number;
  isOnline: number; // 0 or 1
  usernameSearchEnabled: number; // 0 or 1
}

export interface PreKeyBundle {
  phone: string;
  identityKey: string; // Base64
  signedPreKey: string; // Base64
  signedPreKeySignature: string; // Base64
  oneTimePreKeys: string; // JSON Array of Base64 strings
}

export interface MessagePayload {
  id: string;
  sender: string;
  recipient: string; // Phone number (or group ID)
  isGroup: number; // 0 or 1
  ciphertext: string; // E2E Encrypted message payload (Base64)
  iv: string; // Base64 Initialization Vector for AES
  ephemeralPublicKey: string | null; // Base64, for X3DH/Double Ratchet
  messageType: 'TEXT' | 'IMAGE' | 'VIDEO' | 'VOICE' | 'DOCUMENT' | 'LOCATION' | 'CONTACT';
  timestamp: number;
  status: 'SENDING' | 'SENT' | 'DELIVERED' | 'READ';
}

export interface StatusPayload {
  id: string;
  sender: string;
  mediaUrl: string; // Encrypted media URL
  caption: string | null; // E2E Encrypted caption or plain
  mediaType: 'TEXT' | 'IMAGE' | 'VIDEO';
  timestamp: number;
  expiresAt: number;
}

export interface StatusViewPayload {
  statusId: string;
  viewer: string;
  timestamp: number;
}
