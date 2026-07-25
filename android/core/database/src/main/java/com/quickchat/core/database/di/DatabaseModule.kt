package com.quickchat.core.database.di

import android.content.Context
import android.content.SharedPreferences
import androidx.room.Room
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.quickchat.core.crypto.KeystoreManager
import com.quickchat.core.database.AppDatabase
import com.quickchat.core.database.dao.*
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import net.zetetic.database.sqlcipher.SupportOpenHelperFactory
import java.security.SecureRandom
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    private const val PREFS_NAME = "quickchat_secure_prefs"
    private const val KEY_SEALED_PASSPHRASE = "sealed_db_passphrase"

    @Provides
    @Singleton
    fun provideSecureSharedPreferences(@ApplicationContext context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    @Provides
    @Singleton
    fun provideDatabasePassphrase(sharedPrefs: SharedPreferences): ByteArray {
        val sealedPassphrase = sharedPrefs.getString(KEY_SEALED_PASSPHRASE, null)
        return if (sealedPassphrase == null) {
            // Generate a random secure passphrase
            val randomBytes = ByteArray(32)
            SecureRandom().nextBytes(randomBytes)
            val newPassphrase = android.util.Base64.encodeToString(randomBytes, android.util.Base64.NO_WRAP)
            
            // Seal using Android Keystore and store in preferences
            val encrypted = KeystoreManager.encryptPassphrase(newPassphrase)
            sharedPrefs.edit().putString(KEY_SEALED_PASSPHRASE, encrypted).apply()
            
            newPassphrase.toByteArray(Charsets.UTF_8)
        } else {
            // Unseal using Android Keystore
            val decrypted = KeystoreManager.decryptPassphrase(sealedPassphrase)
            decrypted.toByteArray(Charsets.UTF_8)
        }
    }

    val MIGRATION_1_2 = object : Migration(1, 2) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("""
                CREATE TABLE IF NOT EXISTS `users` (
                    `phone` TEXT NOT NULL, 
                    `phoneNumber` TEXT, 
                    `email` TEXT, 
                    `username` TEXT, 
                    `displayName` TEXT NOT NULL, 
                    `avatarUrl` TEXT, 
                    `about` TEXT, 
                    `lastSeen` INTEGER NOT NULL, 
                    `isOnline` INTEGER NOT NULL, 
                    PRIMARY KEY(`phone`)
                )
            """)
            db.execSQL("""
                CREATE TABLE IF NOT EXISTS `phone_contacts` (
                    `phone` TEXT NOT NULL, 
                    `contactName` TEXT NOT NULL, 
                    PRIMARY KEY(`phone`)
                )
            """)
        }
    }

    val MIGRATION_2_3 = object : Migration(2, 3) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("""
                CREATE TABLE IF NOT EXISTS `blocked_contacts` (
                    `phone` TEXT NOT NULL, 
                    `displayName` TEXT NOT NULL, 
                    PRIMARY KEY(`phone`)
                )
            """)
            db.execSQL("ALTER TABLE `chats` ADD COLUMN `disappearingDuration` INTEGER NOT NULL DEFAULT 0")
            db.execSQL("ALTER TABLE `messages` ADD COLUMN `isStarred` INTEGER NOT NULL DEFAULT 0")
            db.execSQL("ALTER TABLE `messages` ADD COLUMN `expireAt` INTEGER DEFAULT NULL")
        }
    }

    val MIGRATION_3_4 = object : Migration(3, 4) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("""
                CREATE VIRTUAL TABLE IF NOT EXISTS `messages_fts` USING FTS4(`messageId`, `chatPhone`, `plainText`)
            """)
            db.execSQL("""
                INSERT INTO `messages_fts` (`messageId`, `chatPhone`, `plainText`)
                SELECT `id`, 
                       CASE 
                           WHEN `senderPhone` IN (SELECT `recipientPhone` FROM `chats` UNION SELECT `recipientPhone` FROM `crypto_sessions`) THEN `senderPhone` 
                           ELSE `recipientPhone` 
                       END, 
                       `plainText` 
                FROM `messages` 
                WHERE `plainText` IS NOT NULL
            """)
        }
    }

    val MIGRATION_4_5 = object : Migration(4, 5) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("ALTER TABLE `messages` ADD COLUMN `reaction` TEXT DEFAULT NULL")
            db.execSQL("ALTER TABLE `messages` ADD COLUMN `playbackSpeed` REAL NOT NULL DEFAULT 1.0")
            db.execSQL("ALTER TABLE `messages` ADD COLUMN `isEdited` INTEGER NOT NULL DEFAULT 0")
            db.execSQL("ALTER TABLE `messages` ADD COLUMN `pinnedAt` INTEGER DEFAULT NULL")
            db.execSQL("""
                CREATE TABLE IF NOT EXISTS `scheduled_messages` (
                    `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, 
                    `recipientPhone` TEXT NOT NULL, 
                    `plainText` TEXT NOT NULL, 
                    `messageType` TEXT NOT NULL, 
                    `scheduledTime` INTEGER NOT NULL
                )
            """)
        }
    }

    val MIGRATION_5_6 = object : Migration(5, 6) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("ALTER TABLE `messages` ADD COLUMN `isDeleted` INTEGER NOT NULL DEFAULT 0")
        }
    }

    @Provides
    @Singleton
    fun provideAppDatabase(
        @ApplicationContext context: Context,
        passphraseBytes: ByteArray
    ): AppDatabase {
        // Initialize SQLCipher library
        System.loadLibrary("sqlcipher")
        
        val factory = SupportOpenHelperFactory(passphraseBytes)
        
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "quickchat_encrypted.db"
        )
            .openHelperFactory(factory)
            .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5, MIGRATION_5_6)
            .build()
    }

    @Provides
    fun provideChatDao(db: AppDatabase): ChatDao = db.chatDao()

    @Provides
    fun provideMessageDao(db: AppDatabase): MessageDao = db.messageDao()

    @Provides
    fun provideStatusDao(db: AppDatabase): StatusDao = db.statusDao()

    @Provides
    fun provideSessionDao(db: AppDatabase): SessionDao = db.sessionDao()

    @Provides
    fun provideOutboxDao(db: AppDatabase): OutboxDao = db.outboxDao()

    @Provides
    fun provideUserDao(db: AppDatabase): UserDao = db.userDao()

    @Provides
    fun providePhoneContactDao(db: AppDatabase): PhoneContactDao = db.phoneContactDao()

    @Provides
    fun provideBlockedContactDao(db: AppDatabase): BlockedContactDao = db.blockedContactDao()

    @Provides
    fun provideScheduledMessageDao(db: AppDatabase): ScheduledMessageDao = db.scheduledMessageDao()
}
