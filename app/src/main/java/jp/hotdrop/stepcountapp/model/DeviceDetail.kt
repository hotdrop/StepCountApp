package jp.hotdrop.stepcountapp.model

import org.threeten.bp.Instant
import org.threeten.bp.ZoneId
import org.threeten.bp.ZonedDateTime

data class DeviceDetail (
    val appStartFirstCounter: Long,
    val appStartStepCounterDateTimeEpoch: Long,
    val initAfterRebootDateTimeEpoch: Long,
    val stepCounterFromOS: Long
) {
    fun getInitAfterRebootDateTime(): ZonedDateTime? {
        return if (initAfterRebootDateTimeEpoch == 0L) {
            null
        } else {
            ZonedDateTime.ofInstant(Instant.ofEpochMilli(initAfterRebootDateTimeEpoch), ZoneId.systemDefault())
        }
    }
}