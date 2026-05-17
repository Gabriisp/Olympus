package com.example.olympus

// Activity para seleccionar y agregar un ejercicio a una rutina
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class SeleccionarEjercicioActivity : AppCompatActivity() {

    private val firebaseRepository = FirebaseRepository.instance
    private lateinit var recyclerEjercicios: RecyclerView
    private lateinit var searchView: SearchView
    private lateinit var adapter: EjerciciosListAdapter
    private var cloudRoutineId: String = ""
    private var ejerciciosList: List<Ejercicio> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_seleccionar_ejercicio)

        cloudRoutineId = intent.getStringExtra("CLOUD_ROUTINE_ID").orEmpty()

        recyclerEjercicios = findViewById(R.id.recyclerEjercicios)
        searchView = findViewById(R.id.searchViewEjercicios)
        
        recyclerEjercicios.layoutManager = LinearLayoutManager(this)

        ejerciciosList = EjerciciosData.getEjercicios()

        adapter = EjerciciosListAdapter(ejerciciosList) { ejercicio ->
            if (cloudRoutineId.isBlank()) {
                showToast("No se encontró la rutina en la nube", Toast.LENGTH_LONG)
            } else {
                firebaseRepository.addExerciseToRoutine(cloudRoutineId, ejercicio.id, ejercicio.nombre) { result ->
                    runOnUiThread {
                        result.onSuccess {
                            showToast("${ejercicio.nombre} agregado")
                            finish()
                        }.onFailure { error ->
                            showToast(error.message ?: "No se pudo agregar el ejercicio", Toast.LENGTH_LONG)
                        }
                    }
                }
            }
        }

        recyclerEjercicios.adapter = adapter

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean = false

            override fun onQueryTextChange(newText: String?): Boolean {
                val filtered = if (newText.isNullOrEmpty()) {
                    ejerciciosList
                } else {
                    ejerciciosList.filter {
                        it.nombre.contains(newText, ignoreCase = true)
                    }
                }
                adapter.updateList(filtered)
                return true
            }
        })
    }
}
