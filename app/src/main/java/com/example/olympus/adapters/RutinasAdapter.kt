package com.example.olympus

// Adapter principal para mostrar listas de rutinas tanto locales como de la nube
import android.graphics.Color
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.ForegroundColorSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.PopupMenu
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class RutinasAdapter(
    private var localRutinas: List<Rutina>,
    private val onLocalRutinaClick: (Int) -> Unit,
    private val onCloudRutinaClick: (String) -> Unit,
    private val onCloudEliminar: (String) -> Unit
) : RecyclerView.Adapter<RutinasAdapter.RutinaViewHolder>() {

    private var cloudRutinas: List<CloudRoutine> = emptyList()
    private var useCloudData: Boolean = false

    inner class RutinaViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvNombreRutina: TextView = view.findViewById(R.id.tvNombreRutina)
        val tvDiaRutina: TextView = view.findViewById(R.id.tvDiaRutina)
        val btnMenuRutina: ImageButton = view.findViewById(R.id.btnMenuRutina)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RutinaViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_rutina, parent, false)
        return RutinaViewHolder(view)
    }

    override fun onBindViewHolder(holder: RutinaViewHolder, position: Int) {
        if (useCloudData) {
            val rutina = cloudRutinas[position]
            holder.tvNombreRutina.text = rutina.nombre

            val diaNombre = when (rutina.diaSemana) {
                "L" -> "Lunes"
                "M" -> "Martes"
                "X" -> "Miércoles"
                "J" -> "Jueves"
                "V" -> "Viernes"
                "S" -> "Sábado"
                "D" -> "Domingo"
                else -> ""
            }
            holder.tvDiaRutina.text = diaNombre
            holder.tvDiaRutina.visibility = if (diaNombre.isEmpty()) View.GONE else View.VISIBLE

            holder.itemView.setOnClickListener {
                onCloudRutinaClick(rutina.id)
            }

            holder.btnMenuRutina.setOnClickListener {
                val popup = PopupMenu(holder.itemView.context, holder.btnMenuRutina)
                popup.menu.add("Eliminar rutina")

                for (i in 0 until popup.menu.size()) {
                    popup.menu.getItem(i).title = SpannableStringBuilder(popup.menu.getItem(i).title.toString()).apply {
                        setSpan(ForegroundColorSpan(Color.WHITE), 0, length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                    }
                }

                popup.setOnMenuItemClickListener {
                    onCloudEliminar(rutina.id)
                    true
                }
                popup.show()
            }
        } else {
            val rutina = localRutinas[position]
            holder.tvNombreRutina.text = rutina.nombre

            holder.itemView.setOnClickListener {
                onLocalRutinaClick(rutina.id)
            }

            holder.btnMenuRutina.setOnClickListener {
                val popup = PopupMenu(holder.itemView.context, holder.btnMenuRutina)
                popup.inflate(R.menu.menu_rutina)

                for (i in 0 until popup.menu.size()) {
                    popup.menu.getItem(i).title = SpannableStringBuilder(popup.menu.getItem(i).title.toString()).apply {
                        setSpan(ForegroundColorSpan(Color.WHITE), 0, length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                    }
                }

                popup.setOnMenuItemClickListener { item ->
                    when (item.itemId) {
                        R.id.action_eliminar -> {
                            val dbHelper = RutinasDatabaseHelper(holder.itemView.context)
                            dbHelper.eliminarRutina(rutina.id)
                            val newList = localRutinas.toMutableList()
                            newList.removeAt(position)
                            localRutinas = newList
                            notifyItemRemoved(position)
                            true
                        }
                        else -> false
                    }
                }
                popup.show()
            }
        }
    }

    override fun getItemCount() = if (useCloudData) cloudRutinas.size else localRutinas.size

    // Muestra las rutinas de la nube y cambia el modo de datos
    fun showCloudRutinas(newList: List<CloudRoutine>) {
        cloudRutinas = newList
        useCloudData = true
        notifyDataSetChanged()
    }

    // Muestra las rutinas locales y cambia el modo de datos
    fun showLocalRutinas(newList: List<Rutina>) {
        localRutinas = newList
        useCloudData = false
        notifyDataSetChanged()
    }
}
