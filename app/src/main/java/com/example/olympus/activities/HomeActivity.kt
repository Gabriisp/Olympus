package com.example.olympus

// Pantalla principal para usuarios normales
// Contiene tabs para Rutinas, Nutricion, Notas y Stats
// Tambien permite acceder a la Tienda y ver profesionales disponibles

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.Button
import android.widget.FrameLayout
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.example.olympus.utils.SessionManager

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

        findViewById<Button>(R.id.btnContactos).setOnClickListener {
            mostrarDialogoContactos()
        }

        selectTab(tabRutinas)
        loadFragment(RutinasFragment())
    }

    // Resetea el estilo visual de todos los tabs y aplica el estilo activo al seleccionado
    private fun selectTab(selectedTab: TextView) {
        val allTabs = listOf(tabRutinas, tabNutricion, tabNotas, tabStats)
        for (tab in allTabs) {
            tab.setTextColor(getColor(R.color.gym_text_gray))
            tab.setBackgroundColor(android.graphics.Color.TRANSPARENT)
        }
        selectedTab.setTextColor(getColor(R.color.black))
        selectedTab.setBackgroundResource(R.drawable.bg_tab_selected_white)
    }

    // Carga un fragmento en el contenedor principal
    private fun loadFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, fragment)
            .commit()
    }

    // Muestra un dialogo con opciones de contacto (email e instagram)
    private fun mostrarDialogoContactos() {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_contactos, null)

        AlertDialog.Builder(this)
            .setView(dialogView)
            .create()
            .show()

        dialogView.findViewById<TextView>(R.id.tvEmail1).setOnClickListener {
            val intent = Intent(Intent.ACTION_SENDTO).apply {
                data = android.net.Uri.parse("mailto:jaime.de.luengo.al@iespoligonosur.org")
            }
            startActivity(intent)
        }

        dialogView.findViewById<TextView>(R.id.tvEmail2).setOnClickListener {
            val intent = Intent(Intent.ACTION_SENDTO).apply {
                data = android.net.Uri.parse("mailto:gabriel.santos.palomino.al@iespoligonosur.org")
            }
            startActivity(intent)
        }

        dialogView.findViewById<TextView>(R.id.tvInstagramUser1).setOnClickListener {
            val intent = Intent(Intent.ACTION_VIEW).apply {
                data = android.net.Uri.parse("https://instagram.com/jaimeeesantoss")
            }
            startActivity(intent)
        }

        dialogView.findViewById<TextView>(R.id.tvInstagramUser2).setOnClickListener {
            val intent = Intent(Intent.ACTION_VIEW).apply {
                data = android.net.Uri.parse("https://instagram.com/gabriel_angelsp")
            }
            startActivity(intent)
        }
    }
}
