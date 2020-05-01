package jp.hotdrop.stepcountapp.repository

import jp.hotdrop.stepcountapp.repository.local.SharedPrefs
import javax.inject.Inject

class AppSettingRepository @Inject constructor(
    private val sharedPrefs: SharedPrefs
) {

    val isReboot: Boolean = sharedPrefs.isReboot

    fun initAppStartFirstTime(firstStepCount: Long, previousRebootEpoch: Long) {
        sharedPrefs.appStartDeviceCounter = firstStepCount
        sharedPrefs.isReboot = false
        sharedPrefs.initStepCounterAfterRebootDateTime = previousRebootEpoch
    }

    fun updateInfoAfterReboot(lastCurrentStepCounter: Long, rebootEpoch: Long) {
        sharedPrefs.appStartDeviceCounter = lastCurrentStepCounter
        sharedPrefs.isReboot = true
        sharedPrefs.initStepCounterAfterRebootDateTime = rebootEpoch
    }

    fun getStepCounter(): Long = sharedPrefs.stepCounterSensor
    fun saveStepCounter(count: Long) {
        sharedPrefs.stepCounterSensor = count
    }

    fun getAppStartFirstCounter(): Long = sharedPrefs.appStartDeviceCounter
    fun saveAppStartFirstCounter(count: Long) {
        sharedPrefs.appStartDeviceCounter = count
    }

    fun getInitAfterRebootDateTimeEpoch(): Long = sharedPrefs.initStepCounterAfterRebootDateTime
}