package com.ucompensar.technova.data

import kotlinx.coroutines.tasks.await

/**
 * CRUD de usuarios en la colección "users" de Firestore. Lo usa el administrador
 * para listar, crear, editar y eliminar cuentas, y cada usuario para su propio perfil.
 */
object UserRepository {
    private val users get() = Db.store.collection(Db.USERS)

    /** Devuelve todos los usuarios ordenados por nombre. */
    suspend fun getAll(): List<User> =
        users.get().await().documents.map { Db.toUser(it) }.sortedBy { it.name.lowercase() }

    /** Busca un usuario por su uid; devuelve null si no existe. */
    suspend fun get(id: String): User? {
        val doc = users.document(id).get().await()
        return if (doc.exists()) Db.toUser(doc) else null
    }

    /** Actualiza los datos de un usuario existente. */
    suspend fun update(user: User) {
        users.document(user.id).set(Db.fromUser(user)).await()
        if (user.id == Db.uid) Session.current = user
    }

    /**
     * Crea un usuario nuevo desde el panel del administrador: primero la cuenta en
     * Firebase Authentication y luego su perfil en Firestore con el rol elegido.
     */
    suspend fun createByAdmin(user: User, password: String): User {
        val uid = AuthRepository.createAccountAsAdmin(user.email, password)
        val created = user.copy(id = uid)
        users.document(uid).set(Db.fromUser(created) + ("createdAt" to System.currentTimeMillis())).await()
        return created
    }

    /**
     * Elimina el perfil del usuario en Firestore. Sin perfil, la cuenta ya no puede
     * entrar a la app (el inicio de sesión valida que el perfil exista).
     */
    suspend fun delete(id: String) {
        users.document(id).delete().await()
    }

    /** Guarda la dirección de entrega (con coordenadas GPS si se capturaron) del usuario actual. */
    suspend fun saveAddress(address: String, city: String, latitude: Double, longitude: Double) {
        val uid = Db.uid ?: return
        users.document(uid).update(
            mapOf("address" to address, "city" to city, "latitude" to latitude, "longitude" to longitude)
        ).await()
        Session.current = Session.current?.copy(address = address, city = city, latitude = latitude, longitude = longitude)
    }
}
