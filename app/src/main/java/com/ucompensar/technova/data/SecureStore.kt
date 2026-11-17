package com.ucompensar.technova.data

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

/** Guarda el correo y la contraseña cifrados para poder entrar con la huella */
object SecureStore {
    private const val FILE = "technova_secure"
    private const val KEY_EMAIL = "email"
    private const val KEY_PASSWORD = "password"

    /** Abre el archivo cifrado */
    private fun prefs(context: Context): SharedPreferences {
        val masterKey = MasterKey.Builder(context).setKeyScheme(MasterKey.KeyScheme.AES256_GCM).build()
        return EncryptedSharedPreferences.create(
            context, FILE, masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    /** Guarda el correo y la contraseña */
    fun saveCredentials(context: Context, email: String, password: String) {
        runCatching { prefs(context).edit().putString(KEY_EMAIL, email).putString(KEY_PASSWORD, password).apply() }
    }

    /** Trae el correo y la contraseña guardados */
    fun credentials(context: Context): Pair<String, String>? = runCatching {
        val p = prefs(context)
        val email = p.getString(KEY_EMAIL, null)
        val password = p.getString(KEY_PASSWORD, null)
        if (email != null && password != null) email to password else null
    }.getOrNull()

    /** Borra los datos guardados */
    fun clear(context: Context) {
        runCatching { prefs(context).edit().clear().apply() }
    }
}
