package jp.hotdrop.stepcountapp.ui.tab

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.viewpager.widget.ViewPager
import jp.hotdrop.stepcountapp.R
import jp.hotdrop.stepcountapp.common.toFormatWithComma
import jp.hotdrop.stepcountapp.di.ViewModelFactory
import jp.hotdrop.stepcountapp.di.component.component
import jp.hotdrop.stepcountapp.model.DailyStepCount
import jp.hotdrop.stepcountapp.services.GoogleFit
import jp.hotdrop.stepcountapp.ui.adapter.DateViewPagerAdapter
import kotlinx.android.synthetic.main.fragment_google_fit.*
import org.threeten.bp.ZonedDateTime
import timber.log.Timber
import javax.inject.Inject

class GoogleFitFragment : Fragment() {

    @Inject
    lateinit var factory: ViewModelFactory<GoogleFit>
    private val googleFit: GoogleFit by viewModels { factory }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_google_fit, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        component.inject(this)

        initView()
        observe()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode != Activity.RESULT_OK) {
            return
        }

        if (requestCode == GOOGLE_FIT_PERMISSION_REQUEST_CODE) {
            Timber.d("onActivityResultからsignInメソッド呼ぶ")
            googleFit.signIn(requireContext())
        }
    }

    private fun initView() {
        preparedGoogleSignIn()
        initViewPager(ZonedDateTime.now())

        info_button.setOnClickListener {
            google_fit_disable_desc_card_view.isVisible = false
            info_card_view.isVisible = !info_card_view.isVisible
        }
        disable_desc_button.setOnClickListener {
            info_card_view.isVisible = false
            google_fit_disable_desc_card_view.isVisible = !google_fit_disable_desc_card_view.isVisible
        }
    }

    private fun observe() {
        googleFit.signIn.observe(viewLifecycleOwner, Observer {
            googleFit.registerTodayCount(requireContext())
        })
        googleFit.counter.observe(viewLifecycleOwner, Observer {
            initStepCountView(it)
        })
        lifecycle.addObserver(googleFit)
    }

    private fun preparedGoogleSignIn() {
        if (!googleFit.hasPermissions(requireContext())) {
            googleFit.requestPermissions(requireActivity(), GOOGLE_FIT_PERMISSION_REQUEST_CODE)
        } else {
            googleFit.signIn(requireContext())
        }
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
                    if (isSelectToday(date)) {
                        googleFit.registerTodayCount(requireContext())
                    } else {

                        googleFit.findPastStepCount(requireContext(), date)
                    }
                }
                override fun onPageScrollStateChanged(state: Int) { /** no op  */ }
                override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) { /** no op  */ }
            })

            it.setPageTransformer(false) { page, position ->
                val pageWidth: Int = it.measuredWidth - it.paddingLeft - it.paddingRight
                val transformPos = (page.left - (it.scrollX + it.paddingLeft)).toFloat() / pageWidth

                when {
                    transformPos == -1f -> page.alpha = 0.5f
                    -1f < transformPos && transformPos < 0f -> page.alpha = position + 1f
                    transformPos == 0f -> page.alpha = 1f
                    0f < transformPos && transformPos < 1f -> page.alpha = 2f - position
                    transformPos == 1f -> page.alpha = 0.5f
                }
            }
        }
    }

    private fun initStepCountView(dailyStepCount: DailyStepCount) {
        hideLoading()
        step_counter.text = dailyStepCount.stepNum.toFormatWithComma()
        distance_label.text = getString(R.string.google_fit_screen_distance_label, dailyStepCount.distance.toFormatWithComma())
    }

    private fun isSelectToday(targetAt: ZonedDateTime): Boolean {
        val now = ZonedDateTime.now()
        return now.year == targetAt.year && now.monthValue == targetAt.monthValue && now.dayOfMonth == targetAt.dayOfMonth
    }

    private fun visibleLoading() {
        step_counter.isInvisible = true
        progress_bar.isVisible = true
    }

    private fun hideLoading() {
        step_counter.isVisible = true
        distance_label.isVisible = true
        progress_bar.isVisible = false
    }

    companion object {
        private const val GOOGLE_FIT_PERMISSION_REQUEST_CODE = 10000
    }
}
