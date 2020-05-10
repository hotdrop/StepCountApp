package jp.hotdrop.stepcountapp.ui.dashboard

import android.content.res.Configuration
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.github.mikephil.charting.animation.Easing
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.formatter.ValueFormatter
import jp.hotdrop.stepcountapp.R
import jp.hotdrop.stepcountapp.di.ViewModelFactory
import jp.hotdrop.stepcountapp.di.component.component
import jp.hotdrop.stepcountapp.model.StepCountChartData
import kotlinx.android.synthetic.main.fragment_dashboard.*
import org.threeten.bp.ZonedDateTime
import javax.inject.Inject

class DashboardFragment: Fragment() {

    @Inject
    lateinit var factory: ViewModelFactory<DashboardViewModel>
    private val viewModel: DashboardViewModel by viewModels { factory }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_dashboard, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        component.inject(this)

        initView()
        observe()
    }

    private fun initView() {
        // 初期は通常のセンサー
        viewModel.onLoadStepCountData(ZonedDateTime.now(), DashboardViewModel.PeriodType.DAY)

        step_counter_toggle_button.setOnClickListener {
            viewModel.onLoadStepCountData(ZonedDateTime.now(), DashboardViewModel.PeriodType.DAY)
        }
        google_fit_toggle_button.setOnClickListener {
            viewModel.onLoadGoogleFitData(ZonedDateTime.now(), DashboardViewModel.PeriodType.DAY)
        }
    }

    private fun observe() {
        viewModel.chartData.observe(viewLifecycleOwner, Observer {
            onLoadChart(it)
        })
        lifecycle.addObserver(viewModel)
    }

    private fun onLoadChart(chartData: StepCountChartData) {

        chart_title.text = chartData.createTitle()

        // バーの設定
        val entries = chartData.createEntries()
        val barDataSet = BarDataSet(entries, "歩数").apply {
            // デフォルトのFloat型にすると歩数が小数点表示されて嫌な感じなので整数にしている
            valueFormatter = IntegerValueFormatter()
        }
        setDataColorForDarkTheme(barDataSet)
        bar_chart.data = BarData(barDataSet)

        // 横軸のラベル設定
        setXAxisLabel(chartData)

        // グラフの設定
        bar_chart.run {
            invalidate() // グラフを再描画する
            setFitBars(true) // 端末解像度に合わせてグラフ表示（スクロール出さない）
            setDrawValueAboveBar(true) // バーの上に値を表示
            description.isEnabled = false // 詳細表示をしない
            setScaleEnabled(false) // ズームはさせない
            animateY(300, Easing.Linear) // グラフ表示の際のアニメーション
            setChartColorForDarkTheme()
        }
    }

    private fun setXAxisLabel(chartData: StepCountChartData) {
        bar_chart.xAxis.run {
            position = XAxis.XAxisPosition.BOTTOM
            setDrawLabels(true)
            setDrawGridLines(false)
            setDrawAxisLine(true)
            valueFormatter = IndexAxisValueFormatter(chartData.createDayLabels())
        }
    }

    private fun setDataColorForDarkTheme(barDataSet: BarDataSet) {
        val currentMode = resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
        if (currentMode == Configuration.UI_MODE_NIGHT_YES) {
            val whiteResId = ContextCompat.getColor(requireContext(), R.color.white)
            barDataSet.valueTextColor = whiteResId
        }
    }

    private fun setChartColorForDarkTheme() {
        val currentMode = resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
        if (currentMode == Configuration.UI_MODE_NIGHT_YES) {
            val whiteResId = ContextCompat.getColor(requireContext(), R.color.white)
            bar_chart.xAxis.textColor = whiteResId
            bar_chart.axisLeft.textColor = whiteResId
            bar_chart.axisRight.textColor = whiteResId
            bar_chart.legend.textColor = whiteResId
        }
    }

    class IntegerValueFormatter : ValueFormatter() {
        override fun getFormattedValue(value: Float): String {
            return value.toInt().toString()
        }
    }
}
