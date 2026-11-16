package com.ucompensar.technova.data

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

/**
 * Guarda cifradas (con una llave del Android Keystore) las credenciales del último
 * usuario que entró con correo y contraseña, para poder iniciar sesión con la huella.
 */
object SecureStore {
    private const val FILE = "technova_secure"
    private const val KEY_EMAIL = "email"
    private const val KEY_PASSWORD = "password"

    /** Abre el archivo de preferencias cifradas. */
    private fun prefs(context: Context): SharedPreferences {
        val masterKey = MasterKey.Builder(context).setKeyScheme(MasterKey.KeyScheme.AES256_GCM).build()
        return EncryptedSharedPreferences.create(
            context, FILE, masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    /** Guarda el correo y la contraseña después de un inicio de sesión exitoso. */
    fun saveCredentials(context: Context, email: String, password: String) {
        runCatching { prefs(context).edit().putString(KEY_EMAIL, email).putString(KEY_PASSWORD, password).apply() }
    }

    /** Devuelve las credenciales guardadas o null si nunca se ha iniciado sesión con correo. */
    fun credentials(context: Context): Pair<String, String>? = runCatching {
        val p = prefs(context)
        val email = p.getString(KEY_EMAIL, null)
        val password = p.getString(KEY_PASSWORD, null)
        if (email != null && password != null) email to password else null
    }.getOrNull()

    /** Borra las credenciales (por ejemplo, al eliminar la cuenta). */
    fun clear(context: Context) {
        runCatching { prefs(context).edit().clear().apply() }
    }
}
