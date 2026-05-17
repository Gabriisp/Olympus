package com.example.olympus

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class ComidasPlanAdapter(
    private val comidasAsignadas: List<CloudComidaPlan>,
    private val onAgregarComida: (tipo: String) -> Unit,
    private val onEditarComida: (comida: CloudComidaPlan) -> Unit,
    private val onEliminarComida: (comidaId: String) -> Unit
) : RecyclerView.Adapter<ComidasPlanAdapter.SlotViewHolder>() {

    private val tipos = ComidaData.TIPOS_COMIDA

    inner class SlotViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvTipoComida: TextView = view.findViewById(R.id.tvTipoComida)
        val ivComidaImagen: ImageView = view.findViewById(R.id.ivComidaImagen)
        val tvNombreComida: TextView = view.findViewById(R.id.tvNombreComida)
        val tvDescComida: TextView = view.findViewById(R.id.tvDescComida)
        val tvCaloriasComida: TextView = view.findViewById(R.id.tvCaloriasComida)
        val btnAgregarComida: View = view.findViewById(R.id.btnAgregarComida)
        val layoutComidaAsignada: View = view.findViewById(R.id.layoutComidaAsignada)
        val btnEditarComida: ImageButton = view.findViewById(R.id.btnEditarComida)
        val btnEliminarComida: ImageButton = view.findViewById(R.id.btnEliminarComida)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SlotViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_comida_slot, parent, false)
        return SlotViewHolder(view)
    }

    override fun onBindViewHolder(holder: SlotViewHolder, position: Int) {
        val tipo = tipos[position]
        val emoji = ComidaData.EMOJIS_TIPO[tipo] ?: ""
        holder.tvTipoComida.text = "$emoji $tipo"

        val comidaAsignada = comidasAsignadas.find { it.tipo == tipo }

        if (comidaAsignada != null) {
            holder.layoutComidaAsignada.visibility = View.VISIBLE
            holder.btnAgregarComida.visibility = View.GONE
            holder.tvNombreComida.text = comidaAsignada.nombre
            holder.tvDescComida.text = comidaAsignada.descripcion
            holder.tvCaloriasComida.text = "${comidaAsignada.calorias} kcal"
            holder.ivComidaImagen.setImageResource(android.R.drawable.ic_menu_gallery)

            holder.btnEditarComida.setOnClickListener { onEditarComida(comidaAsignada) }
            holder.btnEliminarComida.setOnClickListener { onEliminarComida(comidaAsignada.id) }
        } else {
            holder.layoutComidaAsignada.visibility = View.GONE
            holder.btnAgregarComida.visibility = View.VISIBLE
            holder.btnAgregarComida.setOnClickListener { onAgregarComida(tipo) }
        }
    }

    override fun getItemCount() = tipos.size
}
