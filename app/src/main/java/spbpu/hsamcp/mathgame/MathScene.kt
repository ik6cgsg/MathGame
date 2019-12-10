package spbpu.hsamcp.mathgame

class MathScene {
    companion object {
        var globalFormula: GlobalMathView? = null
        var currentRule: RuleMathView? = null
        var currentLevel: Level? = null

        fun onRuleClicked() {
            if (globalFormula != null) {
                if (currentRule!!.substFrom != null && currentRule!!.substTo != null) {
                    globalFormula!!.performSubstitution(currentRule!!.substFrom!!, currentRule!!.substTo!!)
                }
            }
        }
    }
}