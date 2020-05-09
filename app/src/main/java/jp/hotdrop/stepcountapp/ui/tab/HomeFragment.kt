package jp.hotdrop.stepcountapp.ui.tab

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.viewpager.widget.ViewPager
import jp.hotdrop.stepcountapp.R
import jp.hotdrop.stepcountapp.common.Formatter
import jp.hotdrop.stepcountapp.common.toFormatWithComma
import jp.hotdrop.stepcountapp.di.ViewModelFactory
import jp.hotdrop.stepcountapp.di.component.component
import jp.hotdrop.stepcountapp.model.Accuracy
import jp.hotdrop.stepcountapp.model.DailyStepCount
import jp.hotdrop.stepcountapp.model.DeviceDetail
import jp.hotdrop.stepcountapp.services.StepCounterSensor
import jp.hotdrop.stepcountapp.ui.adapter.DateViewPagerAdapter
import kotlinx.android.synthetic.main.fragment_home.*
import org.threeten.bp.ZonedDateTime
import javax.inject.Inject

class HomeFragment: Fragment() {

    @Inject
    lateinit var factory: ViewModelFactory<StepCounterSensor>
    private val stepCounterSensor: StepCounterSensor by activityViewModels { factory }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        component.inject(this)

        initViewPager(ZonedDateTime.now())
        observe()
    }

    private fun observe() {
        stepCounterSensor.dailyStepCounter.observe(viewLifecycleOwner, Observer {
            initStepCountView(it)
        })
        stepCounterSensor.accuracy.observe(viewLifecycleOwner, Observer {
            initAccuracy(it)
        })
        stepCounterSensor.deviceDetail.observe(viewLifecycleOwner, Observer {
            initDetail(it)
        })
    }

    private fun initStepCountView(dailyStepCount: DailyStepCount) {
        hideLoading()
        step_counter.text = dailyStepCount.stepNum.toFormatWithComma()
    }

    private fun initViewPager(currentAt: ZonedDateTime) {
        val viewPagerDayList = DateViewPagerAdapter.createSelectedList(currentAt)
        date_view_pager.let {
            it.adapter = DateViewPagerAdapter(currentAt, viewPagerDayList)
            it.clearOnPageChangeListeners()
            it.currentItem = viewPagerDayList.indexOf(0L)
            it.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
                override fun onPageSelected(position: Int) {
                    val date = currentAt.plusDays(viewPagerDayList[position])
                    stepCounterSensor.onLoadPastStepCount(date)
                }
                override fun onPageScrollStateChanged(state: Int) { /** no op  */ }
                override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) { /** no op  */ }
            })
        }
    }

    private fun initAccuracy(accuracy: Accuracy) {
        sensor_accuracy.text = getString(accuracy.labelResId)
        sensor_accuracy.setTextColor(ContextCompat.getColor(requireContext(), accuracy.colorResId))
    }

    private fun initDetail(deviceDetail: DeviceDetail) {
        step_count_in_os.text = deviceDetail.stepCounterFromOS.toFormatWithComma()
        step_count_by_reboot.text = deviceDetail.appStartFirstCounter.toFormatWithComma()
        device_screen_os_reboot_date.text = deviceDetail.getInitAfterRebootDateTime()?.format(Formatter.ofDateTime) ?: getString(R.string.device_screen_os_reboot_non_date)

        info_button.setOnClickListener {
            attention_card_view.isVisible = false
            info_card_view.isVisible = !info_card_view.isVisible
        }
        sensor_hint_button.setOnClickListener {
            sensor_hint_description.isVisible = !sensor_hint_description.isVisible
        }
        attention_button.setOnClickListener {
            info_card_view.isVisible = false
            attention_card_view.isVisible = !attention_card_view.isVisible
        }
    }

    private fun visibleLoading() {
        step_counter.isInvisible = true
        progress_bar.isVisible = true
    }

    private fun hideLoading() {
        step_counter.isVisible = true
        progress_bar.isVisible = false
    }
}
