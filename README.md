# Quick Chat — E2EE Android Messaging Client & Backend

Quick Chat is a production-quality, modern, end-to-end encrypted messaging application patterned after WhatsApp/Signal. It features real-time messaging, presence tracking, typing indicators, stories, and offline-first queue syncing.

---

## 🛠 Tech Stack
- **Android App:** Kotlin, Jetpack Compose, Material 3, Coroutines & Flow, Hilt (DI), Room (encapsulated with SQLCipher), CameraX, ExoPlayer, Coil.
- **Backend:** Node.js (TypeScript), Express, Socket.IO (WebSockets), SQLite.
- **E2EE Cryptography:** Signal Protocol concepts (X3DH Key Agreement, Double Ratchet, Sender Keys for groups, and AES-GCM-256 for media files).

---

## 🏗 Modular Architecture (Android)
The Android client uses clean, decoupled MVVM architecture divided into layers:

- `:app` — Launcher activity, Hilt application graph, and Navigation Compose routes.
- `:feature:auth` — Onboarding, OTP validation view, and profile setups.
- `:feature:chat` — Chat feeds list, bubble dialog screens, reactions, and security code checking.
- `:feature:status` — stories lists, auto-advancing viewer slideshow, and creator panels.
- `:core:crypto` — Double Ratchet, X3DH key agreement, media encryption, and Android Keystore seal.
- `:core:database` — Room tables encrypted by SQLCipher at rest, DAOs, and outbox queues.
- `:core:network` — Socket.IO client, Retrofit services, and synchronization repositories.
- `:core:model` — Shared business entities (`User`, `Message`, `Chat`, `Status`).

---

## 🔐 How Encryption Works
1. **Key Setup (X3DH):** Upon first registration, the app generates an Identity Key pair, a Signed Prekey pair, and 10 One-Time Prekeys. The public keys are uploaded to the server prekey bundle.
2. **Session Initialization:** When Alice starts a chat with Bob, she fetches Bob's prekey bundle from `/api/prekeys/:phone`. She computes a shared master secret via ECDH (secp256r1) on the keys (X3DH).
3. **Double Ratchet:** For every message sent, Alice ratchets her sending chain. Each message carries Alice's current ephemeral public key. Bob performs a DH ratchet step upon receiving to synchronize, maintaining Forward Secrecy and Break-in Recovery.
4. **Media Security:** Heavy media (images/videos/audio) are encrypted locally using a random 256-bit AES-GCM key. The media ciphertext is uploaded to `/api/media/upload`, and the AES decryption key is sent wrapped inside the encrypted text message payload.
5. **Group E2EE:** Done via **Sender Keys** (like Signal/WhatsApp). Each member publishes their symmetric Sender Key to group members once via 1:1 sessions, and then broadcasts group ciphertexts.

---

## 🚀 How to Run Locally

### 1. Start the Node.js Backend Server
Make sure you have Node.js installed, then execute:
```bash
cd backend
npm install
npm run build
npm start
```
The server will start on `http://localhost:3000`. Serving uploaded media on `/uploads` and SQLite on `quickchat.db`.

### 2. Run the Android App
1. Open the `android/` directory in **Android Studio**.
2. Android Studio will automatically resolve dependencies via the Version Catalog (`libs.versions.toml`).
3. Connect an emulator or physical device.
   > [!NOTE]
   > The Android client points to `http://10.0.2.2:3000` (the standard Android loopback IP mapping to the host's localhost). If testing on a physical device, update the IP address inside `NetworkModule.kt` and `ChatRepository.kt` to match your local Wi-Fi router IP.
4. Run the project. You can run two emulators side-by-side to test real-time E2EE messaging and status updates!

---

## 🧪 Running Tests
To run the cryptographic unit tests:
```bash
cd android
./gradlew :core:crypto:test
```
This runs `CryptoUnitTest.kt` verifying ECDH agreements, Double Ratchet session handshakes, QR fingerprints, and symmetric media decryption.
