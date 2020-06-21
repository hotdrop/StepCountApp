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
import jp.hotdrop.stepcountapp.common.toEndDateTime
import jp.hotdrop.stepcountapp.common.toStartDateTime
import jp.hotdrop.stepcountapp.model.DailyStepCount
import jp.hotdrop.stepcountapp.repository.GoogleFitRepository
import jp.hotdrop.stepcountapp.ui.BaseViewModel
import kotlinx.coroutines.launch
import org.threeten.bp.ZonedDateTime
import timber.log.Timber
import java.lang.IllegalArgumentException
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class GoogleFit @Inject constructor(
    private val repository: GoogleFitRepository
) : BaseViewModel() {

    private val mutableSignIn = MutableLiveData<GoogleSignInAccount?>()
    val signIn: LiveData<GoogleSignInAccount?> = mutableSignIn

    private val mutableCounter = MutableLiveData<DailyStepCount>()
    val counter: LiveData<DailyStepCount> = mutableCounter

    private val options: FitnessOptions = FitnessOptions.builder()
        .addDataType(DataType.TYPE_STEP_COUNT_DELTA, FitnessOptions.ACCESS_READ)
        .addDataType(DataType.TYPE_DISTANCE_DELTA, FitnessOptions.ACCESS_READ)
        .addDataType(DataType.AGGREGATE_STEP_COUNT_DELTA, FitnessOptions.ACCESS_READ)
        .addDataType(DataType.AGGREGATE_DISTANCE_DELTA, FitnessOptions.ACCESS_READ)
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

    fun registerTodayCount(context: Context) {
        Timber.d("GoogleFitのHistoryClientリスナーを登録")

        Fitness.getHistoryClient(context, account(context))
            .readDailyTotal(DataType.TYPE_STEP_COUNT_DELTA)
            .addOnSuccessListener {
                Timber.d("GoogleFitから今日の歩数レスポンスがきました。")
                val dataPoint = it.dataPoints?.firstOrNull()
                postStepCountInPoint(dataPoint, ZonedDateTime.now())
            }

        Fitness.getHistoryClient(context, account(context))
            .readDailyTotal(DataType.TYPE_DISTANCE_DELTA)
            .addOnSuccessListener {
                Timber.d("GoogleFitから今日の距離レスポンスがきました。")
                val dataPoint = it.dataPoints?.firstOrNull()
                postDistanceInPoint(dataPoint, ZonedDateTime.now())
            }
    }

    fun findPastStepCount(context: Context, targetAt: ZonedDateTime) {
        launch {
            // TODO 過去7日間くらい一気にとった方がいいかも
            Timber.d("$targetAt の歩数をDBから取得します。")
            val daily = repository.find(targetAt)
            if (daily != null) {
                Timber.d("DBから取得できたのでそのまま返します。")
                mutableCounter.postValue(daily)
            } else {
                Timber.d("DBに登録されていないので取得します。")
                registerPastCount(context, targetAt)
                registerPastDistance(context, targetAt)
            }
        }
    }

    private fun registerPastCount(context: Context, targetAt: ZonedDateTime) {
        val request = DataReadRequest.Builder()
            .aggregate(DataType.TYPE_STEP_COUNT_DELTA, DataType.AGGREGATE_STEP_COUNT_DELTA)
            .setTimeRange(targetAt.toStartDateTime().toEpochSecond(), targetAt.toEndDateTime().toEpochSecond(), TimeUnit.SECONDS)
            .bucketByTime(1, TimeUnit.DAYS)
            .build()

        Fitness.getHistoryClient(context, account(context))
            .readData(request)
            .addOnSuccessListener {
                Timber.d("GoogleFitから過去のレスポンスがきました。")
                it.buckets?.firstOrNull()?.let { bucket ->
                    val dataPoint = bucket.getDataSet(DataType.AGGREGATE_STEP_COUNT_DELTA)?.dataPoints?.firstOrNull()
                    postStepCountInPoint(dataPoint, targetAt)
                }
            }
    }

    private fun registerPastDistance(context: Context, targetAt: ZonedDateTime) {
        val request = DataReadRequest.Builder()
            .aggregate(DataType.TYPE_DISTANCE_DELTA, DataType.AGGREGATE_DISTANCE_DELTA)
            .setTimeRange(targetAt.toStartDateTime().toEpochSecond(), targetAt.toEndDateTime().toEpochSecond(), TimeUnit.SECONDS)
            .bucketByTime(1, TimeUnit.DAYS)
            .build()

        Fitness.getHistoryClient(context, account(context))
            .readData(request)
            .addOnSuccessListener {
                Timber.d("GoogleFitから過去のレスポンスがきました。")
                it.buckets?.firstOrNull()?.let { bucket ->
                    val dataPoint = bucket.getDataSet(DataType.AGGREGATE_DISTANCE_DELTA)?.dataPoints?.firstOrNull()
                    postDistanceInPoint(dataPoint, targetAt)
                }
            }
    }

    private fun postStepCountInPoint(dp: DataPoint?, dayAt: ZonedDateTime) {
        val stepNum = dp?.getValue(Field.FIELD_STEPS)?.asInt()?.toLong() ?: 0
        launch {
            repository.saveStepNum(stepNum, dayAt)
            repository.find(dayAt)?.let {
                mutableCounter.postValue(it)
            }
        }
    }

    private fun postDistanceInPoint(dp: DataPoint?, dayAt: ZonedDateTime) {
        val stepNum = dp?.getValue(Field.FIELD_DISTANCE)?.asFloat()?.toLong() ?: 0
        launch {
            repository.saveDistance(stepNum, dayAt)
            repository.find(dayAt)?.let {
                mutableCounter.postValue(it)
            }
        }
    }
}