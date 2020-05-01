package jp.hotdrop.stepcountapp.model

import org.threeten.bp.ZonedDateTime

data class DailyStepCount (
    val id: Long,
    val stepNum: Long,
    val dayAt: ZonedDateTime
)