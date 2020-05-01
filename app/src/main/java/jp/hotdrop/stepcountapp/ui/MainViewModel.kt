package jp.hotdrop.stepcountapp.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import jp.hotdrop.stepcountapp.model.Accuracy
import jp.hotdrop.stepcountapp.model.DailyStepCount
import jp.hotdrop.stepcountapp.model.DeviceDetail
import jp.hotdrop.stepcountapp.repository.StepCounterRepository
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

class MainViewModel @Inject constructor(
    private val repository: StepCounterRepository
) : BaseViewModel() {

    val todayStepCounter: LiveData<DailyStepCount?> = repository.todayCountLiveData()

    private val mutableAccuracy = MutableLiveData<Accuracy>()
    val accuracy: LiveData<Accuracy> = mutableAccuracy

    private val mutableDeviceDetail = MutableLiveData<DeviceDetail>()
    val deviceDetail: LiveData<DeviceDetail> = mutableDeviceDetail

    fun calcTodayCount(effectiveCount: Long) {
        launch {
            val previousTotalNum = repository.totalCountPreviousDateStepNum()
            val todayStepCount = effectiveCount - previousTotalNum
            Timber.d("有効歩数=$effectiveCount 前日までのトータル歩数=$previousTotalNum この差分が今日の歩数になるはず。")
            repository.save(todayStepCount)
        }
    }

    fun updateAccuracy(accuracy: Accuracy) {
        mutableAccuracy.postValue(accuracy)
    }

    fun updateCounterInOS(counter: Long) {
        val detail = repository.getDeviceDetail(counter)
        mutableDeviceDetail.postValue(detail)
    }
}