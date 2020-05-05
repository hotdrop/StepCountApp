package jp.hotdrop.stepcountapp.repository

import dagger.Reusable
import jp.hotdrop.stepcountapp.repository.local.SharedPrefs
import javax.inject.Inject

@Reusable
class AppSettingRepository @Inject constructor(
    private val sharedPrefs: SharedPrefs
) {
    fun initAppStartFirstTime(previousRebootEpoch: Long) {
        sharedPrefs.isReboot = false
        sharedPrefs.initStepCounterAfterRebootDateTime = previousRebootEpoch
    }

    fun initOSStepCount(firstStepCount: Long) {
        sharedPrefs.appStartDeviceCounter = firstStepCount
    }

    fun updateInfoAfterReboot(rebootEpoch: Long) {
        sharedPrefs.isReboot = true
        sharedPrefs.initStepCounterAfterRebootDateTime = rebootEpoch
    }

    val isReboot: Boolean
        get() = sharedPrefs.isReboot

    val appStartFirstCounter: Long
        get() = sharedPrefs.appStartDeviceCounter

    val initAfterRebootDateTimeEpoch: Long
        get() = sharedPrefs.initStepCounterAfterRebootDateTime

}