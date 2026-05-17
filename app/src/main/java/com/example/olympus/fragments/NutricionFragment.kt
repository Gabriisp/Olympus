package com.example.olympus

// Fragmento que muestra los planes nutricionales del usuario
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.olympus.utils.SessionManager

class NutricionFragment : Fragment() {

    private val firebaseRepository = FirebaseRepository.instance
    private lateinit var recyclerPlanes: RecyclerView
    private lateinit var tvSinPlanes: TextView
    private lateinit var sessionManager: SessionManager

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_nutricion, container, false)

        sessionManager = SessionManager(requireContext())
        recyclerPlanes = view.findViewById(R.id.recyclerPlanesNutricion)
        tvSinPlanes = view.findViewById(R.id.tvSinPlanesNutricion)

        recyclerPlanes.layoutManager = LinearLayoutManager(requireContext())
        loadPlanes()

        return view
    }

    override fun onResume() {
        super.onResume()
        loadPlanes()
    }

    // Carga los planes nutricionales del usuario desde Firebase
    private fun loadPlanes() {
        val userUid = sessionManager.getUserUid().orEmpty()
        if (userUid.isBlank()) return

        firebaseRepository.getPlansByUser(userUid) { result ->
            activity?.runOnUiThread {
                result.onSuccess { planes ->
                    if (planes.isEmpty()) {
                        tvSinPlanes.visibility = View.VISIBLE
                        recyclerPlanes.visibility = View.GONE
                    } else {
                        tvSinPlanes.visibility = View.GONE
                        recyclerPlanes.visibility = View.VISIBLE
                        recyclerPlanes.adapter = PlanesUsuarioAdapter(planes, firebaseRepository)
                    }
                }.onFailure { error ->
                    requireContext().showToast(error.message ?: "No se pudieron cargar los planes", Toast.LENGTH_LONG)
                }
            }
        }
    }
}
