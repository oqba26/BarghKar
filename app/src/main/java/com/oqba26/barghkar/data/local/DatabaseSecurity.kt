package com.oqba26.barghkar.data.local

import android.content.Context
import androidx.core.content.edit
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import java.util.UUID

object DatabaseSecurity {
    private const val PREF_FILE = "db_security_prefs"
    private const val KEY_PASSPHRASE = "db_passphrase"

    fun getPassphrase(context: Context): ByteArray {
        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()

        val prefs = EncryptedSharedPreferences.create(
            context,
            PREF_FILE,
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )

        var passphrase = prefs.getString(KEY_PASSPHRASE, null)
        if (passphrase == null) {
            passphrase = UUID.randomUUID().toString() + UUID.randomUUID().toString()
            prefs.edit {
                putString(KEY_PASSPHRASE, passphrase)
            }
        }
        return passphrase.toByteArray()
    }
}
