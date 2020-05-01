package jp.hotdrop.stepcountapp.model

import jp.hotdrop.stepcountapp.common.milliToZonedDateTime
import org.threeten.bp.ZonedDateTime

data class DeviceDetail (
    val appStartFirstCounter: Long,
    val initAfterRebootDateTimeEpoch: Long,
    val stepCounterFromOS: Long
) {
    fun getInitAfterRebootDateTime(): ZonedDateTime? {
        return if (initAfterRebootDateTimeEpoch == 0L) {
            null
        } else {
            initAfterRebootDateTimeEpoch.milliToZonedDateTime()
        }
    }
}