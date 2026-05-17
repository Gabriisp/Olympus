package com.example.olympus

// Adapter para mostrar los articulos disponibles en la tienda
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class TiendaAdapter(
    private val articulos: List<Articulo>,
    private val onImageClick: (Articulo) -> Unit,
    private val onAgregarClick: (Articulo) -> Unit,
    private val onEliminarClick: (Articulo) -> Unit,
    private val getCantidadEnCarrito: (Int) -> Int
) : RecyclerView.Adapter<TiendaAdapter.ArticuloViewHolder>() {

    inner class ArticuloViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val ivArticulo: ImageView = view.findViewById(R.id.ivArticulo)
        val tvNombre: TextView = view.findViewById(R.id.tvNombreArticulo)
        val tvDescripcion: TextView = view.findViewById(R.id.tvDescripcionArticulo)
        val tvPrecio: TextView = view.findViewById(R.id.tvPrecioArticulo)
        val tvCantidad: TextView = view.findViewById(R.id.tvCantidad)
        val btnAgregar: Button = view.findViewById(R.id.btnAgregar)
        val btnEliminar: Button = view.findViewById(R.id.btnEliminar)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ArticuloViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_articulo, parent, false)
        return ArticuloViewHolder(view)
    }

    override fun onBindViewHolder(holder: ArticuloViewHolder, position: Int) {
        val articulo = articulos[position]
        holder.ivArticulo.setImageResource(articulo.imagenResId)
        holder.tvNombre.text = articulo.nombre
        holder.tvDescripcion.text = articulo.descripcion
        holder.tvPrecio.text = String.format("%.2f€", articulo.precio)

        val cantidad = getCantidadEnCarrito(articulo.id)
        holder.tvCantidad.text = cantidad.toString()
        holder.tvCantidad.visibility = if (cantidad > 0) View.VISIBLE else View.INVISIBLE

        holder.ivArticulo.setOnClickListener {
            onImageClick(articulo)
        }

        holder.btnAgregar.setOnClickListener {
            onAgregarClick(articulo)
        }

        holder.btnEliminar.setOnClickListener {
            if (cantidad > 0) {
                onEliminarClick(articulo)
            }
        }
    }

    override fun getItemCount() = articulos.size
}