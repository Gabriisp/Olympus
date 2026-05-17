package com.example.olympus

// Adapter para mostrar estadísticas de un ejercicio con grafico de progreso
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import kotlin.math.abs

class ExerciseStatsAdapter(
    private var stats: List<ExerciseProgressStat>
) : RecyclerView.Adapter<ExerciseStatsAdapter.ExerciseStatsViewHolder>() {

    inner class ExerciseStatsViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvExerciseName: TextView = view.findViewById(R.id.tvStatExerciseName)
        val tvTrend: TextView = view.findViewById(R.id.tvStatExerciseTrend)
        val tvSummary: TextView = view.findViewById(R.id.tvStatExerciseSummary)
        val tvVolume: TextView = view.findViewById(R.id.tvStatExerciseVolume)
        val chart: LineChart = view.findViewById(R.id.chartExerciseProgress)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ExerciseStatsViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_stat_ejercicio, parent, false)
        return ExerciseStatsViewHolder(view)
    }

    override fun onBindViewHolder(holder: ExerciseStatsViewHolder, position: Int) {
        val stat = stats[position]
        holder.tvExerciseName.text = stat.ejercicioNombre
        holder.tvTrend.text = buildTrendLabel(stat)
        holder.tvTrend.setTextColor(resolveTrendColor(stat.tendenciaPeso))
        holder.tvSummary.text =
            "Series: ${stat.totalSeries} · Max: ${formatDecimal(stat.maxPeso)} kg · Reps máx: ${stat.maxRepeticiones} · Media: ${formatDecimal(stat.promedioPeso)} kg"
        holder.tvVolume.text =
            "Volumen total: ${formatDecimal(stat.totalVolumen)} kg · Repeticiones acumuladas: ${stat.totalRepeticiones}"
        setupExerciseChart(holder.chart, stat)
    }

    override fun getItemCount(): Int = stats.size

    // Actualiza las estadisticas y refresca la vista
    fun updateStats(newStats: List<ExerciseProgressStat>) {
        stats = newStats
        notifyDataSetChanged()
    }

    // Construye la etiqueta de tendencia para mostrar progreso
    private fun buildTrendLabel(stat: ExerciseProgressStat): String {
        val difMaxPeso = if (stat.pesosPorSerie.size >= 2)
            stat.pesosPorSerie.last() - stat.pesosPorSerie.first() else 0f
        val difVolumen = if (stat.evolucionVolumen.size >= 2)
            stat.evolucionVolumen.last() - stat.evolucionVolumen.first() else 0f

        return when {
            difMaxPeso > 0f ->
                " +${formatDecimal(difMaxPeso)} kg de peso máximo desde el inicio"
            difMaxPeso < 0f ->
                " ${formatDecimal(difMaxPeso)} kg de peso máximo desde el inicio"
            difVolumen > 0f ->
                " +${formatDecimal(difVolumen)} kg de volumen total desde el inicio"
            difVolumen < 0f ->
                " -${formatDecimal(abs(difVolumen))} kg de volumen total desde el inicio"
            else ->
                "Sin cambios registrados aún"
        }
    }

    private fun resolveTrendColor(trend: Float): Int {
        return when {
            trend > 0f -> Color.parseColor("#22C55E")
            trend < 0f -> Color.parseColor("#EF4444")
            else -> Color.parseColor("#9CA3AF")
        }
    }

    // Configura el grafico de linea para mostrar el progreso del ejercicio
    private fun setupExerciseChart(chart: LineChart, stat: ExerciseProgressStat) {
        val entries = stat.pesosPorSerie.mapIndexed { index, peso ->
            Entry((index + 1).toFloat(), peso)
        }

        // Etiquetas del eje X: "Inicio" al principio, "Hoy" al final
        val labels = when (stat.pesosPorSerie.size) {
            0 -> listOf("Sin datos")
            1 -> listOf("Inicio")
            2 -> listOf("Inicio", "Hoy")
            else -> listOf("Inicio") +
                    (2 until stat.pesosPorSerie.size).map { "Cambio $it" } +
                    listOf("Hoy")
        }

        chart.description.isEnabled = false
        chart.setDrawGridBackground(false)
        chart.legend.isEnabled = false
        chart.setTouchEnabled(false)
        chart.setPinchZoom(false)
        chart.setScaleEnabled(false)
        chart.setViewPortOffsets(40f, 20f, 20f, 40f)

        chart.xAxis.apply {
            position = XAxis.XAxisPosition.BOTTOM
            textColor = Color.WHITE
            setDrawGridLines(false)
            granularity = 1f
            valueFormatter = IndexAxisValueFormatter(labels)
        }

        chart.axisLeft.apply {
            textColor = Color.WHITE
            setDrawGridLines(true)
            gridColor = Color.GRAY
        }

        chart.axisRight.isEnabled = false

        val lineColor = resolveTrendColor(stat.tendenciaPeso)
        val dataSet = LineDataSet(entries, "Peso").apply {
            color = lineColor
            valueTextColor = Color.WHITE
            lineWidth = 3f
            setCircleColor(lineColor)
            circleRadius = 4f
            setDrawFilled(true)
            fillColor = lineColor
            fillAlpha = 35
        }

        chart.data = LineData(dataSet)
        chart.invalidate()
    }

    private fun formatDecimal(value: Float): String {
        return if (value % 1f == 0f) {
            value.toInt().toString()
        } else {
            String.format("%.1f", value)
        }
    }
}
