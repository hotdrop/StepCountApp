package jp.hotdrop.stepcountapp.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import jp.hotdrop.stepcountapp.R
import jp.hotdrop.stepcountapp.common.Formatter
import jp.hotdrop.stepcountapp.common.toFormatWithComma
import jp.hotdrop.stepcountapp.di.ViewModelFactory
import jp.hotdrop.stepcountapp.di.component.component
import jp.hotdrop.stepcountapp.model.Accuracy
import jp.hotdrop.stepcountapp.model.DeviceDetail
import jp.hotdrop.stepcountapp.ui.MainViewModel
import kotlinx.android.synthetic.main.fragment_home.*
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
        observe()
    }

    private fun observe() {
        viewModel.todayStepCounter.observe(viewLifecycleOwner, Observer {
            it?.let {
                step_counter.text = it.stepNum.toFormatWithComma()
                now_date.text = it.dayAt.format(Formatter.ofDate)
            }
        })
        viewModel.accuracy.observe(viewLifecycleOwner, Observer {
            initAccuracy(it)
        })
        viewModel.deviceDetail.observe(viewLifecycleOwner, Observer {
            initDetail(it)
        })
    }

    private fun initAccuracy(accuracy: Accuracy) {
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
}
