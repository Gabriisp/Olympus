package com.example.olympus

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class CloudNotasAdapter(
    private val notas: List<CloudNote>,
    private val onEliminarNota: (String) -> Unit
) : RecyclerView.Adapter<CloudNotasAdapter.NotaViewHolder>() {

    inner class NotaViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvTitulo: TextView = view.findViewById(R.id.tvTituloNota)
        val tvContenido: TextView = view.findViewById(R.id.tvContenidoNota)
        val btnEliminar: ImageButton = view.findViewById(R.id.btnEliminarNota)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NotaViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_nota, parent, false)
        return NotaViewHolder(view)
    }

    override fun onBindViewHolder(holder: NotaViewHolder, position: Int) {
        val nota = notas[position]
        holder.tvTitulo.text = nota.titulo
        holder.tvContenido.text = nota.contenido
        holder.btnEliminar.setOnClickListener {
            onEliminarNota(nota.id)
        }
    }

    override fun getItemCount() = notas.size
}
