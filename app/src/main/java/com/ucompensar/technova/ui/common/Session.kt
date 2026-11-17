package com.ucompensar.technova.ui.common

import android.app.Activity
import android.content.Intent
import com.ucompensar.technova.data.AuthRepository
import com.ucompensar.technova.data.Role
import com.ucompensar.technova.ui.admin.AdminActivity
import com.ucompensar.technova.ui.auth.LoginActivity
import com.ucompensar.technova.ui.buyer.BuyerActivity
import com.ucompensar.technova.ui.seller.SellerActivity

/** Cierra la sesión y vuelve al login */
fun Activity.logout() {
    AuthRepository.logout()
    val intent = Intent(this, LoginActivity::class.java)
    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
    startActivity(intent)
    finish()
}

/** Abre el panel que le toca al usuario según su rol */
fun Activity.openPanelFor(role: Role) {
    val destination = when (role) {
        Role.SELLER -> SellerActivity::class.java
        Role.ADMIN -> AdminActivity::class.java
        Role.BUYER -> BuyerActivity::class.java
    }
    val intent = Intent(this, destination)
    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
    startActivity(intent)
    finish()
}
