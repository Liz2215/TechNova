package com.ucompensar.technova.data

/**
 * Guarda en memoria el perfil del usuario que inició sesión (nombre, correo y rol)
 * para no tener que leerlo de Firestore en cada pantalla.
 */
object Session {
    var current: User? = null

    /** Rol del usuario actual; si no hay sesión se asume Comprador. */
    val role: Role get() = current?.role ?: Role.BUYER

    /** Indica si el usuario actual es administrador. */
    val isAdmin: Boolean get() = role == Role.ADMIN
}
