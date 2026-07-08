# Quick Chat

<div align="center">

![Quick Chat Banner](https://img.shields.io/badge/Quick%20Chat-E2EE%20Messaging-6366f1?style=for-the-badge&logo=android&logoColor=white)

[![License](https://img.shields.io/badge/License-MIT-green.svg?style=flat-square)](LICENSE)
[![Android](https://img.shields.io/badge/Android-Kotlin%20|%20Jetpack%20Compose-3DDC84?style=flat-square&logo=android&logoColor=white)](https://developer.android.com/kotlin)
[![Backend](https://img.shields.io/badge/Backend-Node.js%20|%20TypeScript-339933?style=flat-square&logo=nodedotjs&logoColor=white)](https://nodejs.org)
[![Security](https://img.shields.io/badge/Security-Signal%20Protocol-00d4aa?style=flat-square&logo=signal&logoColor=white)](https://signal.org/docs/)
[![Build](https://img.shields.io/badge/Build-Gradle%20KTS-02303A?style=flat-square&logo=gradle&logoColor=white)](https://gradle.org)

**A production-quality, end-to-end encrypted messaging application inspired by WhatsApp and Signal.**  
Real-time messaging • Presence tracking • Typing indicators • Stories (Status) • Offline-first queue syncing • [syncing]

</div>

---

## ✨ Features Overview

| Feature | Description |
|---------|-------------|
| 🔐 **End-to-End Encryption** | Signal Protocol: X3DH key agreement, Double Ratchet, Sender Keys for groups, AES-GCM-256 media encryption |
| 💬 **Real-time Messaging** | WebSocket (Socket.IO) with delivery/read receipts, typing indicators, reactions |
| 👥 **Group Chats** | E2EE group messaging via Sender Keys; admin controls, member management |
| 📱 **Stories (Status)** | 24-hour ephemeral stories with text/media, auto-advance viewer, view receipts |
| 🔄 **Offline Queue** | Messages queued server-side when recipient offline; delivered on reconnect |
| 👤 **Multi-Auth** | Phone OTP + Google Sign-In; link phone to existing Google account |
| 🔍 **Username Search** | Unique @usernames with privacy controls & rate limiting |
| 🎨 **Custom Theming** | Material 3 dynamic theming, chat wallpapers, font scaling, dark/light/system modes |
| 🛡 **Security Verification** | QR code safety number comparison, security code verification screen |
| 📴 **Offline-First** | Room + SQLCipher local DB; outbox queue with exponential backoff retry |

---

## 🏗 Architecture

### Project Structure

```
Quick-Chat/
├── android/                    # Android Client (Kotlin + Jetpack Compose)
│   ├── app/                    # Main application module
│   ├── core/
│   │   ├── crypto/             # Signal Protocol implementation
│   │   ├── database/           # Room + SQLCipher encrypted DB
│   │   ├── model/              # Shared entities (User, Message, Chat, Status)
│   │   └── network/            # Socket.IO, Retrofit, Repositories
│   └── feature/
│       ├── auth/               # Login, OTP, Profile, Settings
│       ├── chat/               # Chat list, chat room, security verify
│       └── status/             # Status list, creator, viewer
│
├── backend/                    # Node.js + TypeScript Backend
│   ├── src/
│   │   ├── index.ts            # Express + Socket.IO server entry
│   │   ├── routes.ts           # REST API endpoints
│   │   ├── socket.ts           # Real-time WebSocket handlers
│   │   ├── database.ts         # SQLite schema + operations
│   │   └── types.ts            # Shared TypeScript interfaces
│   └── uploads/                # Encrypted media file storage
│
├── SECURITY.md                 # Threat model & security documentation
└── README.md                   # This file
```

### Android Modular Architecture (Clean MVVM)

```
┌─────────────────────────────────────────────────────────────┐
│                        :app                                 │
│  MainActivity | Hilt Graph | Navigation Compose Routes     │
└─────────────────────────────────────────────────────────────┘
                              │
        ┌─────────────────────┼─────────────────────┐
        ▼                     ▼                     ▼
┌───────────────┐    ┌───────────────┐    ┌───────────────┐
│ feature:auth  │    │ feature:chat  │    │ feature:status│
│ Login, OTP,   │    │ ChatList,     │    │ StatusList,   │
│ ProfileSetup, │    │ ChatRoom,     │    │ StatusCreator,│
│ Settings      │    │ SecurityVerify│    │ StatusViewer  │
└───────┬───────┘    └───────┬───────┘    └───────┬───────┘
        │                    │                    │
        └────────────────────┼────────────────────┘
                             ▼
        ┌───────────────────────────────────────┐
        │            Core Modules               │
        ├──────────┬──────────┬────────┬────────┤
        │:core:    │:core:    │:core:  │:core:  │
        │crypto    │database  │network │model   │
        │DoubleRat-│Room +    │Socket. │User,   │
        │chet,X3DH,│SQLCipher │IO,     │Message,│
        │MediaEnc  │DAOs      │Retrofit│Chat,   │
        │          │Outbox    │Sync    │Status  │
        └──────────┴──────────┴────────┴────────┘
```

---

## 🔐 End-to-End Encryption Deep Dive

Quick Chat implements the **Signal Protocol** with these cryptographic primitives:

### 1. X3DH (Extended Triple Diffie-Hellman) Key Agreement
```
┌─────────────┐     PreKey Bundle      ┌─────────────┐
│   Alice     │ ─────────────────────► │     Bob     │
│ (Initiator) │  Identity Key          │ (Responder) │
│             │  Signed PreKey         │             │
│             │  10 One-Time PreKeys   │             │
└─────────────┘                        └─────────────┘
        │                                     │
        ▼                                     ▼
┌─────────────────────────────────────────────────────┐
│              Shared Master Secret                   │
│  DH1 = DH(IK_A, SPK_B)                              │
│  DH2 = DH(EK_A, IK_B)                               │
│  DH3 = DH(EK_A, SPK_B)                              │
│  DH4 = DH(EK_A, OPK_B)  (if available)             │
│  SK = HKDF(DH1 || DH2 || DH3 || DH4)               │
└─────────────────────────────────────────────────────┘
```
- **Curve**: secp256r1 (NIST P-256) via Android Keystore
- **PreKeys**: 1 Identity Key + 1 Signed PreKey (rotated periodically) + 10 One-Time PreKeys
- **Server Role**: Stores public prekey bundles only; **never sees private keys**

### 2. Double Ratchet (Per-Message Forward Secrecy)
```
Sending Chain                    Receiving Chain
─────────────────                ─────────────────
Chain Key ──────► Message Key     Chain Key ──────► Message Key
   │                 │               │                 │
   ▼                 ▼               ▼                 ▼
Next Chain Key    Ciphertext     Next Chain Key   Plaintext
```
- **Symmetric Ratchet**: HMAC-SHA256 derives next chain key + message key
- **DH Ratchet**: New ephemeral key pair on direction change; root key updated via HKDF
- **Skipped Message Keys**: Stored for out-of-order delivery (up to configurable window)
- **Forward Secrecy**: Compromised session key ≠ past messages decryptable
- **Break-in Recovery**: New DH ratchet step restores confidentiality

### 3. Media Encryption (AES-GCM-256)
```
┌──────────────────────────────────────────────────────┐
│  Large Media (Image/Video/Audio)                     │
└──────────────────────────────────────────────────────┘
                        │
                        ▼
┌──────────────────────────────────────────────────────┐
│  1. Generate random 256-bit Content Encryption Key  │
│  2. Encrypt media: AES-GCM-256 (CEK, random IV)     │
│  3. Upload ciphertext to /api/media/upload          │
│  4. Wrap CEK in Double Ratchet message payload      │
│  5. Send encrypted message with media metadata      │
└──────────────────────────────────────────────────────┘
```
- Server stores **only encrypted blobs** — cannot decrypt content
- CEK never leaves E2EE channel; delivered inside encrypted message

### 4. Group Encryption (Sender Keys)
- Each member generates a symmetric **Sender Key** (Chain Key + Signing Key)
- Sender Key distributed **once** via pairwise 1:1 Double Ratchet sessions
- Group messages encrypted with Sender Key (AES-GCM + HMAC for integrity)
- Scales efficiently: O(1) encryption per message regardless of group size

### 5. Local Key Storage (At-Rest Protection)
```
┌────────────────────────────────────────────────────────┐
│ Android Keystore (Hardware-Backed)                     │
│  ┌──────────────────────────────────────────────────┐  │
│  │ AES-GCM Master Key (generated in TEE/StrongBox)  │  │
│  │  │                                             │  │
│  │  ▼ Encrypts                                    │  │
│  │ ┌────────────────────────────────────────────┐  │  │
│  │ │ SQLCipher Database Passphrase (256-bit)    │  │  │
│  │ └────────────────────────────────────────────┘  │  │
│  │  │                                             │  │
│  │  ▼ Encrypts (via Room + SQLCipher)             │  │
│  │ ┌────────────────────────────────────────────┐  │  │
│  │ │ Messages, Sessions, PreKeys, Outbox Queue  │  │  │
│  │ └────────────────────────────────────────────┘  │  │
│  └──────────────────────────────────────────────────┘  │
└────────────────────────────────────────────────────────┘
```
- **SQLCipher** encrypts entire SQLite database at rest
- Passphrase sealed by **Android Keystore** (hardware-backed when available)
- Private keys never logged, serialized, or sent over network

---

## 🛠 Tech Stack

### Android Client
| Category | Technologies |
|----------|--------------|
| **Language** | Kotlin 1.9+ |
| **UI** | Jetpack Compose, Material 3, Material Design 3 Expressive |
| **Architecture** | MVVM, Clean Architecture, Unidirectional Data Flow |
| **DI** | Hilt (Dagger) |
| **Async** | Coroutines, Flow, StateFlow, SharedFlow |
| **Database** | Room + SQLCipher (encrypted at rest) |
| **Network** | Socket.IO (WebSocket), Retrofit (REST), OkHttp |
| **Image Loading** | Coil |
| **Media** | CameraX, ExoPlayer |
| **Crypto** | Android Keystore, javax.crypto, BouncyCastle (spongycastle) |
| **Navigation** | Navigation Compose |
| **Testing** | JUnit, Turbine, Compose Testing, CryptoUnitTest |

### Backend Server
| Category | Technologies |
|----------|--------------|
| **Runtime** | Node.js 20+, TypeScript 5.4+ |
| **Framework** | Express.js |
| **Real-time** | Socket.IO 4.7 (WebSocket + polling fallback) |
| **Database** | SQLite3 (better-sqlite3 or sqlite3 driver) |
| **File Upload** | Multer (local disk storage) |
| **Auth** | Mock OTP (6-digit), Google OAuth (extensible) |
| **Dev Tools** | ts-node-dev, TypeScript ESLint |

---

## 🚀 Getting Started

### Prerequisites
- **Android Studio** Ladybug (2024.2.1+) or newer
- **JDK 17+** (for Android Gradle Plugin 8.5+)
- **Node.js 20+** & **npm 10+** (for backend)
- **Android SDK 34** (compileSdk), **minSdk 24**

---

### 1. Backend Setup

```bash
# Navigate to backend directory
cd backend

# Install dependencies
npm install

# Build TypeScript
npm run build

# Start production server
npm start

# OR start development server with hot reload
npm run dev
```

**Server runs on:** `http://localhost:3000`  
**API Base:** `http://localhost:3000/api`  
**Media:** `http://localhost:3000/uploads`  
**Database:** `backend/quickchat.db` (auto-created)

#### Environment Variables (Optional)
```bash
PORT=3000                    # Server port (default: 3000)
NODE_ENV=production          # Environment
```

---

### 2. Android App Setup

```bash
# Open in Android Studio
# File → Open → select the 'android/' directory

# Android Studio will:
# 1. Sync Gradle (uses Version Catalog: gradle/libs.versions.toml)
# 2. Download dependencies
# 3. Build project
```

#### Network Configuration

The app defaults to **Android Emulator loopback** (`http://10.0.2.2:3000`).

| Environment | Configuration |
|-------------|---------------|
| **Emulator** | Works out of the box (10.0.2.2 → host localhost) |
| **Physical Device** | Update `BASE_URL` in `NetworkModule.kt` to your machine's LAN IP (e.g., `http://192.168.1.42:3000`) |
| **WiFi Debugging** | Ensure device & dev machine on same network; firewall allows port 3000 |

**Files to modify for physical device:**
- `android/core/network/src/main/java/com/quickchat/core/network/di/NetworkModule.kt`
- `android/core/network/src/main/java/com/quickchat/core/network/repository/ChatRepository.kt`

---

### 3. Run & Test

```bash
# Run backend (Terminal 1)
cd backend && npm run dev

# Run Android app (Android Studio)
# ▶ Run 'app' configuration on emulator/device

# Run crypto unit tests
cd android && ./gradlew :core:crypto:test
```

**Test two clients:** Run two emulators side-by-side to verify real-time E2EE messaging, status updates, and presence.

---

## 📱 Screens & User Flows

### Authentication Flow
```
┌─────────┐    ┌─────────┐    ┌──────────────┐    ┌────────┐
│  Login  │───►│  OTP    │───►│ Profile Setup │───►│  Home  │
│ (Phone) │    │ Verify  │    │ (Name/Avatar) │    │ (Chat) │
└─────────┘    └─────────┘    └──────────────┘    └────────┘
     │                                       ▲
     │         ┌───────────────────────────┘
     └────────►│ Google Sign-In (Alternative)
               └───────────────────────────┘
```

### Main Navigation (Bottom Bar)
| Tab | Screen | Key Features |
|-----|--------|--------------|
| 💬 **Chats** | `ChatListScreen` | Chat list, search, archive, new chat FAB |
| 📸 **Status** | `StatusListScreen` | Story viewer, creator, view receipts, 24h expiry |
| 👤 **Profile** | `ProfileScreen` | Settings, username, privacy, theme, wallpaper |

### Chat Room Features
- **Message Bubbles**: Text, Image, Video, Audio, Location, Contact
- **Reactions**: Long-press → emoji picker (👍 ❤️ 😂 😮 😢 😡)
- **Delivery States**: ⏳ SENT → ✓✓ DELIVERED → ✓✓ READ (blue)
- **Typing Indicator**: "User is typing..." real-time
- **Security Verification**: QR code + 60-digit safety number comparison
- **Media Viewer**: Full-screen zoom, swipe gallery, download
- **Wallpaper & Font**: Per-chat customization

### Status (Stories) System
```
┌─────────────────────────────────────────────────────────┐
│                    Status List                          │
│  ┌─────────┐  ┌─────────┐  ┌─────────┐  ┌─────────┐    │
│  │ My      │  │ Alice   │  │ Bob     │  │ Carol   │    │
│  │ Status  │  │ 🟢 2h   │  │ 🟢 5h   │  │ 🔴 23h  │    │
│  │ (+)     │  │ 3 views │  │ 1 view  │  │ 0 views │    │
│  └─────────┘  └─────────┘  └─────────┘  └─────────┘    │
└─────────────────────────────────────────────────────────┘
                          │
          ┌───────────────┼───────────────┐
          ▼               ▼               ▼
   ┌────────────┐  ┌────────────┐  ┌────────────┐
   │   Text     │  │   Photo/   │  │   Video    │
   │  Status    │  │   Video    │  │  Status    │
   └────────────┘  └────────────┘  └────────────┘
          │               │               │
          └───────────────┼───────────────┘
                          ▼
              ┌───────────────────────┐
              │   Status Viewer       │
              │  Auto-advance (5s)    │
              │  Tap: pause/next      │
              │  Swipe: next/prev     │
              │  Long-press: pause    │
              │  View receipt sent    │
              └───────────────────────┘
```

---

## 🔌 API Reference

### REST Endpoints (`/api`)

| Method | Endpoint | Description | Auth |
|--------|----------|-------------|------|
| `POST` | `/auth/verify-otp` | Verify 6-digit OTP | Phone |
| `POST` | `/auth/register-profile` | Create/update profile | Phone |
| `POST` | `/auth/google` | Google Sign-In | Google UID |
| `POST` | `/auth/link-phone` | Link phone to Google account | User ID |
| `GET` | `/auth/check-username` | Check username availability | Optional |
| `POST` | `/auth/update-username` | Set/change username (30-day cooldown) | User ID |
| `GET` | `/users/search` | Search by @username (rate-limited) | Requester ID |
| `POST` | `/users/privacy` | Toggle username search visibility | User ID |
| `DELETE` | `/auth/account/:userId` | Delete account & purge data | User ID |
| `POST` | `/contacts/sync` | Sync phone contacts | Phone |
| `POST` | `/prekeys` | Upload prekey bundle | Phone |
| `GET` | `/prekeys/:phone` | Get prekey bundle (consumes 1 OTPK) | Public |
| `POST` | `/media/upload` | Upload encrypted media (multipart) | Phone |

### Socket.IO Events

| Direction | Event | Payload | Description |
|-----------|-------|---------|-------------|
| `Client→Server` | `send-message` | `MessagePayload` | Send E2EE message |
| `Server→Client` | `receive-message` | `MessagePayload + status` | Incoming message |
| `Server→Client` | `offline-messages` | `MessagePayload[]` | Queued messages on connect |
| `Bidirectional` | `message-receipt` | `{messageId, recipient, status, timestamp}` | DELIVERED/READ receipts |
| `Client→Server` | `typing` | `{recipient, isTyping}` | Typing indicator |
| `Server→Client` | `presence-change` | `{phone, isOnline, lastSeen}` | Online/offline broadcast |
| `Server→Client` | `status-update` | `Status` | New story broadcast |
| `Server→Client` | `status-delete` | `{statusId}` | Expired story removal |

---

## 🗄 Database Schema

### Backend (SQLite)
```sql
-- Users
CREATE TABLE users (
  phone TEXT PRIMARY KEY,
  phoneNumber TEXT,
  email TEXT,
  username TEXT UNIQUE,
  usernameLastChanged INTEGER DEFAULT 0,
  authProviders TEXT NOT NULL DEFAULT 'phone',
  displayName TEXT NOT NULL,
  avatarUrl TEXT,
  about TEXT,
  lastSeen INTEGER DEFAULT 0,
  isOnline INTEGER DEFAULT 0,
  usernameSearchEnabled INTEGER DEFAULT 1
);

-- PreKeys (E2EE)
CREATE TABLE prekeys (
  phone TEXT PRIMARY KEY,
  identityKey TEXT NOT NULL,
  signedPreKey TEXT NOT NULL,
  signedPreKeySignature TEXT NOT NULL,
  oneTimePreKeys TEXT NOT NULL  -- JSON array
);

-- Messages (Offline Queue)
CREATE TABLE messages (
  id TEXT PRIMARY KEY,
  sender TEXT NOT NULL,
  recipient TEXT NOT NULL,
  isGroup INTEGER NOT NULL,
  ciphertext TEXT NOT NULL,
  iv TEXT NOT NULL,
  ephemeralPublicKey TEXT,
  messageType TEXT NOT NULL,
  timestamp INTEGER NOT NULL,
  status TEXT NOT NULL  -- SENT, DELIVERED, READ
);

-- Status/Stories (24h TTL)
CREATE TABLE status (
  id TEXT PRIMARY KEY,
  sender TEXT NOT NULL,
  mediaUrl TEXT NOT NULL,
  caption TEXT,
  mediaType TEXT NOT NULL,
  timestamp INTEGER NOT NULL,
  expiresAt INTEGER NOT NULL  -- timestamp + 24h
);

-- Status Views
CREATE TABLE status_views (
  statusId TEXT NOT NULL,
  viewer TEXT NOT NULL,
  timestamp INTEGER NOT NULL,
  PRIMARY KEY (statusId, viewer)
);
```

### Android (Room + SQLCipher)
- `UserEntity`, `ChatEntity`, `MessageEntity`, `SessionEntity`, `StatusEntity`, `OutboxEntity`
- **All tables encrypted** via SQLCipher with Keystore-sealed passphrase
- **Outbox Queue**: Persistent retry with exponential backoff for offline sends

---

## 🧪 Testing

### Crypto Unit Tests
```bash
cd android
./gradlew :core:crypto:test
```
**Coverage:** `CryptoUnitTest.kt` verifies:
- ECDH key agreement (X3DH)
- Double Ratchet session init (Alice/Bob)
- Encryption/decryption round-trip
- DH ratchet forward secrecy
- Skipped message key handling
- QR safety number fingerprint generation
- Media AES-GCM encryption/decryption

### Android Instrumented Tests
```bash
./gradlew connectedAndroidTest
```

### Backend Tests
```bash
cd backend
npm test  # (configure Jest/Vitest as needed)
```

---

## 🔧 Configuration

### Android Version Catalog (`android/gradle/libs.versions.toml`)
Centralized dependency versions for:
- Kotlin, Compose, Material3, Hilt, Room, SQLCipher
- Coroutines, Flow, Navigation, Coil, CameraX, ExoPlayer
- Socket.IO, Retrofit, OkHttp, Logging

### Backend TypeScript Config (`backend/tsconfig.json`)
- Target: ES2022, Module: CommonJS
- Strict mode enabled
- Path aliases: `@/*` → `src/*`

---

## 📦 Building for Release

### Android (AAB/APK)
```bash
cd android
./gradlew bundleRelease   # Google Play AAB
./gradlew assembleRelease # APK
```
- Signing config in `local.properties` or `keystore.properties`
- R8/ProGuard rules in `proguard-rules.pro`
- SQLCipher native libs bundled per ABI

### Backend (Docker)
```dockerfile
# Dockerfile.example
FROM node:20-alpine
WORKDIR /app
COPY package*.json ./
RUN npm ci --omit=dev
COPY dist ./dist
COPY uploads ./uploads
EXPOSE 3000
CMD ["node", "dist/index.js"]
```
```bash
docker build -t quick-chat-backend .
docker run -p 3000:3000 -v $(pwd)/quickchat.db:/app/quickchat.db quick-chat-backend
```

---

## 🛡 Security Considerations

See **[SECURITY.md](SECURITY.md)** for complete threat model, including:

- **What the server CANNOT see**: Message plaintext, media content, private keys
- **Metadata leakage**: Routing info, traffic patterns, presence
- **Key rotation**: Signed PreKey rotation, OTPK consumption
- **Device compromise**: SQLCipher + Keystore protection
- **Future work**: Sealed Sender, PQXDH (post-quantum), backup encryption

### Verification Checklist
- [ ] Safety number QR comparison on first chat
- [ ] Verify security code matches on both devices
- [ ] Enable biometric app lock (Android Keystore integration)
- [ ] Disable username search in privacy settings if desired
- [ ] Regular signed prekey rotation (app handles automatically)

---

## 🤝 Contributing

1. **Fork** the repository
2. **Create** a feature branch: `git checkout -b feat/amazing-feature`
3. **Commit** changes: `git commit -m 'feat: add amazing feature'`
4. **Push** to branch: `git push origin feat/amazing-feature`
5. **Open** a Pull Request

### Code Style
- **Kotlin**: ktlint + Android Kotlin Style Guide
- **TypeScript**: ESLint + Prettier
- **Commits**: Conventional Commits (`feat:`, `fix:`, `docs:`, `refactor:`)

---

## 📄 License

This project is licensed under the **MIT License** - see [LICENSE](LICENSE) for details.

---

## 🙏 Acknowledgments

| Project | Purpose |
|---------|---------|
| [Signal Protocol](https://signal.org/docs/) | Cryptographic protocol specification |
| [libsignal](https://github.com/signalapp/libsignal) | Reference implementation |
| [Android Keystore](https://developer.android.com/training/articles/keystore) | Hardware-backed key storage |
| [SQLCipher](https://www.zetetic.net/sqlcipher/) | Encrypted SQLite database |
| [Socket.IO](https://socket.io/) | Real-time bidirectional communication |
| [Jetpack Compose](https://developer.android.com/jetpack/compose) | Modern declarative UI toolkit |

---

## 📞 Support & Community

| Channel | Link |
|---------|------|
| **Issues** | [GitHub Issues](https://github.com/your-org/Quick-Chat/issues) |
| **Discussions** | [GitHub Discussions](https://github.com/your-org/Quick-Chat/discussions) |
| **Security** | [SECURITY.md](SECURITY.md) - Report vulnerabilities privately |

---

<div align="center">

**Built with ❤️ for privacy-first communication**

[⬆ Back to Top](#quick-chat)

</div>