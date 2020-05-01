package jp.hotdrop.stepcountapp.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import jp.hotdrop.stepcountapp.model.Accuracy
import jp.hotdrop.stepcountapp.model.DailyStepCount
import jp.hotdrop.stepcountapp.model.DeviceDetail
import jp.hotdrop.stepcountapp.repository.StepCounterRepository
import kotlinx.coroutines.launch
import javax.inject.Inject

class MainViewModel @Inject constructor(
    private val repository: StepCounterRepository
) : BaseViewModel() {

    val todayStepCounter: LiveData<DailyStepCount?> = repository.todayCountLiveData()

    private val mutableAccuracy = MutableLiveData<Accuracy>()
    val accuracy: LiveData<Accuracy> = mutableAccuracy

    private val mutableDeviceDetail = MutableLiveData<DeviceDetail>()
    val deviceDetail: LiveData<DeviceDetail> = mutableDeviceDetail

    fun saveCounter(counter: Long) {
        launch {
            repository.save(counter)
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