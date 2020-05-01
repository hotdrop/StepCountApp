package jp.hotdrop.stepcountapp.repository

import jp.hotdrop.stepcountapp.repository.local.SharedPrefs
import javax.inject.Inject

class AppSettingRepository @Inject constructor(
    private val sharedPrefs: SharedPrefs
) {
    fun getStepCounter(): Long = sharedPrefs.stepCounterSensor
    fun saveStepCounter(count: Long) {
        sharedPrefs.stepCounterSensor = count
    }

    fun getAppStartFirstCounter(): Long = sharedPrefs.appStartFirstCounter
    fun saveAppStartFirstCounter(count: Long) {
        sharedPrefs.appStartFirstCounter = count
    }

    fun getAppStartStepCounterDateTimeEpoch(): Long = sharedPrefs.startStepCounterDateTime
    fun saveAppStartStepCounterDateTimeEpoch() {
        sharedPrefs.startStepCounterDateTime = System.currentTimeMillis()
    }

    fun getInitAfterRebootDateTimeEpoch(): Long = sharedPrefs.initStepCounterAfterRebootDateTime
    fun saveInitAfterRebootDateTimeEpoch() {
        sharedPrefs.initStepCounterAfterRebootDateTime = System.currentTimeMillis()
    }
}