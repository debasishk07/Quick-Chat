package com.quickchat.core.database.di

import android.content.Context
import android.content.SharedPreferences
import androidx.room.Room
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
            .fallbackToDestructiveMigration() // Simple for local development schema updates
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
}
