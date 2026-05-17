package com.example.olympus

// Adapter para mostrar los items en el carrito de compras
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class CarritoAdapter(
    private val items: List<CarritoItem>,
    private val onQuitarClick: (Articulo) -> Unit
) : RecyclerView.Adapter<CarritoAdapter.CarritoViewHolder>() {

    inner class CarritoViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val ivArticulo: ImageView = view.findViewById(R.id.ivArticuloCarrito)
        val tvNombre: TextView = view.findViewById(R.id.tvNombreArticuloCarrito)
        val tvCantidad: TextView = view.findViewById(R.id.tvCantidadCarrito)
        val tvSubtotal: TextView = view.findViewById(R.id.tvSubtotalCarrito)
        val btnQuitar: Button = view.findViewById(R.id.btnQuitarCarrito)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CarritoViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_carrito, parent, false)
        return CarritoViewHolder(view)
    }

    override fun onBindViewHolder(holder: CarritoViewHolder, position: Int) {
        val item = items[position]
        holder.ivArticulo.setImageResource(item.articulo.imagenResId)
        holder.tvNombre.text = item.articulo.nombre
        holder.tvCantidad.text = "x${item.cantidad}"
        holder.tvSubtotal.text = String.format("%.2f€", item.subtotal)

        holder.btnQuitar.setOnClickListener {
            onQuitarClick(item.articulo)
        }
    }

    override fun getItemCount() = items.size
}