package jp.hotdrop.stepcountapp.ui.dashboard

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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

    private var previousCheckedChipId: Int = R.id.chip_step_counter
    private fun initView() {
        // 初期は通常のセンサー
        viewModel.onLoadStepCountData(ZonedDateTime.now(), DashboardViewModel.PeriodType.DAY)

        chip_group.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                R.id.chip_step_counter -> {
                    if (previousCheckedChipId != R.id.chip_step_counter) {
                        previousCheckedChipId = R.id.chip_step_counter
                        viewModel.onLoadStepCountData(ZonedDateTime.now(), DashboardViewModel.PeriodType.DAY)
                    }
                }
                R.id.chip_google_fit -> {
                    if (previousCheckedChipId != R.id.chip_google_fit) {
                        previousCheckedChipId = R.id.chip_google_fit
                        viewModel.onLoadGoogleFitData(ZonedDateTime.now(), DashboardViewModel.PeriodType.DAY)
                    }
                }
                // 本当はselectionRequiredで対応したいがalphaなのでこれで凌ぐ
                else -> chip_group.check(previousCheckedChipId)
            }
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

    class IntegerValueFormatter : ValueFormatter() {
        override fun getFormattedValue(value: Float): String {
            return value.toInt().toString()
        }
    }
}
