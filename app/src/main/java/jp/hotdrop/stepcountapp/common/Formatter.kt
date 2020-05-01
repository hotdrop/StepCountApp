package jp.hotdrop.stepcountapp.common

import org.threeten.bp.Instant
import org.threeten.bp.ZoneId
import org.threeten.bp.ZonedDateTime
import org.threeten.bp.format.DateTimeFormatter
import java.util.*

object Formatter {
    val ofDate: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy年MM月dd日")
    val ofDateTime: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy年MM月dd日 HH:mm:ss")
}

fun Long.milliToZonedDateTime(): ZonedDateTime =
    ZonedDateTime.ofInstant(Instant.ofEpochMilli(this), ZoneId.systemDefault())

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