# Security & Threat Model Configuration — Quick Chat

This document details the security design, threat models, metadata leakage prevention, and local key storage policies for Quick Chat.

---

## 🎯 Security Goals
- **Confidentiality:** Plaintext messages, media, voice notes, and contact lists must never be readable by anyone except the sender and recipient.
- **Integrity:** Message tampering must be detected cryptographically (AES-GCM tag verification).
- **Forward Secrecy:** If a user's long-term identity private key is compromised in the future, past message sessions cannot be decrypted.
- **Break-in Recovery:** If an ephemeral session key is compromised, subsequent messages will regain confidentiality as soon as the Double Ratchet performs a DH step.
- **At-Rest Protection:** Local databases must be secure even if the physical device is stolen or rooted.

---

## 🔍 Threat Model & Trust Boundaries

### 1. What the Server CANNOT See
- **Message Plaintext:** The server only relays the `ciphertext` parameter.
- **Media Content:** The server stores encrypted binary blobs. The key to decrypt the media file is sent inside the E2E encrypted message metadata, meaning the server cannot decrypt file storage.
- **Private Keys:** The server never receives, stores, or processes private keys.

### 2. What the Server CAN See (Metadata Leakage)
- **Routing Metadata:** The server must see the `sender`, `recipient`, and `timestamp` to route messages.
- **Traffic Patterns:** The server knows the size of the ciphertext payloads and the frequency of communications.
- **Presence Details:** The server coordinates the online/offline presence status.

---

## 🔑 Key Management & Device Storage

### 1. Local Device Protection (Room + SQLCipher)
Message history is saved locally using an encrypted Room SQLite database powered by **SQLCipher**.
- The database passphrase is a secure 256-bit key.
- The passphrase is encrypted and sealed using the **Android Keystore System** via a hardware-backed AES-GCM Master Key (where available).
- The encrypted passphrase is saved in SharedPreferences. On boot, the Keystore decrypts the passphrase, allowing Room to attach.

### 2. Private Key Isolation
Cryptographic session keys (Identity Private Key, Signed Prekey Private Key, One-time Prekey Private Keys) are:
- Kept isolated within Secure SharedPreferences.
- Never logged, printed, or sent over network sockets.

### 3. One-Time Prekey Rotation (OTPKs)
To prevent reuse attacks:
- The server stores a list of Bob's One-Time Prekeys.
- When Alice requests Bob's prekey bundle, the server returns a single One-Time Prekey and **immediately deletes** it from the server database.
- Once Bob receives the first message, he resolves and deletes his local copy of that One-Time Prekey.
