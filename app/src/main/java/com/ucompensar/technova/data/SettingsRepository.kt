package com.ucompensar.technova.data

import kotlinx.coroutines.tasks.await

/** Lectura y guardado de la configuración general de la tienda (documento settings/general). */
object SettingsRepository {
    private val doc get() = Db.store.collection(Db.SETTINGS).document("general")

    /** Lee la configuración; si aún no existe devuelve los valores por defecto. */
    @Suppress("UNCHECKED_CAST")
    suspend fun get(): StoreSettings {
        val snap = doc.get().await()
        if (!snap.exists()) return StoreSettings()
        return StoreSettings(
            shippingCost = snap.getLong("shippingCost") ?: Catalog.DEFAULT_SHIPPING,
            maintenance = snap.getBoolean("maintenance") ?: false,
            notifications = snap.getBoolean("notifications") ?: true,
            newSellers = snap.getBoolean("newSellers") ?: true,
            gateways = snap.get("gateways") as? List<String> ?: listOf("epayco")
        )
    }

    /** Guarda la configuración (solo el administrador tiene permiso en las reglas). */
    suspend fun save(settings: StoreSettings) {
        doc.set(
            mapOf(
                "shippingCost" to settings.shippingCost,
                "maintenance" to settings.maintenance,
                "notifications" to settings.notifications,
                "newSellers" to settings.newSellers,
                "gateways" to settings.gateways
            )
        ).await()
    }
}
