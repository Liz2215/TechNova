package com.ucompensar.technova.ui.seller

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.ucompensar.technova.R
import com.ucompensar.technova.databinding.ActivityPanelBinding
import com.ucompensar.technova.ui.common.logout

class SellerActivity : AppCompatActivity() {
    private lateinit var binding: ActivityPanelBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPanelBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.toolbar.setTitle(R.string.seller_title)
        binding.toolbar.inflateMenu(R.menu.menu_toolbar_main)
        binding.toolbar.setOnMenuItemClickListener {
            if (it.itemId == R.id.action_logout) logout()
            true
        }

        binding.bottomNav.inflateMenu(R.menu.menu_seller_nav)
        binding.bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_seller_home -> show(SellerDashboardFragment())
                R.id.nav_seller_products -> show(SellerProductsFragment())
                R.id.nav_seller_orders -> show(SellerOrdersFragment())
                R.id.nav_seller_profile -> show(SellerProfileFragment())
            }
            true
        }
        if (savedInstanceState == null) show(SellerDashboardFragment())
    }

    private fun show(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, fragment)
            .commit()
    }
}
