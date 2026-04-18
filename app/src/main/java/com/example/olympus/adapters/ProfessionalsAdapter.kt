package com.example.olympus

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class ProfessionalsAdapter(
    private var professionals: List<UserProfile>,
    private val onProfessionalClick: (UserProfile) -> Unit
) : RecyclerView.Adapter<ProfessionalsAdapter.ProfessionalViewHolder>() {

    inner class ProfessionalViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvName: TextView = view.findViewById(R.id.tvProfessionalName)
        val tvEmail: TextView = view.findViewById(R.id.tvProfessionalEmail)
        val tvRole: TextView = view.findViewById(R.id.tvProfessionalRole)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProfessionalViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_professional, parent, false)
        return ProfessionalViewHolder(view)
    }

    override fun onBindViewHolder(holder: ProfessionalViewHolder, position: Int) {
        val professional = professionals[position]
        holder.tvName.text = professional.name
        holder.tvEmail.text = professional.email
        holder.tvRole.text = professional.role
        holder.itemView.setOnClickListener {
            onProfessionalClick(professional)
        }
    }

    override fun getItemCount(): Int = professionals.size

    fun updateData(newProfessionals: List<UserProfile>) {
        professionals = newProfessionals
        notifyDataSetChanged()
    }
}
