package jp.hotdrop.stepcountapp.common

import org.threeten.bp.Instant
import org.threeten.bp.ZoneId
import org.threeten.bp.ZonedDateTime
import java.util.*

fun Long.toZonedDateTime(): ZonedDateTime =
    ZonedDateTime.ofInstant(Instant.ofEpochMilli(this), ZoneId.systemDefault())

fun Instant.toZonedDateTime(): ZonedDateTime =
    ZonedDateTime.ofInstant(this, ZoneId.systemDefault())

fun ZonedDateTime.toStartDateTime(): ZonedDateTime {
    return ZonedDateTime.of(this.year, this.monthValue, this.dayOfMonth, 0, 0, 0, 0, ZoneId.systemDefault())
}

fun ZonedDateTime.toEndDateTime(): ZonedDateTime {
    val nextDay = this.plusDays(1)
    val nextDayStartTime = ZonedDateTime.of(nextDay.year, nextDay.monthValue, nextDay.dayOfMonth, 0, 0, 0, 0, ZoneId.systemDefault())
    return nextDayStartTime.minusNanos(1)
}

fun ZonedDateTime.toLongYearMonthDay(): Long {
    val monthStr = this.month.value.toString().padStart(2, '0')
    val dayStr = this.dayOfMonth.toString().padStart(2, '0')
    return "${this.year}${monthStr}${dayStr}".toLong()
}

fun ZonedDateTime.toStartDayEpochSecond(): Long {
    return ZonedDateTime.of(this.year, this.monthValue, this.dayOfMonth, 0, 0, 0, 0, ZoneId.systemDefault()).toEpochSecond()
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