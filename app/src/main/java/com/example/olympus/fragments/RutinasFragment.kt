package com.example.olympus

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class RutinasFragment : Fragment() {

    private val firebaseRepository = FirebaseRepository.instance
    private lateinit var recyclerRutinas: RecyclerView
    private lateinit var btnNuevaRutina: Button
    private lateinit var adapter: RutinasAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_rutinas, container, false)

        recyclerRutinas = view.findViewById(R.id.recyclerRutinas)
        btnNuevaRutina = view.findViewById(R.id.btnNuevaRutina)

        recyclerRutinas.layoutManager = LinearLayoutManager(requireContext())
        adapter = RutinasAdapter(
            localRutinas = emptyList(),
            onLocalRutinaClick = { },
            onCloudRutinaClick = { routineId ->
                val intent = Intent(requireContext(), CrearRutinaActivity::class.java)
                intent.putExtra("CLOUD_ROUTINE_ID", routineId)
                startActivity(intent)
            },
            onCloudEliminar = { routineId ->
                firebaseRepository.deleteRoutine(routineId) { result ->
                    activity?.runOnUiThread {
                        result.onSuccess {
                            loadRutinas()
                        }.onFailure { error ->
                            android.widget.Toast.makeText(
                                requireContext(),
                                error.message ?: "No se pudo eliminar la rutina",
                                android.widget.Toast.LENGTH_LONG
                            ).show()
                        }
                    }
                }
            }
        )
        recyclerRutinas.adapter = adapter
        
        loadRutinas()

        btnNuevaRutina.setOnClickListener {
            val intent = Intent(requireContext(), CrearRutinaActivity::class.java)
            startActivity(intent)
        }

        return view
    }

    override fun onResume() {
        super.onResume()
        loadRutinas()
    }

    private fun loadRutinas() {
        val sessionManager = SessionManager(requireContext())
        val userUid = sessionManager.getUserUid().orEmpty()

        if (userUid.isBlank()) {
            adapter.showCloudRutinas(emptyList())
            return
        }

        firebaseRepository.getRoutinesByUser(userUid) { result ->
            activity?.runOnUiThread {
                result.onSuccess { cloudRutinas ->
                    adapter.showCloudRutinas(cloudRutinas)
                }.onFailure {
                    adapter.showCloudRutinas(emptyList())
                }
            }
        }
    }
}
