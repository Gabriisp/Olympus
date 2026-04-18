package com.example.olympus

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.PopupMenu
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class CloudRutinaEjerciciosAdapter(
    private var ejercicios: List<CloudRoutineExercise>,
    private val firebaseRepository: FirebaseRepository,
    private val routineId: String,
    private val onAgregarSerie: (String) -> Unit,
    private val onEliminarEjercicio: (String) -> Unit,
    private val onReordenar: (String, Boolean) -> Unit,
    private val onReemplazar: (String) -> Unit
) : RecyclerView.Adapter<CloudRutinaEjerciciosAdapter.EjercicioViewHolder>() {

    inner class EjercicioViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvNombreEjercicio: TextView = view.findViewById(R.id.tvNombreEjercicio)
        val btnAgregarSerie: Button = view.findViewById(R.id.btnAgregarSerie)
        val btnMenuEjercicio: ImageButton = view.findViewById(R.id.btnMenuEjercicio)
        val recyclerSeries: RecyclerView = view.findViewById(R.id.recyclerSeries)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EjercicioViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_ejercicio_rutina, parent, false)
        return EjercicioViewHolder(view)
    }

    override fun onBindViewHolder(holder: EjercicioViewHolder, position: Int) {
        val ejercicio = ejercicios[position]
        holder.tvNombreEjercicio.text = ejercicio.exerciseName

        holder.btnAgregarSerie.setOnClickListener {
            onAgregarSerie(ejercicio.id)
        }

        holder.btnMenuEjercicio.setOnClickListener {
            val popup = PopupMenu(holder.itemView.context, holder.btnMenuEjercicio)
            popup.inflate(R.menu.menu_ejercicio_rutina)

            popup.setOnMenuItemClickListener { item ->
                when (item.itemId) {
                    R.id.action_mover_arriba -> {
                        if (position > 0) {
                            onReordenar(ejercicio.id, true)
                        }
                        true
                    }
                    R.id.action_mover_abajo -> {
                        if (position < ejercicios.size - 1) {
                            onReordenar(ejercicio.id, false)
                        }
                        true
                    }
                    R.id.action_reemplazar -> {
                        onReemplazar(ejercicio.id)
                        true
                    }
                    R.id.action_eliminar_ejercicio -> {
                        onEliminarEjercicio(ejercicio.id)
                        true
                    }
                    else -> false
                }
            }
            popup.show()
        }

        holder.recyclerSeries.layoutManager = LinearLayoutManager(holder.itemView.context)
        firebaseRepository.getSetsOfExercise(routineId, ejercicio.id) { result ->
            holder.itemView.post {
                val series = result.getOrDefault(emptyList())
                holder.recyclerSeries.adapter = CloudSeriesAdapter(series) { setId ->
                    firebaseRepository.deleteSet(routineId, ejercicio.id, setId) {
                        holder.itemView.post { notifyItemChanged(position) }
                    }
                }
            }
        }
    }

    override fun getItemCount() = ejercicios.size

    fun updateEjercicios(newList: List<CloudRoutineExercise>) {
        ejercicios = newList
        notifyDataSetChanged()
    }
}
