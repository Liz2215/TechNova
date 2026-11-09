package com.ucompensar.technova.ui.common

import android.app.Activity
import android.content.Intent
import com.ucompensar.technova.ui.auth.LoginActivity

fun Activity.logout() {
    val intent = Intent(this, LoginActivity::class.java)
    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
    startActivity(intent)
    finish()
}
