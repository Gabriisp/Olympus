package com.example.olympus

// Fragmento que muestra las rutinas de entrenamiento del usuario
import android.content.Intent
import android.widget.Toast
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.RadioGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.olympus.utils.SessionManager

class RutinasFragment : Fragment() {

    private val firebaseRepository = FirebaseRepository.instance
    private lateinit var recyclerRutinas: RecyclerView
    private lateinit var btnNuevaRutina: Button
    private lateinit var rgFiltroDias: RadioGroup
    private lateinit var adapter: RutinasAdapter
    private var todasLasRutinas: List<CloudRoutine> = emptyList()
    private var diaFiltrado: String = ""

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_rutinas, container, false)

        recyclerRutinas = view.findViewById(R.id.recyclerRutinas)
        btnNuevaRutina = view.findViewById(R.id.btnNuevaRutina)
        rgFiltroDias = view.findViewById(R.id.rgFiltroDias)

        rgFiltroDias.setOnCheckedChangeListener { _, checkedId ->
            diaFiltrado = when (checkedId) {
                R.id.rbLunes -> "L"
                R.id.rbMartes -> "M"
                R.id.rbMiercoles -> "X"
                R.id.rbJueves -> "J"
                R.id.rbViernes -> "V"
                R.id.rbSabado -> "S"
                R.id.rbDomingo -> "D"
                else -> ""
            }
            filtrarRutinas()
        }

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
                            requireContext().showToast(error.message ?: "No se pudo eliminar la rutina", Toast.LENGTH_LONG)
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

    // Carga las rutinas del usuario desde Firebase
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
                    todasLasRutinas = cloudRutinas
                    filtrarRutinas()
                }.onFailure {
                    adapter.showCloudRutinas(emptyList())
                }
            }
        }
    }

    // Filtra las rutinas segun el dia de la semana seleccionado
    private fun filtrarRutinas() {
        val rutinasFiltradas = if (diaFiltrado.isEmpty()) {
            todasLasRutinas
        } else {
            todasLasRutinas.filter { it.diaSemana == diaFiltrado }
        }
        adapter.showCloudRutinas(rutinasFiltradas)
    }
}
