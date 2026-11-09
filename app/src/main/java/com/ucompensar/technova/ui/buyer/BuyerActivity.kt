package com.ucompensar.technova.ui.buyer

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.ucompensar.technova.R
import com.ucompensar.technova.databinding.ActivityPanelBinding
import com.ucompensar.technova.ui.common.logout

class BuyerActivity : AppCompatActivity() {
    private lateinit var binding: ActivityPanelBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPanelBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.toolbar.setTitle(R.string.role_buyer)
        binding.toolbar.inflateMenu(R.menu.menu_toolbar_buyer)
        binding.toolbar.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.action_cart -> startActivity(Intent(this, CartActivity::class.java))
                R.id.action_logout -> logout()
            }
            true
        }

        binding.bottomNav.inflateMenu(R.menu.menu_buyer_nav)
        binding.bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_buyer_home -> show(BuyerHomeFragment())
                R.id.nav_buyer_catalog -> show(CatalogFragment.newInstance(null))
                R.id.nav_buyer_categories -> show(BuyerCategoriesFragment())
                R.id.nav_buyer_profile -> show(BuyerProfileFragment())
            }
            true
        }
        if (savedInstanceState == null) show(BuyerHomeFragment())
    }

    fun openCatalog(category: String? = null) {
        binding.bottomNav.menu.findItem(R.id.nav_buyer_catalog).isChecked = true
        show(CatalogFragment.newInstance(category))
    }

    private fun show(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, fragment)
            .commit()
    }
}
