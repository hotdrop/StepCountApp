package jp.hotdrop.stepcountapp.common

import org.threeten.bp.Instant
import org.threeten.bp.ZoneId
import org.threeten.bp.ZonedDateTime
import java.util.*

fun Long.milliToZonedDateTime(): ZonedDateTime =
    ZonedDateTime.ofInstant(Instant.ofEpochMilli(this), ZoneId.systemDefault())

fun Instant.toZonedDateTime(): ZonedDateTime =
    ZonedDateTime.ofInstant(this, ZoneId.systemDefault())

fun ZonedDateTime.toLongYearMonthDay(): Long {
    val monthStr = this.month.value.toString().padStart(2, '0')
    val dayStr = this.dayOfMonth.toString().padStart(2, '0')
    return "${this.year}${monthStr}${dayStr}".toLong()
}

fun <T> Iterable<T>.sumByLong(selector: (T) -> Long): Long {
    var sum: Long = 0
    for (element in this) {
        sum += selector(element)
    }
    return sum
}

fun Long.toFormatWithComma(): String =
    if (this == 0L) {
        "0"
    } else {
        String.format(Locale.JAPAN, "%1$,3d", this)
    }

fun String.toLongRemoveComma(): Long =
    if (this.isEmpty()) {
        0
    } else {
        replace(",", "").trim().toLongOrNull() ?: 0
    }