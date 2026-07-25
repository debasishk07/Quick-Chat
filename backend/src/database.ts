import sqlite3 from 'sqlite3';
import path from 'path';

const dbPath = path.resolve(__dirname, '../quickchat.db');
const db = new sqlite3.Database(dbPath);

export function initializeDatabase(): Promise<void> {
  return new Promise((resolve, reject) => {
    db.serialize(() => {
      // Users table
      db.run(`
        CREATE TABLE IF NOT EXISTS users (
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
        )
      `, () => {
        // Run migration checks to add columns if users table existed from before
        const columns = [
          { name: 'phoneNumber', type: 'TEXT' },
          { name: 'email', type: 'TEXT' },
          { name: 'username', type: 'TEXT UNIQUE' },
          { name: 'usernameLastChanged', type: 'INTEGER DEFAULT 0' },
          { name: 'authProviders', type: "TEXT NOT NULL DEFAULT 'phone'" },
          { name: 'usernameSearchEnabled', type: 'INTEGER DEFAULT 1' }
        ];
        for (const col of columns) {
          db.run(`ALTER TABLE users ADD COLUMN ${col.name} ${col.type}`, (err) => {
            // Ignore error if column already exists
          });
        }
      });

      // Prekeys table for E2EE
      db.run(`
        CREATE TABLE IF NOT EXISTS prekeys (
          phone TEXT PRIMARY KEY,
          identityKey TEXT NOT NULL,
          signedPreKey TEXT NOT NULL,
          signedPreKeySignature TEXT NOT NULL,
          oneTimePreKeys TEXT NOT NULL
        )
      `);

      // Messages table for offline queue / message logs
      db.run(`
        CREATE TABLE IF NOT EXISTS messages (
          id TEXT PRIMARY KEY,
          sender TEXT NOT NULL,
          recipient TEXT NOT NULL,
          isGroup INTEGER NOT NULL,
          ciphertext TEXT NOT NULL,
          iv TEXT NOT NULL,
          ephemeralPublicKey TEXT,
          messageType TEXT NOT NULL,
          timestamp INTEGER NOT NULL,
          status TEXT NOT NULL,
          isDeleted INTEGER DEFAULT 0,
          deletedFor TEXT DEFAULT '[]'
        )
      `, () => {
        db.run(`ALTER TABLE messages ADD COLUMN isDeleted INTEGER DEFAULT 0`, () => {});
        db.run(`ALTER TABLE messages ADD COLUMN deletedFor TEXT DEFAULT '[]'`, () => {});
      });

      // Status/Stories table
      db.run(`
        CREATE TABLE IF NOT EXISTS status (
          id TEXT PRIMARY KEY,
          sender TEXT NOT NULL,
          mediaUrl TEXT NOT NULL,
          caption TEXT,
          mediaType TEXT NOT NULL,
          timestamp INTEGER NOT NULL,
          expiresAt INTEGER NOT NULL
        )
      `);

      // Status Views table
      db.run(`
        CREATE TABLE IF NOT EXISTS status_views (
          statusId TEXT NOT NULL,
          viewer TEXT NOT NULL,
          timestamp INTEGER NOT NULL,
          PRIMARY KEY (statusId, viewer)
        )
      `);

      // Blocked Contacts table
      db.run(`
        CREATE TABLE IF NOT EXISTS blocked_contacts (
          blocker TEXT NOT NULL,
          blocked TEXT NOT NULL,
          PRIMARY KEY (blocker, blocked)
        )
      `);

      // Moderation Reports table
      db.run(`
        CREATE TABLE IF NOT EXISTS moderation_reports (
          id TEXT PRIMARY KEY,
          reporter TEXT NOT NULL,
          reported TEXT NOT NULL,
          reason TEXT NOT NULL,
          description TEXT,
          messagesJson TEXT,
          timestamp INTEGER NOT NULL
        )
      `);

      // Pending Events table for offline event delivery (e.g. deletions)
      db.run(`
        CREATE TABLE IF NOT EXISTS pending_events (
          id INTEGER PRIMARY KEY AUTOINCREMENT,
          recipient TEXT NOT NULL,
          event TEXT NOT NULL,
          payloadText TEXT NOT NULL,
          timestamp INTEGER NOT NULL
        )
      `, (err) => {
        if (err) reject(err);
        else resolve();
      });
    });
  });
}

// Database helper operations
export const dbOperations = {
  run(sql: string, params: any[] = []): Promise<void> {
    return new Promise((resolve, reject) => {
      db.run(sql, params, function (err) {
        if (err) reject(err);
        else resolve();
      });
    });
  },

  get<T>(sql: string, params: any[] = []): Promise<T | null> {
    return new Promise((resolve, reject) => {
      db.get(sql, params, (err, row) => {
        if (err) reject(err);
        else resolve((row as T) || null);
      });
    });
  },

  all<T>(sql: string, params: any[] = []): Promise<T[]> {
    return new Promise((resolve, reject) => {
      db.all(sql, params, (err, rows) => {
        if (err) reject(err);
        else resolve((rows as T[]) || []);
      });
    });
  }
};
