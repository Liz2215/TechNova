package com.ucompensar.technova.ui.admin

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.ucompensar.technova.R
import com.ucompensar.technova.databinding.ActivityPanelBinding
import com.ucompensar.technova.ui.common.logout

/** Panel del rol Administrador: Métricas, Usuarios y Configuración. */
class AdminActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPanelBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPanelBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.toolbar.setTitle(R.string.admin_title)
        binding.toolbar.inflateMenu(R.menu.menu_toolbar_main)
        binding.toolbar.setOnMenuItemClickListener {
            if (it.itemId == R.id.action_logout) logout()
            true
        }

        binding.bottomNav.inflateMenu(R.menu.menu_admin_nav)
        binding.bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_admin_metrics -> show(AdminMetricsFragment())
                R.id.nav_admin_users -> show(AdminUsersFragment())
                R.id.nav_admin_settings -> show(AdminSettingsFragment())
            }
            true
        }
        if (savedInstanceState == null) show(AdminMetricsFragment())
    }

    private fun show(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, fragment)
            .commit()
    }
}
