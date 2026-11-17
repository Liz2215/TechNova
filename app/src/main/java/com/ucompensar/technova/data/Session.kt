package com.ucompensar.technova.data

/** Guarda en memoria los datos del usuario que inició sesión */
object Session {
    var current: User? = null

    /** Rol del usuario que inició sesión */
    val role: Role get() = current?.role ?: Role.BUYER

    /** Dice si el usuario es administrador */
    val isAdmin: Boolean get() = role == Role.ADMIN
}
