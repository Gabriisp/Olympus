package com.example.olympus

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

data class RoutineStatsItem(
    val routineId: String,
    val routineName: String,
    val summary: RoutineProgressSummary
)

class RoutineStatsAdapter(
    private var items: List<RoutineStatsItem>,
    private val onRoutineClick: (String) -> Unit
) : RecyclerView.Adapter<RoutineStatsAdapter.RoutineStatsViewHolder>() {

    inner class RoutineStatsViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvName: TextView = view.findViewById(R.id.tvRoutineStatName)
        val tvSummary: TextView = view.findViewById(R.id.tvRoutineStatSummary)
        val tvHighlights: TextView = view.findViewById(R.id.tvRoutineStatHighlights)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RoutineStatsViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_stat_rutina, parent, false)
        return RoutineStatsViewHolder(view)
    }

    override fun onBindViewHolder(holder: RoutineStatsViewHolder, position: Int) {
        val item = items[position]
        holder.tvName.text = item.routineName
        holder.tvSummary.text =
            "Ejercicios: ${item.summary.totalEjercicios} · Series: ${item.summary.totalSeries} · Volumen: ${formatDecimal(item.summary.totalVolumen)} kg"
        holder.tvHighlights.text =
            "Destacado: ${item.summary.ejercicioDestacado} · Mejor tendencia: ${item.summary.mejorTendencia}"

        holder.itemView.setOnClickListener {
            onRoutineClick(item.routineId)
        }
    }

    override fun getItemCount(): Int = items.size

    fun updateItems(newItems: List<RoutineStatsItem>) {
        items = newItems
        notifyDataSetChanged()
    }

    private fun formatDecimal(value: Float): String {
        return if (value % 1f == 0f) {
            value.toInt().toString()
        } else {
            String.format("%.1f", value)
        }
    }
}
