package com.example.olympus

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.PopupMenu
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class PlanesNutricionalesAdapter(
    private val planes: List<CloudPlanNutricional>,
    private val firebaseRepository: FirebaseRepository,
    private val onVerDetalles: (String) -> Unit,
    private val onEliminar: (String) -> Unit
) : RecyclerView.Adapter<PlanesNutricionalesAdapter.PlanViewHolder>() {

    inner class PlanViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvNombrePlan: TextView = view.findViewById(R.id.tvNombrePlanItem)
        val tvDescPlan: TextView = view.findViewById(R.id.tvDescripcionPlanItem)
        val tvNumComidas: TextView = view.findViewById(R.id.tvNumComidasItem)
        val btnMenu: ImageButton = view.findViewById(R.id.btnMenuPlanNutricionista)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlanViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_plan_nutricional, parent, false)
        return PlanViewHolder(view)
    }

    override fun onBindViewHolder(holder: PlanViewHolder, position: Int) {
        val plan = planes[position]
        holder.tvNombrePlan.text = plan.nombre
        holder.tvDescPlan.text = plan.descripcion.ifEmpty { "Sin descripción" }
        holder.tvNumComidas.text = "Cargando..."

        firebaseRepository.getMealsOfPlan(plan.id) { result ->
            result.onSuccess { comidas ->
                holder.tvNumComidas.post {
                    holder.tvNumComidas.text = "${comidas.size}/4 comidas"
                }
            }.onFailure {
                holder.tvNumComidas.post {
                    holder.tvNumComidas.text = "0/4 comidas"
                }
            }
        }

        holder.itemView.setOnClickListener { onVerDetalles(plan.id) }

        holder.btnMenu.setOnClickListener {
            val popup = PopupMenu(holder.itemView.context, holder.btnMenu)
            popup.inflate(R.menu.menu_plan_nutricional)
            popup.setOnMenuItemClickListener { item ->
                when (item.itemId) {
                    R.id.action_editar_plan -> {
                        onVerDetalles(plan.id)
                        true
                    }
                    R.id.action_eliminar_plan -> {
                        onEliminar(plan.id)
                        true
                    }
                    else -> false
                }
            }
            popup.show()
        }
    }

    override fun getItemCount() = planes.size
}
