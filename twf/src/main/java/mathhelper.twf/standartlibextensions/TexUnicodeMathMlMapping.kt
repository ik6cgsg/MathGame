package mathhelper.twf.standartlibextensions

data class Letter(
        val unicode: Char,
        val unicodeCode: Int,
        val tex: String,
        val mathMl: String
)

val lettersData = listOf(
        Letter('α', 945,"\\alpha", "&#x3B1;"),
        Letter('β', 946,"\\beta", "&#x3B2;"),
        Letter('γ', 947,"\\gamma", "&#x3B3;"),
        Letter('π', 960,"\\pi", "&#x3C0;"),
        Letter('ω', 969,"\\omega", "&#x3C9;")
)

val texToUnicode = lettersData.associateBy({it.tex}, {it.unicode.toString()})
val unicodeToTex = lettersData.associateBy({it.unicode.toString()}, {it.tex})

val texOpenBracket = "\\left("
val texCloseBracket = "\\right)"