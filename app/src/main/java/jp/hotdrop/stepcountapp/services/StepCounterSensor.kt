package jp.hotdrop.stepcountapp.services

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.SystemClock
import androidx.core.content.getSystemService
import androidx.lifecycle.*
import jp.hotdrop.stepcountapp.common.toStartDateTime
import jp.hotdrop.stepcountapp.common.toZonedDateTime
import jp.hotdrop.stepcountapp.model.Accuracy
import jp.hotdrop.stepcountapp.model.DailyStepCount
import jp.hotdrop.stepcountapp.model.DeviceDetail
import jp.hotdrop.stepcountapp.repository.AppSettingRepository
import jp.hotdrop.stepcountapp.repository.StepCounterRepository
import jp.hotdrop.stepcountapp.ui.BaseViewModel
import kotlinx.coroutines.*
import org.threeten.bp.ZonedDateTime
import timber.log.Timber
import javax.inject.Inject

/**
 * 問題点
 * 再起動した当日の歩数は0になってしまうのでカウントできていない
 * updateInfoAfterRebootを呼ぶ条件に入った時に今日の歩数を取得してそれを最初の1回だけ加算してやれば行ける。
 * が、バグが発生する可能性の方が大きいので一旦やめ
 */
class StepCounterSensor @Inject constructor(
    context: Context,
    private val appSettingRepository: AppSettingRepository,
    private val stepCounterRepository: StepCounterRepository
) : BaseViewModel() {

    private val mutableDailyStepCount = MutableLiveData<DailyStepCount>()
    val dailyStepCounter: LiveData<DailyStepCount> = mutableDailyStepCount

    private val mutableAccuracy = MutableLiveData<Accuracy>()
    val accuracy: LiveData<Accuracy> = mutableAccuracy

    private val mutableDeviceDetail = MutableLiveData<DeviceDetail>()
    val deviceDetail: LiveData<DeviceDetail> = mutableDeviceDetail.distinctUntilChanged()

    private val sensorManager: SensorManager? = context.getSystemService()
    private val sensor: Sensor? = sensorManager?.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)
    private val rebootDateTimeEpoch: Long = System.currentTimeMillis() - SystemClock.elapsedRealtime()

    private var enableTodayStepCountToLiveData: Boolean = true

    /**
     * 端末が保持する歩数値を取得するリスナー
     */
    private val stepCountEventListener = object : SensorEventListener {
        override fun onSensorChanged(event: SensorEvent?) {
            event?.values?.get(0)?.toLong()?.let { stepCounter ->
                calcEffectiveStepCount(stepCounter)
            }
        }
        override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
            when (accuracy) {
                SensorManager.SENSOR_STATUS_ACCURACY_HIGH -> mutableAccuracy.postValue(Accuracy.High)
                SensorManager.SENSOR_STATUS_ACCURACY_MEDIUM -> mutableAccuracy.postValue(Accuracy.Medium)
                SensorManager.SENSOR_STATUS_ACCURACY_LOW -> mutableAccuracy.postValue(Accuracy.Low)
                SensorManager.SENSOR_STATUS_UNRELIABLE -> mutableAccuracy.postValue(Accuracy.Unreliable)
                else -> mutableAccuracy.postValue(Accuracy.NoContact)
            }
        }
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_CREATE)
    fun onCreate() {
        var previousRebootEpoch = appSettingRepository.initAfterRebootDateTimeEpoch
        Timber.d("端末から取得した再起動日: ${rebootDateTimeEpoch.toZonedDateTime()} アプリで持っている前回の再起動日: ${previousRebootEpoch.toZonedDateTime()}")

        // calcEffectiveStepCountでも同じ判定を行なっているが、あっちは歩数センサーが反応するたびに動く。
        // この時刻判定処理はアプリ起動時に1回やれば良いのでここでやっている。
        if (appSettingRepository.appStartFirstCounter == 0L) {
            Timber.d("アプリの初回起動時に1度のみ実行。端末再起動時間を保持")
            appSettingRepository.initAppStartFirstTime(rebootDateTimeEpoch)
            previousRebootEpoch = rebootDateTimeEpoch
        }

        // 端末再起動後の初回1度だけ実行しカウンタの状態を保持する。それ以降は次回の端末再起動時までこの処理は通さない
        if (rebootDateTimeEpoch > previousRebootEpoch) {
            Timber.d("端末リブート後、初回のみ実行")
            appSettingRepository.updateInfoAfterReboot(rebootDateTimeEpoch)
        }
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    fun dispose() {
        unregisterListener()
        coroutineContext.cancel()
    }

    fun registerListener(): Boolean {
        if (!isAvailable()) {
            Timber.d("歩行センサーのregisterListenerの結果がfalseでした。")
            return false
        }

        Timber.d("リスナーを登録します")
        return sensorManager?.registerListener(stepCountEventListener,
            sensor,
            SensorManager.SENSOR_DELAY_NORMAL,
            1_000_000) ?: false
    }

    private fun unregisterListener() {
        Timber.d("リスナーを解除します")
        sensorManager?.unregisterListener(stepCountEventListener)
    }

    fun onLoadPastStepCount(targetAt: ZonedDateTime) {
        launch {
            enableTodayStepCountToLiveData = targetAt.isAfter(ZonedDateTime.now().toStartDateTime())
            Timber.d("$targetAt の歩数をロードします。今日の日付を選択していますか？ = $enableTodayStepCountToLiveData")
            findStepCount(targetAt)
        }
    }

    private fun isAvailable(): Boolean {
        return availableStatus() == SensorStatus.OK
    }

    private fun availableStatus(): SensorStatus {
        return when {
            sensorManager == null -> SensorStatus.NOT_SENSOR_MANAGER
            sensor == null -> SensorStatus.NOT_SENSOR_TYPE_ON_DEVICES
            else -> SensorStatus.OK
        }
    }

    /**
     * 有効なアプリ内歩数を計算してLiveDataに流す。
     */
    private fun calcEffectiveStepCount(stepCounterFromOS: Long) {
        var firstCounter = appSettingRepository.appStartFirstCounter
        if (firstCounter == 0L) {
            Timber.d("アプリの初回起動時に1度のみ実行。カウンターを初期化")
            appSettingRepository.initOSStepCount(stepCounterFromOS)
            firstCounter = stepCounterFromOS
        }

        if (appSettingRepository.isReboot) {
            calcStepCountAfterRebootDevice(stepCounterFromOS)
        } else {
            calcStepCountBeforeRebootDevice(stepCounterFromOS, firstCounter)
        }

        val detail = stepCounterRepository.getDeviceDetail(stepCounterFromOS)
        mutableDeviceDetail.postValue(detail)
    }

    private fun calcStepCountAfterRebootDevice(stepCountFromOS: Long) {
        launch {
            Timber.d("アプリを起動してから端末再起動を1回以上している。")
            val prevStepNum = stepCounterRepository.rangeStepCountNumPreviousDate(rebootDateTimeEpoch.toZonedDateTime())
            Timber.d("OSから受け取った歩数=$stepCountFromOS 再起動後から前日までの歩数=$prevStepNum これの差し引きが有効歩数")
            val effectiveCount = stepCountFromOS - prevStepNum

            Timber.d("　　有効歩数=$effectiveCount")
            stepCounterRepository.save(effectiveCount)

            // 画面で選択している日が今日の場合のみ流す
            if (enableTodayStepCountToLiveData) {
                findStepCount(ZonedDateTime.now())
            }
        }
    }

    private fun calcStepCountBeforeRebootDevice(stepCountFromOS: Long, appFirstStartStepCount: Long) {
        launch {
            Timber.d("アプリを起動してから一度も端末再起動していない。")
            val prevStepNum = stepCounterRepository.totalStepCountNumPreviousDate()
            Timber.d("OSから受け取った歩数=$stepCountFromOS 前日までの歩数=$prevStepNum アプリ起動時歩数=$appFirstStartStepCount")
            val effectiveCount = (stepCountFromOS - appFirstStartStepCount) - prevStepNum

            Timber.d("　　有効歩数=$effectiveCount")
            stepCounterRepository.save(effectiveCount)

            // 画面で選択している日が今日の場合のみ流す
            if (enableTodayStepCountToLiveData) {
                findStepCount(ZonedDateTime.now())
            }
        }
    }

    private suspend fun findStepCount(targetAt: ZonedDateTime) {
        // 歩数がない日も画面表示したいのでカウント0としてLiveDataに投げる。
        val dailyStepCount = stepCounterRepository.find(targetAt) ?: DailyStepCount(stepNum = 0, dayAt = targetAt)
        Timber.d("この日のDBから取得した歩数=$dailyStepCount")
        mutableDailyStepCount.postValue(dailyStepCount)
    }

    companion object {
        enum class SensorStatus { OK, NOT_SENSOR_MANAGER, NOT_SENSOR_TYPE_ON_DEVICES }
    }
}