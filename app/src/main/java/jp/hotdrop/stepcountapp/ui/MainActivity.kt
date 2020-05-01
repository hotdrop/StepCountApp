package jp.hotdrop.stepcountapp.ui

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import jp.hotdrop.stepcountapp.R
import jp.hotdrop.stepcountapp.di.ViewModelFactory
import jp.hotdrop.stepcountapp.di.component.component
import jp.hotdrop.stepcountapp.model.StepCounterSensor
import permissions.dispatcher.*
import javax.inject.Inject

@RuntimePermissions
class MainActivity: AppCompatActivity() {

    @Inject
    lateinit var viewModelFactory: ViewModelFactory<MainViewModel>
    private val viewModel: MainViewModel by viewModels { viewModelFactory }

    @Inject
    lateinit var stepCounterSensor: StepCounterSensor

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        component.inject(this)

        initView()
        observe()
    }

    override fun onResume() {
        super.onResume()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            initStepSensorViewWithPermissionCheck()
        } else {
            initStepSensorView()
        }
    }

    override fun onStop() {
        super.onStop()
        stepCounterSensor.unregisterListener()
    }

    private fun initView() {
        val navView: BottomNavigationView = findViewById(R.id.nav_view)
        val navController = findNavController(R.id.nav_host_fragment)
        val appBarConfiguration = AppBarConfiguration(setOf(
            R.id.navigation_home,
            R.id.navigation_google_fit,
            R.id.navigation_dashboard
        ))
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)
    }

    private fun observe() {
        stepCounterSensor.counter.observe(this, Observer {
            viewModel.calcTodayCount(it)
        })
        stepCounterSensor.accuracy.observe(this, Observer {
            viewModel.updateAccuracy(it)
        })
        stepCounterSensor.counterFromOS.observe(this, Observer {
            viewModel.updateCounterInOS(it)
        })
    }

    @NeedsPermission(Manifest.permission.ACTIVITY_RECOGNITION)
    fun initStepSensorView() {
        stepCounterSensor.registerListener()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        onRequestPermissionsResult(requestCode, grantResults)
    }

    @OnShowRationale(Manifest.permission.ACTIVITY_RECOGNITION)
    fun showRationaleForStepCounter(request: PermissionRequest) {
        AlertDialog.Builder(this)
            .setMessage(R.string.permission_show_rationale_message)
            .setPositiveButton(android.R.string.ok) { _, _ ->
                request.proceed()
            }.setCancelable(false)
            .show()
    }

    @OnPermissionDenied(Manifest.permission.ACTIVITY_RECOGNITION)
    fun showPermissionDenied() {
        finish()
    }

    @OnNeverAskAgain(Manifest.permission.ACTIVITY_RECOGNITION)
    fun showNeverAskAgain() {
        AlertDialog.Builder(this)
            .setMessage(R.string.permission_never_ask_again_message)
            .setPositiveButton(android.R.string.ok) { _, _ ->
                finish()
            }.setNegativeButton(R.string.dialog_to_settings_button) { _, _ ->
                val intent = Intent().apply {
                    action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                    data = Uri.fromParts("package", applicationContext.packageName, null)
                }
                startActivity(intent)
                finish()
            }.setCancelable(false)
            .show()
    }
}
