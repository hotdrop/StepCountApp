package jp.hotdrop.stepcountapp.ui.dashboard

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import jp.hotdrop.stepcountapp.common.toLongYearMonthDay
import jp.hotdrop.stepcountapp.model.ChartItem
import jp.hotdrop.stepcountapp.model.DailyStepCount
import jp.hotdrop.stepcountapp.model.StepCountChartData
import jp.hotdrop.stepcountapp.repository.GoogleFitRepository
import jp.hotdrop.stepcountapp.repository.StepCounterRepository
import jp.hotdrop.stepcountapp.ui.BaseViewModel
import kotlinx.coroutines.launch
import org.threeten.bp.ZonedDateTime
import timber.log.Timber
import javax.inject.Inject

class DashboardViewModel @Inject constructor(
    private val stepCounterRepository: StepCounterRepository,
    private val googleFitRepository: GoogleFitRepository
) : BaseViewModel() {

    private val mutableChartData = MutableLiveData<StepCountChartData>()
    val chartData: LiveData<StepCountChartData> = mutableChartData

    /**
     * 端末歩行センサーの歩数取得
     */
    fun onLoadStepCountData(baseAt: ZonedDateTime, type: PeriodType) {
        launch {
            when (type) {
                PeriodType.DAY -> {
                    val dailyStepCountList = stepCounterRepository.findRange(baseAt.minusDays(DAYS_NUM), baseAt)
                    onLoadByDay(baseAt, dailyStepCountList)
                }
                PeriodType.WEEK -> { /** TODO 未実装 */}
            }
        }
    }

    /**
     * Google Fitの歩数取得
     */
    fun onLoadGoogleFitData(baseAt: ZonedDateTime, type: PeriodType) {
        launch {
            when (type) {
                PeriodType.DAY -> {
                    val dailyStepCountList = googleFitRepository.findRange(baseAt.minusDays(DAYS_NUM), baseAt)
                    onLoadByDay(baseAt, dailyStepCountList)
                }
                PeriodType.WEEK -> { /** TODO 未実装 */}
            }
        }
    }

    private fun onLoadByDay(baseAt: ZonedDateTime, dailyStepCountList: List<DailyStepCount>) {
        val days = rangeDayList(baseAt)
        Timber.d("日付リスト[${days}]")
        val chartData = StepCountChartData(
            memoryCount = DAYS_NUM,
            items = days.map { ymdKey ->
                dailyStepCountList.find { it.ymdId == ymdKey }?.let {
                    ChartItem(stepNum = it.stepNum, ymd = ymdKey)
                } ?: ChartItem(stepNum = 0, ymd = ymdKey)
            }
        )
        mutableChartData.postValue(chartData)
    }

    private fun rangeDayList(baseAt: ZonedDateTime): List<Long> {
        return (0L until DAYS_NUM).map { baseAt.minusDays(it).toLongYearMonthDay() }.reversed()
    }

    enum class PeriodType { DAY, WEEK }

    companion object {
        private const val DAYS_NUM: Long = 7
    }
}