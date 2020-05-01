package jp.hotdrop.stepcountapp.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.distinctUntilChanged
import androidx.lifecycle.map
import dagger.Reusable
import jp.hotdrop.stepcountapp.common.sumByLong
import jp.hotdrop.stepcountapp.model.DeviceDetail
import jp.hotdrop.stepcountapp.model.DailyStepCount
import jp.hotdrop.stepcountapp.repository.local.SharedPrefs
import jp.hotdrop.stepcountapp.repository.local.room.StepCounterDatabase
import jp.hotdrop.stepcountapp.repository.local.room.DailyStepCountEntity
import org.threeten.bp.ZoneId
import org.threeten.bp.ZonedDateTime
import javax.inject.Inject

@Reusable
class StepCounterRepository @Inject constructor(
    private val db: StepCounterDatabase,
    private val sharedPrefs: SharedPrefs
) {
    fun todayCountLiveData(): LiveData<DailyStepCount?> {
        val todayKey = DailyStepCountEntity.makeKey(ZonedDateTime.now())
        return db.select(todayKey).map {
            it?.let {
                DailyStepCount(
                    id = it.id,
                    stepNum = it.stepNum,
                    dayAt = ZonedDateTime.ofInstant(it.dayAt, ZoneId.systemDefault())
                )
            }
        }.distinctUntilChanged()
    }

    suspend fun totalCountPreviousDateStepNum(): Long {
        val todayKey = DailyStepCountEntity.makeKey(ZonedDateTime.now())
        return db.selectAll()
            .filter { it.id != todayKey }
            .sumByLong { it.stepNum }
    }

    suspend fun save(counter: Long) {
        val entity = DailyStepCountEntity.create(stepNum = counter)
        db.save(entity)
    }

    fun getDeviceDetail(counterFromOS: Long): DeviceDetail {
        return DeviceDetail(
            appStartFirstCounter = sharedPrefs.appStartDeviceCounter,
            initAfterRebootDateTimeEpoch = sharedPrefs.initStepCounterAfterRebootDateTime,
            stepCounterFromOS = counterFromOS
        )
    }
}