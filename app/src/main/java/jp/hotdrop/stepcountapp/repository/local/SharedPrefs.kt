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
     * リブートするまではこの値を現在歩数からマイナスした値がアプリで利用したい歩数となる。
     * この値は1度書き換えたら2度と書き換えない。そのため端末再起動後は未使用値となる。
     */
    private val appStartDeviceCounterKey = "key1"
    var appStartDeviceCounter: Long
        get() = getLong(appStartDeviceCounterKey)
        set(value) = putLong(appStartDeviceCounterKey, value)

    /**
     * 再起動フラグ
     * アプリ初回起動後に端末を再起動したらtrueにする。以降はfalseになることはない
     */
    private val isRebootKey = "key2"
    var isReboot: Boolean
        get() = getBoolean(isRebootKey)
        set(value) = putBoolean(isRebootKey, value)

    /**
     * リブート後の歩数カウンタ初回処理の時刻
     * リブート後は歩数カウンタの計算方法を変える必要があるので1度だけ通る処理を実行したい。
     * これはリブートのたびに最初の1回だけ行いたいのでフラグだとダメで別途時刻を持つことにした。
     */
    private val initStepCounterAfterRebootDatetimeKey = "key3"
    var initStepCounterAfterRebootDateTime: Long
        get() = getLong(initStepCounterAfterRebootDatetimeKey)
        set(value) = putLong(initStepCounterAfterRebootDatetimeKey, value)

    private fun getLong(key: String, defaultValue: Long = 0) =
        prefs.getLong(key, defaultValue)

    private fun putLong(key: String, value: Long) {
        editor.putLong(key, value).commit()
        editor.apply()
    }

    private fun getBoolean(key: String) = prefs.getBoolean(key, false)

    private fun putBoolean(key: String, value: Boolean) {
        editor.putBoolean(key, value)
        editor.apply()
    }
}