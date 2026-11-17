package com.ucompensar.technova.data

import kotlinx.coroutines.tasks.await

/** Aquí se manejan los usuarios que se guardan en Firestore */
object UserRepository {
    private val users get() = Db.store.collection(Db.USERS)

    /** Trae todos los usuarios */
    suspend fun getAll(): List<User> =
        users.get().await().documents.map { Db.toUser(it) }.sortedBy { it.name.lowercase() }

    /** Busca un usuario por su id */
    suspend fun get(id: String): User? {
        val doc = users.document(id).get().await()
        return if (doc.exists()) Db.toUser(doc) else null
    }

    /** Guarda los cambios de un usuario */
    suspend fun update(user: User) {
        users.document(user.id).set(Db.fromUser(user)).await()
        if (user.id == Db.uid) Session.current = user
    }

    /** Esta función es para la creación de usuarios desde el panel del administrador */
    suspend fun createByAdmin(user: User, password: String): User {
        val uid = AuthRepository.createAccountAsAdmin(user.email, password)
        val created = user.copy(id = uid)
        users.document(uid).set(Db.fromUser(created) + ("createdAt" to System.currentTimeMillis())).await()
        return created
    }

    /** Borra el perfil de un usuario */
    suspend fun delete(id: String) {
        users.document(id).delete().await()
    }

    /** Guarda la dirección de entrega del usuario */
    suspend fun saveAddress(address: String, city: String, latitude: Double, longitude: Double) {
        val uid = Db.uid ?: return
        users.document(uid).update(
            mapOf("address" to address, "city" to city, "latitude" to latitude, "longitude" to longitude)
        ).await()
        Session.current = Session.current?.copy(address = address, city = city, latitude = latitude, longitude = longitude)
    }
}
