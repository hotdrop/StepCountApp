package jp.hotdrop.stepcountapp.ui.googlefit

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import jp.hotdrop.stepcountapp.R
import jp.hotdrop.stepcountapp.common.toFormatWithComma
import jp.hotdrop.stepcountapp.di.ViewModelFactory
import jp.hotdrop.stepcountapp.di.component.component
import jp.hotdrop.stepcountapp.services.GoogleFit
import jp.hotdrop.stepcountapp.ui.MainViewModel
import kotlinx.android.synthetic.main.fragment_google_fit.*
import timber.log.Timber
import javax.inject.Inject

class GoogleFitFragment : Fragment() {

    @Inject
    lateinit var viewModelFactory: ViewModelFactory<MainViewModel>
    private val viewModel: MainViewModel by activityViewModels { viewModelFactory }

    @Inject
    lateinit var googleFit: GoogleFit

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val root = inflater.inflate(R.layout.fragment_google_fit, container, false)
        component?.inject(this)
        return root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        preparedGoogleSignIn()
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

    private fun observe() {
        googleFit.signIn.observe(viewLifecycleOwner, Observer {
            initAccountInfo(it)
        })
        googleFit.counter.observe(viewLifecycleOwner, Observer {
            initStepCountView(it)
        })
    }

    private fun preparedGoogleSignIn() {
        if (!googleFit.hasPermissions(requireContext())) {
            googleFit.requestPermissions(requireActivity(), GOOGLE_FIT_PERMISSION_REQUEST_CODE)
        } else {
            googleFit.signIn(requireContext())
        }
    }

    private fun initAccountInfo(account: GoogleSignInAccount?) {
        if (account != null) {
            Timber.d("GoogleFitとの連携を有効にしました。")
            google_fit_access_status.text = getString(R.string.google_fit_screen_auth_enable_status)
            enable_button.text = getString(R.string.google_fit_screen_disable_button)
            enable_button.setOnClickListener {
                googleFit.signOut(requireContext())
            }
            googleFit.registerTodayCount(requireContext())
        } else {
            Timber.d("GoogleFitとの連携を無効にしました。")
            google_fit_access_status.text = getString(R.string.google_fit_screen_auth_disable_status)
            enable_button.text = getString(R.string.google_fit_screen_enable_button)
            enable_button.setOnClickListener {
                preparedGoogleSignIn()
            }
        }
    }

    private fun initStepCountView(count: Int) {
        hideLoading()
        // 日付の下の概要
//        if (isSelectToday(dailyStepCount.dayAt)) {
//            overview.text = getString(R.string.device_screen_overview_today)
//        } else {
//            overview.text = getString(R.string.device_screen_overview_past)
//        }

        // 歩数
        step_counter.text = count.toLong().toFormatWithComma()
    }

    private fun visibleLoading() {
        step_counter.isInvisible = true
        progress_bar.isVisible = true
    }

    private fun hideLoading() {
        step_counter.isVisible = true
        progress_bar.isVisible = false
    }

    companion object {
        private const val GOOGLE_FIT_PERMISSION_REQUEST_CODE = 10000
    }
}
