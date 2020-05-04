package jp.hotdrop.stepcountapp.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.viewpager.widget.PagerAdapter
import androidx.viewpager.widget.ViewPager
import jp.hotdrop.stepcountapp.R
import jp.hotdrop.stepcountapp.common.Formatter
import jp.hotdrop.stepcountapp.common.toFormatWithComma
import jp.hotdrop.stepcountapp.common.toLongYearMonthDay
import jp.hotdrop.stepcountapp.di.ViewModelFactory
import jp.hotdrop.stepcountapp.di.component.component
import jp.hotdrop.stepcountapp.model.Accuracy
import jp.hotdrop.stepcountapp.model.DailyStepCount
import jp.hotdrop.stepcountapp.model.DeviceDetail
import jp.hotdrop.stepcountapp.ui.MainViewModel
import kotlinx.android.synthetic.main.fragment_home.*
import kotlinx.android.synthetic.main.row_date.view.*
import org.threeten.bp.ZonedDateTime
import timber.log.Timber
import javax.inject.Inject

class HomeFragment: Fragment() {

    @Inject
    lateinit var viewModelFactory: ViewModelFactory<MainViewModel>
    private val viewModel: MainViewModel by activityViewModels { viewModelFactory }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val root = inflater.inflate(R.layout.fragment_home, container, false)
        component?.inject(this)
        return root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        initViewPager(ZonedDateTime.now())
        observe()
    }

    private fun observe() {
        viewModel.dailyStepCounter.observe(viewLifecycleOwner, Observer {
            initStepCountView(it)
        })
        viewModel.accuracy.observe(viewLifecycleOwner, Observer {
            initAccuracy(it)
        })
        viewModel.deviceDetail.observe(viewLifecycleOwner, Observer {
            initDetail(it)
        })
    }

    private fun initStepCountView(dailyStepCount: DailyStepCount) {
        // 日付の下の概要
        if (isSelectToday(dailyStepCount.dayAt)) {
            overview.text = getString(R.string.device_screen_overview_today)
        } else {
            overview.text = getString(R.string.device_screen_overview_past)
        }

        // 歩数
        step_counter.text = dailyStepCount.stepNum.toFormatWithComma()
    }

    private fun isSelectToday(targetAt: ZonedDateTime): Boolean {
        val now = ZonedDateTime.now()
        return now.year == targetAt.year && now.monthValue == targetAt.monthValue && now.dayOfMonth == targetAt.dayOfMonth
    }

    private fun initViewPager(currentAt: ZonedDateTime) {
        val viewPagerDayList = createSelectedList(currentAt)
        date_view_pager.let {
            it.adapter = DateViewPagerAdapter(currentAt, viewPagerDayList)
            it.clearOnPageChangeListeners()
            it.currentItem = viewPagerDayList.indexOf(0L)
            it.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
                override fun onPageSelected(position: Int) {
                    val date = currentAt.plusDays(viewPagerDayList[position])
                    viewModel.findStepCount(date)
                }
                override fun onPageScrollStateChanged(state: Int) { /** no op  */ }
                override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) { /** no op  */ }
            })
        }
    }

    /**
     * 指定された日を中心として前後が等間隔になるよう計dateListSize日をViewPagerに指定する。
     * ただし、先頭は当日までとしリストは現在選択日付の日数増減で表現する。
     */
    private fun createSelectedList(currentAt: ZonedDateTime): List<Long> {
        val nowYMD = ZonedDateTime.now().toLongYearMonthDay()
        val currentYMD = currentAt.toLongYearMonthDay()

        val numDaysAfter = when {
            nowYMD - currentYMD > (dateListSize/2) -> (dateListSize/2).toLong()
            nowYMD - currentYMD < 0 -> 0L
            else -> nowYMD - currentYMD
        }

        val results = mutableListOf<Long>()
        var index = numDaysAfter
        (1..dateListSize).forEach { _ ->
            results.add(0, index--)
        }
        Timber.d("作成した日数リスト: $results")
        return results
    }

    private fun initAccuracy(accuracy: Accuracy) {
        // TODO 表示色変えたい
        val messageResId = when (accuracy) {
            Accuracy.High -> R.string.accuracy_high_message
            Accuracy.Medium -> R.string.accuracy_medium_message
            Accuracy.Low -> R.string.accuracy_low_message
            Accuracy.Unreliable -> R.string.accuracy_unreliable_message
            Accuracy.NoContact -> R.string.accuracy_no_contact_message
        }
        sensor_accuracy.text = getString(messageResId)
    }

    private fun initDetail(deviceDetail: DeviceDetail) {
        step_count_in_os.text = deviceDetail.stepCounterFromOS.toFormatWithComma()
        step_count_by_reboot.text = deviceDetail.appStartFirstCounter.toFormatWithComma()

        device_screen_os_reboot_date.text = deviceDetail.getInitAfterRebootDateTime()?.format(Formatter.ofDateTime)
            ?: getString(R.string.device_screen_os_reboot_non_date)
    }

    /**
     * 日付のViewPagerアダプター
     */
    inner class DateViewPagerAdapter(private val currentAt: ZonedDateTime, private val viewPagerDayList: List<Long>) : PagerAdapter() {

        override fun getCount(): Int = viewPagerDayList.size

        override fun isViewFromObject(view: View, obj: Any): Boolean {
            return view == obj
        }

        override fun destroyItem(container: ViewGroup, position: Int, obj: Any) {
            container.removeView(obj as View)
        }

        override fun instantiateItem(container: ViewGroup, position: Int): Any {
            val currentDate = currentAt.plusDays(viewPagerDayList[position])
            val view = LayoutInflater.from(container.context).inflate(R.layout.row_date, container, false)
            view.show_date.text = currentDate.format(Formatter.ofDate)
            when (position) {
                0 -> {
                    view.arrow_next.isVisible = true
                    view.arrow_prev.isVisible = false
                }
                dateListSize - 1 -> {
                    view.arrow_next.isVisible = false
                    view.arrow_prev.isVisible = true
                }
                else -> {
                    view.arrow_next.isVisible = true
                    view.arrow_prev.isVisible = true
                }
            }
            view.row_date_layout.setOnClickListener {
                // TODO カレンダーを表示する
            }

            container.addView(view)
            return view
        }
    }

    companion object {
        private const val dateListSize = 7
    }
}
