package com.example.olympus

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class UsuariosAdapter(
    private val usuarios: List<UserProfile>,
    private val actionText: String = "Ver rutinas",
    private val onUsuarioClick: (UserProfile) -> Unit
) : RecyclerView.Adapter<UsuariosAdapter.UsuarioViewHolder>() {

    inner class UsuarioViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvNombreUsuario: TextView = view.findViewById(R.id.tvNombreUsuario)
        val tvAccionUsuario: TextView = view.findViewById(R.id.tvAccionUsuario)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UsuarioViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_usuario, parent, false)
        return UsuarioViewHolder(view)
    }

    override fun onBindViewHolder(holder: UsuarioViewHolder, position: Int) {
        val usuario = usuarios[position]
        holder.tvNombreUsuario.text = usuario.name
        holder.tvAccionUsuario.text = actionText

        holder.itemView.setOnClickListener {
            onUsuarioClick(usuario)
        }
    }

    override fun getItemCount() = usuarios.size
}
