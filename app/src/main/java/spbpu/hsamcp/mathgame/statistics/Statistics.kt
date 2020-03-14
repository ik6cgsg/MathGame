package spbpu.hsamcp.mathgame.statistics

import android.content.Context
import android.net.ConnectivityManager
import android.util.Log
import com.twf.api.expressionToString
import com.twf.expressiontree.ExpressionNode
import com.twf.expressiontree.ExpressionSubstitution
import spbpu.hsamcp.mathgame.BuildConfig.LOG_STAT
import spbpu.hsamcp.mathgame.MathScene
import spbpu.hsamcp.mathgame.level.Award

class Statistics {
    companion object {
        private var login: String = "cgsgilich"
        private var name: String = "Ilya"
        private var surname: String = "Kozlov"
        private var secondName: String = "Alexeevich"
        private var group: String = "2"
        private var institution: String = "SPbPU"
        private var age: Int = 22
        private var startTime: Long = -1
        private var lastActionTime: Long = -1

        private var logArray: ArrayList<MathGameLog> = ArrayList()

        fun setStartTime() {
            startTime = System.currentTimeMillis()
            lastActionTime = startTime
        }

        fun getTimeDiff(): Long {
            return System.currentTimeMillis() - startTime
        }

        fun logRule(currSteps: Int, nextSteps: Int, currExpr: ExpressionNode, nextExpr: ExpressionNode,
                    currRule: ExpressionSubstitution?, place: ExpressionNode) {
            val activity = MathScene.playActivity.get()!!
            val rule = if (currRule == null) {
                ""
            } else {
                expressionToString(currRule.left) + " : " + expressionToString(currRule.right)
            }
            val mathLog = MathGameLog(
                currStepsNumber = currSteps,
                nextStepsNumber = nextSteps,
                currExpression = expressionToString(currExpr),
                nextExpression = expressionToString(nextExpr),
                currRule = rule,
                currSelectedPlace = expressionToString(place)
            )
            mathLog.addInfoFrom(activity, MathScene.currentLevel!!, Action.RULE)
            sendLog(mathLog, activity)
        }

        fun logPlace(currSteps: Int, currExpr: ExpressionNode, place: ExpressionNode) {
            val currExprStr = expressionToString(currExpr)
            val mathLog = MathGameLog(
                currStepsNumber = currSteps,
                nextStepsNumber = currSteps,
                currExpression = currExprStr,
                nextExpression = currExprStr,
                currSelectedPlace = expressionToString(place)
            )
            val activity = MathScene.playActivity.get()!!
            mathLog.addInfoFrom(activity, MathScene.currentLevel!!, Action.PLACE)
            sendLog(mathLog, activity)
        }

        fun logStart() {
            val exprStr = expressionToString(MathScene.currentLevel!!.startFormula)
            val mathLog = MathGameLog(
                currStepsNumber = 0,
                nextStepsNumber = 0,
                currExpression = exprStr,
                nextExpression = exprStr
            )
            val activity = MathScene.playActivity.get()!!
            mathLog.addInfoFrom(activity, MathScene.currentLevel!!, Action.START)
            sendLog(mathLog, activity)
        }

        fun logUndo(currSteps: Int, nextSteps: Int, currExpr: ExpressionNode,
                    nextExpr: ExpressionNode, currPlace: ExpressionNode?) {
            val curr = expressionToString(currExpr)
            val next = expressionToString(nextExpr)
            val place = if (currPlace == null) {
                ""
            } else {
                expressionToString(currPlace)
            }
            val mathLog = MathGameLog(
                currStepsNumber = currSteps,
                nextStepsNumber = nextSteps,
                currExpression = curr,
                nextExpression = next,
                currSelectedPlace = place
            )
            val activity = MathScene.playActivity.get()!!
            mathLog.addInfoFrom(activity, MathScene.currentLevel!!, Action.UNDO)
            sendLog(mathLog, activity)
        }

        fun logRestart(currSteps: Int, currExpr: ExpressionNode, currPlace: ExpressionNode?) {
            val activity = MathScene.playActivity.get()!!
            val curr = expressionToString(currExpr)
            val next = expressionToString(MathScene.currentLevel!!.startFormula)
            val place = if (currPlace == null) {
                ""
            } else {
                expressionToString(currPlace)
            }
            val mathLog = MathGameLog(
                currStepsNumber = currSteps,
                nextStepsNumber = 0,
                currExpression = curr,
                nextExpression = next,
                currSelectedPlace = place
            )
            mathLog.addInfoFrom(activity, MathScene.currentLevel!!, Action.RESTART)
            sendLog(mathLog, activity)
        }

        fun logMenu(currSteps: Int, currExpr: ExpressionNode, currPlace: ExpressionNode?) {
            val activity = MathScene.playActivity.get()!!
            val curr = expressionToString(currExpr)
            val place = if (currPlace == null) {
                ""
            } else {
                expressionToString(currPlace)
            }
            val mathLog = MathGameLog(
                currStepsNumber = currSteps,
                nextStepsNumber = currSteps,
                currExpression = curr,
                nextExpression = curr,
                currSelectedPlace = place
            )
            mathLog.addInfoFrom(activity, MathScene.currentLevel!!, Action.MENU)
            sendLog(mathLog, activity)
        }

        fun logWin(currSteps: Int, award: Award) {
            val exprStr = expressionToString(MathScene.currentLevel!!.endFormula)
            val mathLog = MathGameLog(
                currStepsNumber = currSteps,
                nextStepsNumber = currSteps,
                currAwardCoef = award.coeff.toFloat(),
                currExpression = exprStr,
                nextExpression = exprStr
            )
            val activity = MathScene.playActivity.get()!!
            mathLog.addInfoFrom(activity, MathScene.currentLevel!!, Action.WIN)
            sendLog(mathLog, activity)
        }

        fun logLoose(currSteps: Int, currExpr: ExpressionNode, currPlace: ExpressionNode?) {
            val exprStr = expressionToString(currExpr)
            val place = if (currPlace == null) {
                ""
            } else {
                expressionToString(currPlace)
            }
            val mathLog = MathGameLog(
                currStepsNumber = currSteps,
                nextStepsNumber = currSteps,
                currExpression = exprStr,
                nextExpression = exprStr,
                currSelectedPlace = place
            )
            val activity = MathScene.playActivity.get()!!
            mathLog.addInfoFrom(activity, MathScene.currentLevel!!, Action.LOOSE)
            sendLog(mathLog, activity)
        }

        private fun sendLog(log: MathGameLog, context: Context) {
            setDefault(log)
            if (LOG_STAT) {
                if (isConnectedToNetwork(context)) {
                    if (logArray.isNotEmpty()) {
                        for (l in logArray) {
                            sendOneLog(l)
                        }
                    }
                    logArray.clear()
                    sendOneLog(log)
                } else {
                    logArray.add(log)
                }
            } else {
                Log.d("Statistics", "MathGameLog: $log}")
            }
        }

        private fun sendOneLog(log: MathGameLog) {
            val req = RequestData()
            req.body = log.toString()
            req.headers["Content-type"] = "application/json"
            Request.doAsyncRequest(req)
        }

        private fun isConnectedToNetwork(context: Context): Boolean {
            val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager?
            return connectivityManager?.activeNetworkInfo?.isConnected ?: false
        }

        private fun setDefault(log: MathGameLog) {
            log.login = login
            log.name = name
            log.surname = surname
            log.secondName = secondName
            log.group = group
            log.institution = institution
            log.age = age
            val time = System.currentTimeMillis()
            log.timeFromLastActionMS = time - lastActionTime
            lastActionTime = time
            if (startTime > 0) { // Level was created and set
                log.currTimeMS = time - startTime
                log.leftTimeMS = log.totalTimeMS - log.currTimeMS
            }
        }
    }
}