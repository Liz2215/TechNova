package com.ucompensar.technova.data

import kotlinx.coroutines.tasks.await

/** Aquí se guardan las peticiones quejas y reclamos */
object PqrRepository {

    /** Guarda una PQR y devuelve su número de radicado */
    suspend fun create(type: String, message: String): String {
        val user = Session.current
        val radicado = "PQR-" + (System.currentTimeMillis() % 1_000_000).toString().padStart(6, '0')
        Db.store.collection(Db.PQRS).add(
            mapOf(
                "radicado" to radicado,
                "userId" to Db.uid.orEmpty(),
                "name" to user?.name.orEmpty(),
                "email" to user?.email.orEmpty(),
                "type" to type,
                "message" to message,
                "status" to "RECIBIDA",
                "createdAt" to System.currentTimeMillis()
            )
        ).await()
        return radicado
    }
}
