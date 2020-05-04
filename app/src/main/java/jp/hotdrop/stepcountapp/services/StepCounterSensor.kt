package jp.hotdrop.stepcountapp.services

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.SystemClock
import androidx.core.content.getSystemService
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import dagger.Reusable
import jp.hotdrop.stepcountapp.common.toZonedDateTime
import jp.hotdrop.stepcountapp.model.Accuracy
import jp.hotdrop.stepcountapp.repository.AppSettingRepository
import timber.log.Timber
import javax.inject.Inject

@Reusable
class StepCounterSensor @Inject constructor(
    context: Context,
    private val repository: AppSettingRepository
) {
    private val mutableAccuracy = MutableLiveData<Accuracy>()
    val accuracy: LiveData<Accuracy> = mutableAccuracy

    private val mutableCounter = MutableLiveData<Long>()
    val counter: LiveData<Long> = mutableCounter

    private val mutableCounterFromOS = MutableLiveData<Long>()
    val counterFromOS: LiveData<Long> = mutableCounterFromOS

    private val sensorManager: SensorManager? = context.getSystemService()
    private val sensor: Sensor? = sensorManager?.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)
    private val rebootDateTimeEpoch: Long = System.currentTimeMillis() - SystemClock.elapsedRealtime()

    /**
     * 端末が保持する歩数値を取得するリスナー
     */
    private val stepCountEventListener = object : SensorEventListener {
        override fun onSensorChanged(event: SensorEvent?) {
            event?.values?.get(0)?.toLong()?.let { stepCounter ->
                val calcCounter = calcEffectiveStepCount(stepCounter)
                mutableCounter.postValue(calcCounter)
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

    fun unregisterListener() {
        Timber.d("リスナーを解除します")
        sensorManager?.unregisterListener(stepCountEventListener)
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
     * 有効なアプリ内歩数を取得する
     */
    private fun calcEffectiveStepCount(stepCounter: Long): Long {
        var firstCounter = repository.getAppStartFirstCounter()
        if (firstCounter == 0L) {
            Timber.d("アプリの初回起動時に1度のみ実行: カウンターを初期化")
            repository.initAppStartFirstTime(stepCounter, rebootDateTimeEpoch)
            firstCounter = stepCounter
        }

        // 端末リブートの初回1度だけ計算方法を変える必要があるので分岐している
        if (isRebootAtFirstTime()) {
            val lastCurrentStepCounter = repository.getStepCounter()
            Timber.d("端末リブート後、初回のみ実行: 最後に保存した歩数($lastCurrentStepCounter)を保存")
            repository.updateInfoAfterReboot(lastCurrentStepCounter, rebootDateTimeEpoch)
        }

        Timber.d("歩数計算 OS歩数=$stepCounter アプリ起動時歩数=$firstCounter")
        val currentCounter = if (repository.isReboot) {
            // センサー歩数 + アプリ起動時の歩数
            Timber.d("アプリを起動して歩数カウントを始めた後、端末リブートを1回以上している=歩数を加算")
            stepCounter + firstCounter
        } else {
            // センサー歩数 - 最初の歩数
            Timber.d("アプリを起動してから一度も端末リブートしていない=差し引きで計算")
            stepCounter - firstCounter
        }
        Timber.d("歩数計算結果=$currentCounter")

        repository.saveStepCounter(currentCounter)
        mutableCounterFromOS.postValue(stepCounter)

        return currentCounter
    }

    private fun isRebootAtFirstTime(): Boolean {
        val previousRebootEpoch = repository.getInitAfterRebootDateTimeEpoch()
        Timber.d("リブート日は${rebootDateTimeEpoch.toZonedDateTime()} 前回ブート日は${previousRebootEpoch.toZonedDateTime()}")
        return rebootDateTimeEpoch > previousRebootEpoch
    }

    companion object {
        enum class SensorStatus { OK, NOT_SENSOR_MANAGER, NOT_SENSOR_TYPE_ON_DEVICES }
    }
}