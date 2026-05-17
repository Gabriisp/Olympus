package com.example.olympus

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
        holder.tvTrend.text = buildTrendLabel(stat.tendenciaPeso)
        holder.tvTrend.setTextColor(resolveTrendColor(stat.tendenciaPeso))
        holder.tvSummary.text =
            "Series: ${stat.totalSeries} · Max: ${formatDecimal(stat.maxPeso)} kg · Reps máx: ${stat.maxRepeticiones} · Media: ${formatDecimal(stat.promedioPeso)} kg"
        holder.tvVolume.text =
            "Volumen total: ${formatDecimal(stat.totalVolumen)} kg · Repeticiones acumuladas: ${stat.totalRepeticiones}"
        setupExerciseChart(holder.chart, stat)
    }

    override fun getItemCount(): Int = stats.size

    fun updateStats(newStats: List<ExerciseProgressStat>) {
        stats = newStats
        notifyDataSetChanged()
    }

    private fun buildTrendLabel(trend: Float): String {
        return when {
            trend > 0f -> "Mejora: +${formatDecimal(abs(trend))} kg desde la primera serie"
            trend < 0f -> "Baja: -${formatDecimal(abs(trend))} kg desde la primera serie"
            else -> "Sin cambio de peso entre el inicio y el final"
        }
    }

    private fun resolveTrendColor(trend: Float): Int {
        return when {
            trend > 0f -> Color.parseColor("#22C55E")
            trend < 0f -> Color.parseColor("#EF4444")
            else -> Color.parseColor("#9CA3AF")
        }
    }

    private fun setupExerciseChart(chart: LineChart, stat: ExerciseProgressStat) {
        val entries = stat.pesosPorSerie.mapIndexed { index, peso ->
            Entry((index + 1).toFloat(), peso)
        }
        val labels = stat.pesosPorSerie.indices.map { "S${it + 1}" }

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
