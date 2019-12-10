package spbpu.hsamcp.mathgame.mathResolver

import android.text.Spannable

data class SpanInfo(val span: Any, val strInd: Int, val start: Int, val end: Int, val flag: Int = Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)