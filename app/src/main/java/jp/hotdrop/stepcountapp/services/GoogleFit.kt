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
import jp.hotdrop.stepcountapp.common.toZonedDateTime
import jp.hotdrop.stepcountapp.model.DailyStepCount
import jp.hotdrop.stepcountapp.repository.GoogleFitRepository
import jp.hotdrop.stepcountapp.ui.BaseViewModel
import kotlinx.coroutines.launch
import org.threeten.bp.ZonedDateTime
import timber.log.Timber
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
                val dataPoint = it.dataPoints?.firstOrNull()
                postDataInPoint(dataPoint, ZonedDateTime.now())
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
                Timber.d("DBに登録されていないので保存します。")
                registerPastCount(context, targetAt)
            }
        }
    }

    private fun registerPastCount(context: Context, targetAt: ZonedDateTime) {
        val request = requestHistoryData(targetAt)
        Fitness.getHistoryClient(context, account(context))
            .readData(request)
            .addOnSuccessListener {
                Timber.d("GoogleFitから過去のレスポンスがきました。")
                val dataPoint = it.buckets?.firstOrNull()
                    ?.getDataSet(DataType.AGGREGATE_STEP_COUNT_DELTA)
                    ?.dataPoints?.firstOrNull()
                postDataInPoint(dataPoint, targetAt)
            }
    }

    private fun postDataInPoint(dp: DataPoint?, dayAt: ZonedDateTime) {
        val daily = if (dp != null) {
            val value = dp.getValue(Field.FIELD_STEPS)
            DailyStepCount(stepNum = value.asInt().toLong(), dayAt = dayAt)
        } else {
            DailyStepCount(stepNum = 0, dayAt = dayAt)
        }

        launch {
            repository.save(daily)
            mutableCounter.postValue(daily)
        }
    }

    private fun requestHistoryData(targetAt: ZonedDateTime): DataReadRequest {
        return DataReadRequest.Builder()
            .aggregate(DataType.TYPE_STEP_COUNT_DELTA, DataType.AGGREGATE_STEP_COUNT_DELTA)
            .setTimeRange(targetAt.toStartDateTime().toEpochSecond(), targetAt.toEndDateTime().toEpochSecond(), TimeUnit.SECONDS)
            .bucketByTime(1, TimeUnit.DAYS)
            .build()
    }
}