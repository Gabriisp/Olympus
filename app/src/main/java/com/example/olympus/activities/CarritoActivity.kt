package com.example.olympus

// Pantalla del carrito de compras de la tienda
// Muestra los articulos agregados, permite confirmar compra y ver historial

import android.app.Dialog
import android.app.ProgressDialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.View
import android.view.Window
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.olympus.utils.SessionManager

class CarritoActivity : AppCompatActivity() {

    private lateinit var rvCarrito: RecyclerView
    private lateinit var tvCarritoVacio: TextView
    private lateinit var tvTotalCarrito: TextView
    private lateinit var btnConfirmarCompra: Button
    private lateinit var btnVolver: ImageButton

    private lateinit var carritoAdapter: CarritoAdapter
    private val carritoManager = CarritoManager.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_carrito)

        initViews()
        setupRecyclerViews()
        setupBotones()
        actualizarCarrito()
    }

    // Actualiza la visibilidad de elementos segun si el carrito tiene items o no
    private fun actualizarCarrito() {
        val items = carritoManager.getItems()

        if (items.isEmpty()) {
            tvCarritoVacio.visibility = View.VISIBLE
            rvCarrito.visibility = View.GONE
            btnConfirmarCompra.isEnabled = false
            tvTotalCarrito.text = "0.00€"
        } else {
            tvCarritoVacio.visibility = View.GONE
            rvCarrito.visibility = View.VISIBLE
            btnConfirmarCompra.isEnabled = true

            carritoAdapter = CarritoAdapter(
                items = items,
                onQuitarClick = { articulo ->
                    eliminarDelCarrito(articulo)
                }
            )
            rvCarrito.adapter = carritoAdapter

            tvTotalCarrito.text = String.format("%.2f€", carritoManager.getTotal())
        }
    }

    // Inicializa las vistas con findViewById
    private fun initViews() {
        rvCarrito = findViewById(R.id.rvCarrito)
        tvCarritoVacio = findViewById(R.id.tvCarritoVacio)
        tvTotalCarrito = findViewById(R.id.tvTotalCarrito)
        btnConfirmarCompra = findViewById(R.id.btnConfirmarCompra)
        btnVolver = findViewById(R.id.btnVolver)
    }

    // Configura el RecyclerView con LinearLayoutManager
    private fun setupRecyclerViews() {
        rvCarrito.layoutManager = LinearLayoutManager(this)
    }

    // Configura los listeners de los botones
    private fun setupBotones() {
        btnVolver.setOnClickListener {
            finish()
        }

        btnConfirmarCompra.setOnClickListener {
            confirmarCompra()
        }
    }

    // Elimina una unidad del articulo y actualiza la vista
    private fun eliminarDelCarrito(articulo: Articulo) {
        val cantidadActual = carritoManager.getItems().find { it.articulo.id == articulo.id }?.cantidad ?: 0
        if (cantidadActual > 0) {
            carritoManager.eliminarArticulo(articulo)
            actualizarCarrito()
        }
    }

    // Muestra un dialogo de confirmacion antes de procesar la compra
    private fun confirmarCompra() {
        val items = carritoManager.getItems()
        val total = carritoManager.getTotal()

        val mensaje = buildString {
            appendLine("¿Confirmar compra?")
            appendLine()
            items.forEach { item ->
                appendLine("• ${item.articulo.nombre} x${item.cantidad} = ${String.format("%.2f€", item.subtotal)}")
            }
            appendLine()
            append("Total: ${String.format("%.2f€", total)}")
        }

        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.dialog_confirmar_compra)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.window?.setLayout(
            android.view.ViewGroup.LayoutParams.MATCH_PARENT,
            android.view.ViewGroup.LayoutParams.WRAP_CONTENT
        )

        dialog.findViewById<TextView>(R.id.tvMensajeConfirmar).text = mensaje

        dialog.findViewById<Button>(R.id.btnCancelarCompra).setOnClickListener {
            dialog.dismiss()
        }

        dialog.findViewById<Button>(R.id.btnConfirmarCompraDialog).setOnClickListener {
            dialog.dismiss()
            guardarCompraYFinalizar(items, total)
        }

        dialog.show()
    }

    // Guarda la compra en Firebase y vacia el carrito
    private fun guardarCompraYFinalizar(items: List<CarritoItem>, total: Double) {
        val sessionManager = SessionManager(this)
        val userUid = sessionManager.getUserUid()

        if (userUid.isNullOrBlank()) {
            showToast("Error: No se encontro el usuario")
            return
        }

        val compraItems = items.map { item ->
            CompraItem(
                articuloId = item.articulo.id,
                nombre = item.articulo.nombre,
                cantidad = item.cantidad,
                precioUnitario = item.articulo.precio,
                subtotal = item.subtotal
            )
        }

        val progressDialog = ProgressDialog(this)
        progressDialog.setMessage("Guardando compra...")
        progressDialog.setCancelable(false)
        progressDialog.show()

        FirebaseRepository.instance.savePurchase(userUid, compraItems, total) { result ->
            runOnUiThread {
                progressDialog.dismiss()
                result.onSuccess {
                    showToast("¡Compra realizada con éxito!", Toast.LENGTH_LONG)
                    carritoManager.vaciarCarrito()
                    actualizarCarrito()
                }.onFailure { error ->
                    showToast("Error al guardar compra: ${error.message}")
                }
            }
        }
    }
}
