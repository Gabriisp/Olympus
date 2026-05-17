package com.example.olympus

// Adapter para mostrar el historial de compras del usuario
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class HistorialAdapter(
    private val compras: List<Compra>
) : RecyclerView.Adapter<HistorialAdapter.HistorialViewHolder>() {

    class HistorialViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvFechaCompra: TextView = itemView.findViewById(R.id.tvFechaCompra)
        val tvTotalCompra: TextView = itemView.findViewById(R.id.tvTotalCompra)
        val llItemsCompra: LinearLayout = itemView.findViewById(R.id.llItemsCompra)
    }

    override fun onCreateViewHolder(parent: android.view.ViewGroup, viewType: Int): HistorialViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_historial, parent, false)
        return HistorialViewHolder(view)
    }

    override fun onBindViewHolder(holder: HistorialViewHolder, position: Int) {
        val compra = compras[position]

        val dateFormat = SimpleDateFormat("dd/MM/yyyy - HH:mm", Locale.getDefault())
        holder.tvFechaCompra.text = dateFormat.format(Date(compra.fecha))
        holder.tvTotalCompra.text = String.format("%.2f€", compra.total)

        holder.llItemsCompra.removeAllViews()
        compra.items.forEach { item ->
            val textView = TextView(holder.itemView.context).apply {
                text = "• ${item.nombre} x${item.cantidad} = ${String.format("%.2f€", item.subtotal)}"
                setTextColor(context.getColor(R.color.gym_text_secondary))
                textSize = 12f
            }
            holder.llItemsCompra.addView(textView)
        }
        holder.llItemsCompra.visibility = View.VISIBLE

        holder.itemView.setOnClickListener {
            holder.llItemsCompra.visibility = if (holder.llItemsCompra.visibility == View.VISIBLE) {
                View.GONE
            } else {
                View.VISIBLE
            }
        }
    }

    override fun getItemCount() = compras.size
}