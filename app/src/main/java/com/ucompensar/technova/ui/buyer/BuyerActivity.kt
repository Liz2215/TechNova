package com.ucompensar.technova.ui.buyer

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.ucompensar.technova.R
import com.ucompensar.technova.data.CartRepository
import com.ucompensar.technova.databinding.ActivityPanelBinding
import com.ucompensar.technova.ui.common.launchUi
import com.ucompensar.technova.ui.common.logout

/**
 * Panel del comprador con navegación inferior (Inicio, Catálogo, Categorías y Perfil)
 * y accesos al carrito y al cierre de sesión en la barra superior.
 */
class BuyerActivity : AppCompatActivity() {
    private lateinit var binding: ActivityPanelBinding

    /** Arma la barra superior, la navegación inferior y carga el carrito guardado. */
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
        launchUi { CartRepository.load() }
    }

    /** Abre el catálogo, opcionalmente filtrado por una categoría. */
    fun openCatalog(category: String? = null) {
        binding.bottomNav.menu.findItem(R.id.nav_buyer_catalog).isChecked = true
        show(CatalogFragment.newInstance(category))
    }

    /** Reemplaza el contenido del panel por el fragment indicado. */
    private fun show(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, fragment)
            .commit()
    }
}
