package jp.hotdrop.stepcountapp.services

import android.app.Activity
import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.fitness.Fitness
import com.google.android.gms.fitness.FitnessOptions
import com.google.android.gms.fitness.data.DataPoint
import com.google.android.gms.fitness.data.DataType
import com.google.android.gms.fitness.data.Field
import com.google.android.gms.fitness.request.DataReadRequest
import dagger.Reusable
import jp.hotdrop.stepcountapp.common.toStartDayEpochSecond
import jp.hotdrop.stepcountapp.common.toZonedDateTime
import jp.hotdrop.stepcountapp.model.DailyStepCount
import org.threeten.bp.ZonedDateTime
import timber.log.Timber
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@Reusable
class GoogleFit @Inject constructor() {

    private val mutableSignIn = MutableLiveData<GoogleSignInAccount?>()
    val signIn: LiveData<GoogleSignInAccount?> = mutableSignIn

    private val mutableCounter = MutableLiveData<DailyStepCount>()
    val counter: LiveData<DailyStepCount> = mutableCounter

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
        Timber.d("GoogleFitのHistoryClientリスナーを登録")
        Fitness.getHistoryClient(context, account(context))
            .readDailyTotal(DataType.TYPE_STEP_COUNT_DELTA)
            .addOnSuccessListener {
                Timber.d("GoogleFitから今日のレスポンスがきました。")
                if (it.dataPoints.isEmpty()) {
                    postDataInPoint(null, ZonedDateTime.now())
                } else {
                    it.dataPoints.first().let { dp ->
                        postDataInPoint(dp, ZonedDateTime.now())
                    }
                }
            }
    }

    fun findStepCount(context: Context, targetAt: ZonedDateTime) {
        // TODO 過去のデータはDBから取得したほうがいいのでは
        val request = requestHistoryData(targetAt)
        Fitness.getHistoryClient(context, account(context))
            .readData(request)
            .addOnSuccessListener {
                Timber.d("GoogleFitから過去のレスポンスがきました。")
                val dataPoints = it.getDataSet(DataType.AGGREGATE_STEP_COUNT_DELTA).dataPoints
                if (dataPoints.isEmpty()) {
                    Timber.d("${targetAt}のdataPointは空です。")
                    postDataInPoint(null, targetAt)
                } else {
                    Timber.d("${targetAt}のdataPointは${dataPoints.size}件です。")
                    postDataInPoint(dataPoints.first(), targetAt)
                }
            }
    }

    private fun postDataInPoint(dp: DataPoint?, dayAt: ZonedDateTime) {
        val daily = if (dp != null) {
            val value = dp.getValue(Field.FIELD_STEPS)
            Timber.d("start=${dp.getStartTime(TimeUnit.MILLISECONDS).toZonedDateTime()} end=${dp.getEndTime(TimeUnit.MILLISECONDS).toZonedDateTime()} value=${value}")
            DailyStepCount(stepNum = value.asInt().toLong(), dayAt = ZonedDateTime.now())
        } else {
            DailyStepCount(stepNum = 0, dayAt = dayAt)
        }
        mutableCounter.postValue(daily)
    }

    private fun requestHistoryData(targetAt: ZonedDateTime): DataReadRequest {
        return DataReadRequest.Builder()
            .aggregate(DataType.TYPE_STEP_COUNT_DELTA, DataType.AGGREGATE_STEP_COUNT_DELTA)
            .setTimeRange(targetAt.toStartDayEpochSecond(), targetAt.toEpochSecond(), TimeUnit.SECONDS)
            .bucketByTime(1, TimeUnit.DAYS)
            .build()
    }
}