"use strict";
var __importDefault = (this && this.__importDefault) || function (mod) {
    return (mod && mod.__esModule) ? mod : { "default": mod };
};
Object.defineProperty(exports, "__esModule", { value: true });
exports.dbOperations = void 0;
exports.initializeDatabase = initializeDatabase;
const sqlite3_1 = __importDefault(require("sqlite3"));
const path_1 = __importDefault(require("path"));
const dbPath = path_1.default.resolve(__dirname, '../quickchat.db');
const db = new sqlite3_1.default.Database(dbPath);
function initializeDatabase() {
    return new Promise((resolve, reject) => {
        db.serialize(() => {
            // Users table
            db.run(`
        CREATE TABLE IF NOT EXISTS users (
          phone TEXT PRIMARY KEY,
          displayName TEXT NOT NULL,
          avatarUrl TEXT,
          about TEXT,
          lastSeen INTEGER DEFAULT 0,
          isOnline INTEGER DEFAULT 0
        )
      `);
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
          status TEXT NOT NULL
        )
      `);
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
      `, (err) => {
                if (err)
                    reject(err);
                else
                    resolve();
            });
        });
    });
}
// Database helper operations
exports.dbOperations = {
    run(sql, params = []) {
        return new Promise((resolve, reject) => {
            db.run(sql, params, function (err) {
                if (err)
                    reject(err);
                else
                    resolve();
            });
        });
    },
    get(sql, params = []) {
        return new Promise((resolve, reject) => {
            db.get(sql, params, (err, row) => {
                if (err)
                    reject(err);
                else
                    resolve(row || null);
            });
        });
    },
    all(sql, params = []) {
        return new Promise((resolve, reject) => {
            db.all(sql, params, (err, rows) => {
                if (err)
                    reject(err);
                else
                    resolve(rows || []);
            });
        });
    }
};
