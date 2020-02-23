package spbpu.hsamcp.mathgame.mathResolver

import android.text.Spannable
import android.text.SpannableStringBuilder
import androidx.core.text.getSpans

data class SpanInfo(val span: Any, val strInd: Int, val start: Int,
    val end: Int, val flag: Int = Spannable.SPAN_EXCLUSIVE_EXCLUSIVE) {
    companion object {
        fun getSpanInfoArray(string: SpannableStringBuilder): ArrayList<SpanInfo> {
            val array = ArrayList<SpanInfo>()
            val spans = string.getSpans<Any>()
            val strings = string.split("\n") as ArrayList
            val len = strings[0].length + 1
            for (span in spans) {
                val start = string.getSpanStart(span)
                val end = string.getSpanEnd(span)
                val strInd = start / len
                val startInStr = start % len
                val endInStr = end % len
                array.add(SpanInfo(span, strInd, startInStr, endInStr, Spannable.SPAN_INCLUSIVE_INCLUSIVE))
            }
            return array
        }
    }
}