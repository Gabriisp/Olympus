package com.example.olympus

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class StatsFragment : Fragment() {

    private val firebaseRepository = FirebaseRepository.instance
    private lateinit var tvSubtitle: TextView
    private lateinit var tvEmpty: TextView
    private lateinit var recyclerRoutineStats: RecyclerView
    private lateinit var adapter: RoutineStatsAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_stats, container, false)

        tvSubtitle = view.findViewById(R.id.tvRoutineStatsSubtitle)
        tvEmpty = view.findViewById(R.id.tvRoutineStatsEmpty)
        recyclerRoutineStats = view.findViewById(R.id.recyclerRoutineStats)

        recyclerRoutineStats.layoutManager = LinearLayoutManager(requireContext())
        adapter = RoutineStatsAdapter(emptyList()) { routineId ->
            val intent = Intent(requireContext(), EstadisticasRutinaActivity::class.java)
            intent.putExtra("ROUTINE_ID", routineId)
            startActivity(intent)
        }
        recyclerRoutineStats.adapter = adapter

        loadRoutineStats()

        return view
    }

    override fun onResume() {
        super.onResume()
        loadRoutineStats()
    }

    private fun loadRoutineStats() {
        val sessionManager = SessionManager(requireContext())
        val userUid = sessionManager.getUserUid().orEmpty()

        if (userUid.isBlank()) {
            showEmptyState("Inicia sesión de nuevo para ver tus estadísticas")
            return
        }

        firebaseRepository.getUserRoutineStats(userUid) { result ->
            activity?.runOnUiThread {
                result.onSuccess { routinePairs ->
                    if (routinePairs.isNotEmpty()) {
                        val items = routinePairs.map { (routine, summary) ->
                            RoutineStatsItem(
                                routineId = routine.id,
                                routineName = routine.nombre,
                                summary = summary
                            )
                        }

                        tvSubtitle.text = "Rutinas detectadas: ${items.size}"
                        adapter.updateItems(items)
                        tvEmpty.visibility = View.GONE
                        recyclerRoutineStats.visibility = View.VISIBLE
                    } else {
                        showEmptyState("Todavía no tienes estadísticas sincronizadas")
                    }
                }.onFailure {
                    showEmptyState("No se pudieron cargar las estadísticas")
                }
            }
        }
    }

    private fun showEmptyState(message: String) {
        tvSubtitle.text = "Rutinas detectadas: 0"
        tvEmpty.text = message
        adapter.updateItems(emptyList())
        tvEmpty.visibility = View.VISIBLE
        recyclerRoutineStats.visibility = View.GONE
    }
}
