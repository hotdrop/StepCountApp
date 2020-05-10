package jp.hotdrop.stepcountapp.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.viewpager.widget.PagerAdapter
import jp.hotdrop.stepcountapp.R
import jp.hotdrop.stepcountapp.common.Formatter
import jp.hotdrop.stepcountapp.common.toLongYearMonthDay
import kotlinx.android.synthetic.main.row_date.view.*
import org.threeten.bp.ZonedDateTime
import timber.log.Timber

class DateViewPagerAdapter(private val currentAt: ZonedDateTime, private val viewPagerDayList: List<Long>) : PagerAdapter() {

    override fun getCount(): Int = viewPagerDayList.size

    override fun isViewFromObject(view: View, obj: Any): Boolean {
        return view == obj
    }

    override fun destroyItem(container: ViewGroup, position: Int, obj: Any) {
        container.removeView(obj as View)
    }

    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        val currentDate = currentAt.plusDays(viewPagerDayList[position])
        val view = LayoutInflater.from(container.context).inflate(R.layout.row_date, container, false)
        view.show_date.text = currentDate.format(Formatter.ofDateWithDayOfWeek)
        view.row_date_layout.setOnClickListener {
            // TODO カレンダーを表示する
        }

        container.addView(view)
        return view
    }

    companion object {
        private const val dateListSize = 7

        /**
         * 指定された日を中心として前後が等間隔になるよう計dateListSize日をViewPagerに指定する。
         * ただし、先頭は当日までとしリストは現在選択日付の日数増減で表現する。
         */
        fun createSelectedList(currentAt: ZonedDateTime): List<Long> {
            val nowYMD = ZonedDateTime.now().toLongYearMonthDay()
            val currentYMD = currentAt.toLongYearMonthDay()

            val numDaysAfter = when {
                nowYMD - currentYMD > (dateListSize/2) -> (dateListSize/2).toLong()
                nowYMD - currentYMD < 0 -> 0L
                else -> nowYMD - currentYMD
            }

            val results = mutableListOf<Long>()
            var index = numDaysAfter
            (1..dateListSize).forEach { _ ->
                results.add(0, index--)
            }
            Timber.d("作成した日数リスト: $results")
            return results
        }
    }
}