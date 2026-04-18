package com.example.olympus

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class ReemplazarEjercicioActivity : AppCompatActivity() {

    private val firebaseRepository = FirebaseRepository.instance
    private lateinit var recyclerEjercicios: RecyclerView
    private lateinit var searchView: SearchView
    private lateinit var adapter: EjerciciosListAdapter
    private var cloudRoutineId: String = ""
    private var cloudRoutineExerciseId: String = ""
    private var ejerciciosList: List<Ejercicio> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reemplazar_ejercicio)

        cloudRoutineId = intent.getStringExtra("CLOUD_ROUTINE_ID").orEmpty()
        cloudRoutineExerciseId = intent.getStringExtra("CLOUD_ROUTINE_EJERCICIO_ID").orEmpty()

        recyclerEjercicios = findViewById(R.id.recyclerEjerciciosReemplazar)
        searchView = findViewById(R.id.searchViewReemplazar)
        
        recyclerEjercicios.layoutManager = LinearLayoutManager(this)

        ejerciciosList = EjerciciosData.getEjercicios()

        adapter = EjerciciosListAdapter(ejerciciosList) { ejercicio ->
            if (cloudRoutineId.isNotBlank() && cloudRoutineExerciseId.isNotBlank()) {
                firebaseRepository.replaceExerciseInRoutine(
                    cloudRoutineId,
                    cloudRoutineExerciseId,
                    ejercicio.id,
                    ejercicio.nombre
                ) { result ->
                    runOnUiThread {
                        result.onSuccess {
                            Toast.makeText(this, "Ejercicio reemplazado", Toast.LENGTH_SHORT).show()
                            finish()
                        }.onFailure { error ->
                            Toast.makeText(
                                this,
                                error.message ?: "No se pudo reemplazar el ejercicio",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    }
                }
            } else {
                Toast.makeText(this, "No se encontró el ejercicio sincronizado", Toast.LENGTH_LONG).show()
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
