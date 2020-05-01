package jp.hotdrop.stepcountapp.model

import org.threeten.bp.ZonedDateTime

data class DailyStepCount (
    val stepNum: Long,
    val dayAt: ZonedDateTime
)