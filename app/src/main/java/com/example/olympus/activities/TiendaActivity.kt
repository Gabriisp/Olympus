package com.example.olympus

// Pantalla de la tienda que muestra articulos disponibles
// Permite agregar articulos al carrito y ver el historial de compras

import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.Window
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView

class TiendaActivity : AppCompatActivity() {

    private lateinit var rvArticulos: RecyclerView
    private lateinit var btnCarrito: Button
    private lateinit var btnHistorial: Button

    private lateinit var tiendaAdapter: TiendaAdapter

    private val carritoManager = CarritoManager.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tienda)

        initViews()
        setupRecyclerViews()
        setupCarrito()
        actualizarBadgeCarrito()
    }

    override fun onResume() {
        super.onResume()
        actualizarBadgeCarrito()
    }

    private fun initViews() {
        rvArticulos = findViewById(R.id.rvArticulos)
        btnCarrito = findViewById(R.id.btnCarrito)
        btnHistorial = findViewById(R.id.btnHistorial)
    }

    private fun setupRecyclerViews() {
        val articulos = ArticulosData.listaArticulos

        tiendaAdapter = TiendaAdapter(
            articulos = articulos,
            onImageClick = { articulo -> mostrarZoomImagen(articulo) },
            onAgregarClick = { articulo -> agregarAlCarrito(articulo) },
            onEliminarClick = { articulo -> eliminarDelCarrito(articulo) },
            getCantidadEnCarrito = { articuloId ->
                carritoManager.getItems().find { it.articulo.id == articuloId }?.cantidad ?: 0
            }
        )

        rvArticulos.layoutManager = GridLayoutManager(this, 2)
        rvArticulos.adapter = tiendaAdapter
    }

    private fun setupCarrito() {
        btnCarrito.setOnClickListener {
            mostrarDialogoCarrito()
        }
        btnHistorial.setOnClickListener {
            startActivity(Intent(this, HistorialActivity::class.java))
        }
    }

    // Agrega un articulo al carrito y actualiza el badge
    private fun agregarAlCarrito(articulo: Articulo) {
        carritoManager.agregarArticulo(articulo)
        actualizarBadgeCarrito()
        tiendaAdapter.notifyDataSetChanged()
        showToast("${articulo.nombre} anadido")
    }

    // Elimina una unidad del articulo del carrito
    private fun eliminarDelCarrito(articulo: Articulo) {
        val cantidadActual = carritoManager.getItems().find { it.articulo.id == articulo.id }?.cantidad ?: 0
        if (cantidadActual > 0) {
            carritoManager.eliminarArticulo(articulo)
            actualizarBadgeCarrito()
            tiendaAdapter.notifyDataSetChanged()
        }
    }

    // Actualiza el texto del boton carrito con la cantidad y el total
    private fun actualizarBadgeCarrito() {
        val cantidad = carritoManager.getCantidadTotal()
        val total = carritoManager.getTotal()

        if (cantidad > 0) {
            btnCarrito.text = "Carrito ($cantidad) - ${String.format("%.2f€", total)}"
            btnCarrito.isEnabled = true
        } else {
            btnCarrito.text = "Carrito (0)"
            btnCarrito.isEnabled = false
        }
    }

    // Muestra un dialogo con el resumen del carrito
    private fun mostrarDialogoCarrito() {
        val items = carritoManager.getItems()

        if (items.isEmpty()) {
            showToast("El carrito esta vacio")
            return
        }

        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.dialog_carrito)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.window?.setLayout(-1, -2)

        val tvResumen = dialog.findViewById<TextView>(R.id.tvResumenCarrito)
        val tvTotalResumen = dialog.findViewById<TextView>(R.id.tvTotalResumen)
        val btnIrCarrito = dialog.findViewById<Button>(R.id.btnIrCarrito)
        val btnCerrar = dialog.findViewById<Button>(R.id.btnCerrarDialogo)

        val total = carritoManager.getTotal()

        // Construir el resumen de items del carrito
        val resumen = buildString {
            items.forEach { item ->
                appendLine("• ${item.articulo.nombre} x${item.cantidad} = ${String.format("%.2f€", item.subtotal)}")
            }
        }

        tvResumen.text = resumen
        tvTotalResumen.text = String.format("%.2f€", total)

        btnIrCarrito.setOnClickListener {
            dialog.dismiss()
            startActivity(Intent(this, CarritoActivity::class.java))
        }

        btnCerrar.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }

    // Muestra un dialogo con la imagen ampliada del articulo
    private fun mostrarZoomImagen(articulo: Articulo) {
        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.dialog_imagen)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.window?.setLayout(-1, -2)

        val ivImagen = dialog.findViewById<ImageView>(R.id.ivArticuloZoom)
        val tvNombre = dialog.findViewById<TextView>(R.id.tvNombreArticuloZoom)
        val tvDescripcion = dialog.findViewById<TextView>(R.id.tvDescripcionArticuloZoom)
        val tvPrecio = dialog.findViewById<TextView>(R.id.tvPrecioArticuloZoom)
        val btnCerrar = dialog.findViewById<Button>(R.id.btnCerrarZoom)

        ivImagen.setImageResource(articulo.imagenResId)
        tvNombre.text = articulo.nombre
        tvDescripcion.text = articulo.descripcion
        tvPrecio.text = String.format("%.2f€", articulo.precio)

        btnCerrar.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }
}