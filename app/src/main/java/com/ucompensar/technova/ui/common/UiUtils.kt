package com.ucompensar.technova.ui.common

import android.content.Context
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import com.ucompensar.technova.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

/** Muestra un mensaje corto en pantalla. */
fun Context.toast(message: String) = Toast.makeText(this, message, Toast.LENGTH_SHORT).show()

/** Muestra un mensaje largo en pantalla. */
fun Context.toastLong(message: String) = Toast.makeText(this, message, Toast.LENGTH_LONG).show()

/** Mensaje legible para un error: usa el texto del error o uno genérico si no tiene. */
fun Context.errorMessage(error: Throwable): String =
    error.message?.takeIf { it.isNotBlank() } ?: getString(R.string.msg_generic_error)

/**
 * Ejecuta una tarea con Firebase en segundo plano ligada al ciclo de vida de la pantalla.
 * Si ocurre un error, lo muestra al usuario en lugar de cerrar la app.
 */
fun LifecycleOwner.launchSafe(context: () -> Context?, block: suspend CoroutineScope.() -> Unit): Job =
    lifecycleScope.launch {
        try {
            block()
        } catch (e: kotlinx.coroutines.CancellationException) {
            throw e
        } catch (e: Exception) {
            context()?.let { it.toastLong(it.errorMessage(e)) }
        }
    }

/** Atajo de [launchSafe] para fragments: usa el viewLifecycleOwner y el contexto del fragment. */
fun Fragment.launchUi(block: suspend CoroutineScope.() -> Unit): Job =
    viewLifecycleOwner.launchSafe({ context }, block)

/** Atajo de [launchSafe] para activities. */
fun androidx.appcompat.app.AppCompatActivity.launchUi(block: suspend CoroutineScope.() -> Unit): Job =
    launchSafe({ this }, block)
