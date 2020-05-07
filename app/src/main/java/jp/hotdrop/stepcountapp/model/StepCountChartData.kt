package jp.hotdrop.stepcountapp.model

import com.github.mikephil.charting.data.BarEntry

data class StepCountChartData(
    val memoryCount: Long,
    val items: List<ChartItem>
) {
    fun createTitle(): String {
        val rangeYear = items.map { it.year }.distinct()
        val rangeMonth = items.map { it.month }.distinct()

        return when {
            rangeYear.size > 1 -> String.format("%d年%d月〜%d年%d月", rangeYear.first(), rangeMonth.first(), rangeYear.last(), rangeMonth.last())
            rangeMonth.size > 1 -> String.format("%d年\n%d月〜％d月", rangeYear.first(), rangeMonth.first(), rangeMonth.last())
            else -> String.format("%d年\n%d月", rangeYear.first(), rangeMonth.first())
        }
    }

    fun createEntries(): List<BarEntry> {
        return items.mapIndexed { index, item ->
            BarEntry(index.toFloat(), item.stepNum.toFloat())
        }
    }

    fun createDayLabels(): List<String> {
        return items.map { String.format("%d日", it.day) }
    }
}

data class ChartItem(
    val stepNum: Long,
    private val ymd: Long
) {
    val year: Int
    val month: Int
    val day: Int

    init {
        val ymdString = ymd.toString()
        year = ymdString.substring(0, 4).toInt()
        month = ymdString.substring(4, 6).toInt()
        day = ymdString.substring(6, 8).toInt()
    }
}