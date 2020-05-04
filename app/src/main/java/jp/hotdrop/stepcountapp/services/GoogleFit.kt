package jp.hotdrop.stepcountapp.services

import android.app.Activity
import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.fitness.Fitness
import com.google.android.gms.fitness.FitnessOptions
import com.google.android.gms.fitness.data.DataType
import com.google.android.gms.fitness.data.Field
import com.google.android.gms.fitness.request.DataReadRequest
import dagger.Reusable
import jp.hotdrop.stepcountapp.common.toStartDayEpochSecond
import jp.hotdrop.stepcountapp.common.toZonedDateTime
import org.threeten.bp.ZonedDateTime
import timber.log.Timber
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@Reusable
class GoogleFit @Inject constructor() {

    private val mutableSignIn = MutableLiveData<GoogleSignInAccount?>()
    val signIn: LiveData<GoogleSignInAccount?> = mutableSignIn

    private val mutableCounter = MutableLiveData<Int>()
    val counter: LiveData<Int> = mutableCounter

    private val options: FitnessOptions = FitnessOptions.builder()
        .addDataType(DataType.TYPE_STEP_COUNT_DELTA, FitnessOptions.ACCESS_READ)
        .addDataType(DataType.AGGREGATE_STEP_COUNT_DELTA, FitnessOptions.ACCESS_READ)
        .build()

    private fun account(context: Context): GoogleSignInAccount {
        val lastAccount = GoogleSignIn.getLastSignedInAccount(context)
        if (lastAccount != null) {
            Timber.d("lastAccountがnullではないのでgetLastSignedInAccountを返す")
            return lastAccount
        }

        Timber.d("lastAccountがnullなのでgetAccountForExtension返す")
        return GoogleSignIn.getAccountForExtension(context, options)
    }

    fun hasPermissions(context: Context): Boolean {
        return GoogleSignIn.hasPermissions(account(context), options)
    }

    fun requestPermissions(activity: Activity, requestCode: Int) {
        GoogleSignIn.requestPermissions(
            activity,
            requestCode,
            account(activity),
            options
        )
    }

    fun signIn(context: Context) {
        val account = GoogleSignIn.getAccountForExtension(context, options)
        mutableSignIn.postValue(account)
    }

    fun signOut(context: Context) {
        GoogleSignIn.getLastSignedInAccount(context)?.run {
            Fitness.getConfigClient(context, this).disableFit()
            mutableSignIn.postValue(null)
        }
    }

    fun registerTodayCount(context: Context) {
        val request = requestRealTimeCount()
        Timber.d("GoogleFitのHistoryClientリスナーを登録")
        Fitness.getHistoryClient(context, account(context))
            .readDailyTotal(DataType.TYPE_STEP_COUNT_DELTA)
            .addOnSuccessListener {
                Timber.d("GoogleFitから今日のレスポンスがきました。")
                it.dataPoints.first().let { dataPoint ->
                    val start = dataPoint.getStartTime(TimeUnit.MILLISECONDS)
                    val end = dataPoint.getEndTime(TimeUnit.MILLISECONDS)
                    val value = dataPoint.getValue(Field.FIELD_STEPS)
                    Timber.d("start=${start.toZonedDateTime()} end=${end.toZonedDateTime()} value=${value}")
                    mutableCounter.postValue(value.asInt())
                }
            }
    }

    private fun requestRealTimeCount(): DataReadRequest {
        val now = ZonedDateTime.now()
        return DataReadRequest.Builder()
            .aggregate(DataType.TYPE_STEP_COUNT_DELTA, DataType.AGGREGATE_STEP_COUNT_DELTA)
            .setTimeRange(now.toStartDayEpochSecond(), now.toEpochSecond(), TimeUnit.SECONDS)
            .bucketByTime(1, TimeUnit.MINUTES)
            .build()
    }
}