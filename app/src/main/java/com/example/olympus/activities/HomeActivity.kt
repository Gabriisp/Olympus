package com.example.olympus

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.FrameLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.android.material.floatingactionbutton.FloatingActionButton

class HomeActivity : AppCompatActivity() {

    private val firebaseRepository = FirebaseRepository.instance
    private lateinit var sessionManager: SessionManager
    private lateinit var tabRutinas: TextView
    private lateinit var tabNutricion: TextView
    private lateinit var tabNotas: TextView
    private lateinit var tabStats: TextView
    private lateinit var btnTienda: Button
    private lateinit var fabAvailableProfessionals: FloatingActionButton
    private lateinit var fragmentContainer: FrameLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        sessionManager = SessionManager(this)

        if (sessionManager.getUserRole() != "Usuario") {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }

        setContentView(R.layout.activity_home)

        tabRutinas = findViewById(R.id.tabRutinas)
        tabNutricion = findViewById(R.id.tabNutricion)
        tabNotas = findViewById(R.id.tabNotas)
        tabStats = findViewById(R.id.tabStats)
        btnTienda = findViewById(R.id.btnTienda)
        fabAvailableProfessionals = findViewById(R.id.fabAvailableProfessionals)
        fragmentContainer = findViewById(R.id.fragmentContainer)

        findViewById<TextView>(R.id.tvWelcomeUser)?.text = "Bienvenido, ${sessionManager.getUserName()}"

        findViewById<Button>(R.id.btnLogoutUser).setOnClickListener {
            firebaseRepository.signOut()
            sessionManager.logout()
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }

        tabRutinas.setOnClickListener {
            selectTab(tabRutinas)
            loadFragment(RutinasFragment())
        }

        tabNutricion.setOnClickListener {
            selectTab(tabNutricion)
            loadFragment(NutricionFragment())
        }

        tabNotas.setOnClickListener {
            selectTab(tabNotas)
            loadFragment(NotasFragment())
        }

        tabStats.setOnClickListener {
            selectTab(tabStats)
            loadFragment(StatsFragment())
        }

        btnTienda.setOnClickListener {
            startActivity(Intent(this, TiendaActivity::class.java))
        }

        fabAvailableProfessionals.setOnClickListener {
            startActivity(Intent(this, AvailableProfessionalsActivity::class.java))
        }

        selectTab(tabRutinas)
        loadFragment(RutinasFragment())
    }

    private fun selectTab(selectedTab: TextView) {
        val allTabs = listOf(tabRutinas, tabNutricion, tabNotas, tabStats)
        for (tab in allTabs) {
            tab.setTextColor(getColor(R.color.gym_text_gray))
            tab.setBackgroundColor(android.graphics.Color.TRANSPARENT)
        }
        selectedTab.setTextColor(getColor(R.color.black))
        selectedTab.setBackgroundColor(getColor(R.color.gym_surface_white))
    }

    private fun loadFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, fragment)
            .commit()
    }
}
