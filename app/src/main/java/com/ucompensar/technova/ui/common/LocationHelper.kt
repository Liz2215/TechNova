package com.ucompensar.technova.ui.common

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Geocoder
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.util.Locale

/**
 * Geolocalización con las APIs de Google Play Services: obtiene la ubicación actual
 * del celular (Fused Location Provider) y la convierte en dirección y ciudad con Geocoder.
 */
object LocationHelper {

    /** Permisos de ubicación que se piden al usuario. */
    val PERMISSIONS = arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)

    /** Resultado de la geolocalización: coordenadas y dirección aproximada. */
    data class Place(val latitude: Double, val longitude: Double, val address: String, val city: String)

    /** Indica si el usuario ya dio permiso de ubicación. */
    fun hasPermission(context: Context): Boolean = PERMISSIONS.any {
        ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
    }

    /**
     * Obtiene la ubicación actual y busca la dirección correspondiente.
     * Lanza un error con mensaje claro si el GPS está apagado o no hay señal.
     */
    @SuppressLint("MissingPermission")
    suspend fun currentPlace(context: Context): Place {
        val client = LocationServices.getFusedLocationProviderClient(context)
        val location = client.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null).await()
            ?: client.lastLocation.await()
            ?: throw IllegalStateException("No se pudo obtener la ubicación. Activa el GPS e inténtalo de nuevo.")
        val (address, city) = withContext(Dispatchers.IO) { reverseGeocode(context, location.latitude, location.longitude) }
        return Place(location.latitude, location.longitude, address, city)
    }

    /** Convierte coordenadas en dirección y ciudad. Si no hay servicio, devuelve las coordenadas como texto. */
    @Suppress("DEPRECATION")
    private fun reverseGeocode(context: Context, lat: Double, lng: Double): Pair<String, String> {
        val fallback = String.format(Locale.US, "%.5f, %.5f", lat, lng) to ""
        if (!Geocoder.isPresent()) return fallback
        return runCatching {
            val result = Geocoder(context, Locale("es", "CO")).getFromLocation(lat, lng, 1)?.firstOrNull()
            if (result == null) fallback
            else (result.thoroughfare?.let { "$it ${result.subThoroughfare.orEmpty()}".trim() }
                ?: result.getAddressLine(0).orEmpty()) to (result.locality ?: result.subAdminArea.orEmpty())
        }.getOrDefault(fallback)
    }

    /** Enlace de Google Maps para ver unas coordenadas. */
    fun mapsUrl(lat: Double, lng: Double): String =
        String.format(Locale.US, "https://www.google.com/maps/search/?api=1&query=%.6f,%.6f", lat, lng)
}
