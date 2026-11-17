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

/** Muestra un mensaje corto */
fun Context.toast(message: String) = Toast.makeText(this, message, Toast.LENGTH_SHORT).show()

/** Muestra un mensaje largo */
fun Context.toastLong(message: String) = Toast.makeText(this, message, Toast.LENGTH_LONG).show()

/** Saca el texto del error para mostrarlo */
fun Context.errorMessage(error: Throwable): String =
    error.message?.takeIf { it.isNotBlank() } ?: getString(R.string.msg_generic_error)

/** Hace una tarea con Firebase en segundo plano y si falla muestra el error */
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

/** Lo mismo de arriba pero para los fragments */
fun Fragment.launchUi(block: suspend CoroutineScope.() -> Unit): Job =
    viewLifecycleOwner.launchSafe({ context }, block)

/** Lo mismo de arriba pero para las activities */
fun androidx.appcompat.app.AppCompatActivity.launchUi(block: suspend CoroutineScope.() -> Unit): Job =
    launchSafe({ this }, block)
