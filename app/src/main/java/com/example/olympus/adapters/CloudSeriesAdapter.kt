package com.example.olympus

// Adapter para mostrar las series de un ejercicio en la nube
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class CloudSeriesAdapter(
    private val series: List<CloudRoutineSet>,
    private val onEliminarSerie: (String) -> Unit,
    private val onEditarSerie: (CloudRoutineSet) -> Unit
) : RecyclerView.Adapter<CloudSeriesAdapter.SerieViewHolder>() {

    inner class SerieViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvSerie: TextView = view.findViewById(R.id.tvSerie)
        val btnEliminarSerie: ImageButton = view.findViewById(R.id.btnEliminarSerie)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SerieViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_serie, parent, false)
        return SerieViewHolder(view)
    }

    override fun onBindViewHolder(holder: SerieViewHolder, position: Int) {
        val serie = series[position]
        holder.tvSerie.text = "Serie ${position + 1}: ${serie.peso} kg x ${serie.repeticiones} reps"
        holder.tvSerie.setOnClickListener {
            onEditarSerie(serie)
        }
        holder.btnEliminarSerie.setOnClickListener {
            onEliminarSerie(serie.id)
        }
    }

    override fun getItemCount() = series.size
}
