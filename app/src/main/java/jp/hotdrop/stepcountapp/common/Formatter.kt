package jp.hotdrop.stepcountapp.common

import org.threeten.bp.format.DateTimeFormatter

object Formatter {
    val ofDate: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy年MM月dd日")
    val ofDateTime: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy年MM月dd日 HH:mm:ss")
}
