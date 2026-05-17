package com.example.olympus

// Adapter para mostrar los planes nutricionales asignados al usuario
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class PlanesUsuarioAdapter(
    private val planes: List<CloudPlanNutricional>,
    private val firebaseRepository: FirebaseRepository
) : RecyclerView.Adapter<PlanesUsuarioAdapter.PlanViewHolder>() {

    inner class PlanViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvNombrePlan: TextView = view.findViewById(R.id.tvNombrePlanUsuario)
        val tvDescPlan: TextView = view.findViewById(R.id.tvDescPlanUsuario)
        val tvTotalCalorias: TextView = view.findViewById(R.id.tvTotalCaloriasPlan)
        val layoutComidas: LinearLayout = view.findViewById(R.id.layoutComidasExpandido)
        val btnExpandir: ImageButton = view.findViewById(R.id.btnExpandirPlan)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlanViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_plan_usuario, parent, false)
        return PlanViewHolder(view)
    }

    override fun onBindViewHolder(holder: PlanViewHolder, position: Int) {
        val plan = planes[position]

        holder.tvNombrePlan.text = plan.nombre
        holder.tvDescPlan.text = plan.descripcion.ifEmpty { "Sin descripción" }
        holder.tvTotalCalorias.text = "Total: calculando..."
        holder.layoutComidas.removeAllViews()

        firebaseRepository.getMealsOfPlan(plan.id) { result ->
            result.onSuccess { comidas ->
                holder.itemView.post {
                    val totalCal = comidas.sumOf { it.calorias }
                    holder.tvTotalCalorias.text = "Total: $totalCal kcal/día"
                    buildSlots(holder, comidas)
                }
            }.onFailure {
                holder.itemView.post {
                    holder.tvTotalCalorias.text = "Total: 0 kcal/día"
                    buildSlots(holder, emptyList())
                }
            }
        }

        var expandido = false
        holder.layoutComidas.visibility = View.GONE

        holder.btnExpandir.setOnClickListener {
            expandido = !expandido
            holder.layoutComidas.visibility = if (expandido) View.VISIBLE else View.GONE
            holder.btnExpandir.setImageResource(
                if (expandido) android.R.drawable.arrow_up_float
                else android.R.drawable.arrow_down_float
            )
        }

        holder.itemView.setOnClickListener {
            expandido = !expandido
            holder.layoutComidas.visibility = if (expandido) View.VISIBLE else View.GONE
            holder.btnExpandir.setImageResource(
                if (expandido) android.R.drawable.arrow_up_float
                else android.R.drawable.arrow_down_float
            )
        }
    }

    override fun getItemCount() = planes.size

    // Construye los slots de comida para mostrar en el plan
    private fun buildSlots(holder: PlanViewHolder, comidas: List<CloudComidaPlan>) {
        holder.layoutComidas.removeAllViews()

        for (tipo in ComidaData.TIPOS_COMIDA) {
            val comida = comidas.find { it.tipo == tipo }
            val slotView = LayoutInflater.from(holder.itemView.context)
                .inflate(R.layout.item_comida_usuario, holder.layoutComidas, false)

            slotView.findViewById<TextView>(R.id.tvTipoComidaUsuario).text = tipo

            val ivImagen = slotView.findViewById<ImageView>(R.id.ivComidaUsuarioImagen)
            val tvNombre = slotView.findViewById<TextView>(R.id.tvNombreComidaUsuario)
            val tvDesc = slotView.findViewById<TextView>(R.id.tvDescComidaUsuario)
            val tvCal = slotView.findViewById<TextView>(R.id.tvCaloriasComidaUsuario)
            val tvVacio = slotView.findViewById<TextView>(R.id.tvComidaVaciaUsuario)

            if (comida != null) {
                ivImagen.visibility = View.VISIBLE
                tvNombre.visibility = View.VISIBLE
                tvDesc.visibility = View.VISIBLE
                tvCal.visibility = View.VISIBLE
                tvVacio.visibility = View.GONE

                ivImagen.setImageResource(ComidaData.getDrawableResId(comida.imagenId))
                tvNombre.text = comida.nombre
                tvDesc.text = comida.descripcion
                tvCal.text = "${comida.calorias} kcal"
            } else {
                ivImagen.visibility = View.GONE
                tvNombre.visibility = View.GONE
                tvDesc.visibility = View.GONE
                tvCal.visibility = View.GONE
                tvVacio.visibility = View.VISIBLE
                tvVacio.text = "Sin comida asignada"
            }

            holder.layoutComidas.addView(slotView)
        }
    }
}
