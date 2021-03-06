package jp.hotdrop.stepcountapp.model

import org.threeten.bp.ZonedDateTime

data class DailyStepCount (
    val ymdId: Long,
    val stepNum: Long,
    val distance: Long = 0,
    val dayAt: ZonedDateTime
)