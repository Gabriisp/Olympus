package com.example.olympus

// Adapter para mostrar las comidas disponibles para seleccionar
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class ComidasDisponiblesAdapter(
    private val comidas: List<ComidaDisponible>,
    private val onSeleccionar: (ComidaDisponible) -> Unit
) : RecyclerView.Adapter<ComidasDisponiblesAdapter.ComidaViewHolder>() {

    inner class ComidaViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val ivImagen: ImageView     = view.findViewById(R.id.ivComidaDisponibleImagen)
        val tvNombre: TextView      = view.findViewById(R.id.tvNombreComidaDisponible)
        val tvDescripcion: TextView = view.findViewById(R.id.tvDescComidaDisponible)
        val tvCalorias: TextView    = view.findViewById(R.id.tvCaloriasComidaDisponible)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ComidaViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_comida_disponible, parent, false)
        return ComidaViewHolder(view)
    }

    override fun onBindViewHolder(holder: ComidaViewHolder, position: Int) {
        val comida = comidas[position]
        holder.ivImagen.setImageResource(comida.imagenRes)
        holder.tvNombre.text      = comida.nombre
        holder.tvDescripcion.text = comida.descripcion
        holder.tvCalorias.text    = "${comida.caloriasBase} kcal"
        holder.itemView.setOnClickListener { onSeleccionar(comida) }
    }

    override fun getItemCount() = comidas.size
}
