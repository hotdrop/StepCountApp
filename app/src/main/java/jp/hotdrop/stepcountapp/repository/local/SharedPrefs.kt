package jp.hotdrop.stepcountapp.repository.local

import android.content.Context
import androidx.preference.PreferenceManager
import dagger.Reusable
import javax.inject.Inject

@Reusable
class SharedPrefs @Inject constructor(
    context: Context
) {
    private val prefs = PreferenceManager.getDefaultSharedPreferences(context)
    private val editor = prefs.edit()

    /**
     * アプリで歩数をカウントし出した時にOSから取得した歩数を保持する。
     * リブートするまではこの値を現在歩数からマイナスした値が、アプリで利用したい歩数となる。
     * アプリ起動後（端末再起動後のアプリ初回起動も含む）、またはリセット後に一度しか書き換えないことを保証しないといけない。
     */
    private val appStartDeviceCounterKey = "key1"
    var appStartDeviceCounter: Long
        get() = getLong(appStartDeviceCounterKey)
        set(value) = putLong(appStartDeviceCounterKey, value)

    /**
     * appStartFirstCounterを取得した時刻
     * appStartFirstCounterはアプリ初回起動後、またはリセット後に一度しか実行しないようにしたいと上で書いた。
     * その一度実行を保証するため時刻を持っておく。こちらはアプリ初回起動時のみで端末が再起動されても更新しない
     */
    private val isRebootKey = "key2"
    var isReboot: Boolean
        get() = getBoolean(isRebootKey)
        set(value) = putBoolean(isRebootKey, value)

    /**
     * リブート後の歩数カウンタ初回処理の時刻
     * リブート後は歩数カウンタの計算方法を変える必要があるので1度だけ通る処理を実行したい。
     * これはリブートのたびに最初の1回行いたいのでフラグだとダメ。時刻にした。
     * また、アプリ初回起動時に前回リブート時刻を入れる
     */
    private val initStepCounterAfterRebootDatetimeKey = "key3"
    var initStepCounterAfterRebootDateTime: Long
        get() = getLong(initStepCounterAfterRebootDatetimeKey)
        set(value) = putLong(initStepCounterAfterRebootDatetimeKey, value)

    /**
     * アプリで利用する歩数
     */
    private val stepCounterSensorKey = "key4"
    var stepCounterSensor: Long
        get() = getLong(stepCounterSensorKey)
        set(value) = putLong(stepCounterSensorKey, value)

    private fun getLong(key: String, defaultValue: Long = 0) =
        prefs.getLong(key, defaultValue)

    private fun putLong(key: String, value: Long) {
        editor.putLong(key, value).commit()
        editor.commit()
    }

    private fun getBoolean(key: String) = prefs.getBoolean(key, false)

    private fun putBoolean(key: String, value: Boolean) {
        editor.putBoolean(key, value)
        editor.commit()
    }
}