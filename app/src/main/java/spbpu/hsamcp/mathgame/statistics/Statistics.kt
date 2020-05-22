package spbpu.hsamcp.mathgame.statistics

import android.content.Context
import android.net.ConnectivityManager
import android.os.Build
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.twf.api.expressionToString
import com.twf.expressiontree.ExpressionNode
import com.twf.expressiontree.ExpressionSubstitution
import spbpu.hsamcp.mathgame.LevelScene
import spbpu.hsamcp.mathgame.PlayScene
import spbpu.hsamcp.mathgame.common.AuthInfo
import spbpu.hsamcp.mathgame.common.Constants
import spbpu.hsamcp.mathgame.level.Award

class Statistics {
    companion object {
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

        fun logRule(currSteps: Float, nextSteps: Float, currExpr: ExpressionNode, nextExpr: ExpressionNode,
                    currRule: ExpressionSubstitution?, place: ExpressionNode) {
            val activity = PlayScene.shared.playActivity!!
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
            mathLog.addInfoFrom(activity, LevelScene.shared.currentLevel!!, Action.RULE)
            sendLog(mathLog, activity)
        }

        fun logPlace(currSteps: Float, currExpr: ExpressionNode, place: ExpressionNode) {
            val currExprStr = expressionToString(currExpr)
            val mathLog = MathGameLog(
                currStepsNumber = currSteps,
                nextStepsNumber = currSteps,
                currExpression = currExprStr,
                nextExpression = currExprStr,
                currSelectedPlace = expressionToString(place)
            )
            val activity = PlayScene.shared.playActivity!!
            mathLog.addInfoFrom(activity, LevelScene.shared.currentLevel!!, Action.PLACE)
            sendLog(mathLog, activity)
        }

        fun logStart() {
            val exprStr = expressionToString(LevelScene.shared.currentLevel!!.startExpression)
            val mathLog = MathGameLog(
                currStepsNumber = 0f,
                nextStepsNumber = 0f,
                currExpression = exprStr,
                nextExpression = exprStr
            )
            val activity = PlayScene.shared.playActivity!!
            mathLog.addInfoFrom(activity, LevelScene.shared.currentLevel!!, Action.START)
            sendLog(mathLog, activity)
        }

        fun logUndo(currSteps: Float, nextSteps: Float, currExpr: ExpressionNode,
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
            val activity = PlayScene.shared.playActivity!!
            mathLog.addInfoFrom(activity, LevelScene.shared.currentLevel!!, Action.UNDO)
            sendLog(mathLog, activity)
        }

        fun logRestart(currSteps: Float, currExpr: ExpressionNode, currPlace: ExpressionNode?) {
            val activity = PlayScene.shared.playActivity!!
            val curr = expressionToString(currExpr)
            val next = expressionToString(LevelScene.shared.currentLevel!!.startExpression)
            val place = if (currPlace == null) {
                ""
            } else {
                expressionToString(currPlace)
            }
            val mathLog = MathGameLog(
                currStepsNumber = currSteps,
                nextStepsNumber = 0f,
                currExpression = curr,
                nextExpression = next,
                currSelectedPlace = place
            )
            mathLog.addInfoFrom(activity, LevelScene.shared.currentLevel!!, Action.RESTART)
            sendLog(mathLog, activity)
            startTime = 0
        }

        fun logMenu(currSteps: Float, currExpr: ExpressionNode, currPlace: ExpressionNode?) {
            val activity = PlayScene.shared.playActivity!!
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
            mathLog.addInfoFrom(activity, LevelScene.shared.currentLevel!!, Action.MENU)
            sendLog(mathLog, activity)
            startTime = 0
        }

        fun logWin(currSteps: Float, award: Award) {
            val exprStr = expressionToString(LevelScene.shared.currentLevel!!.endExpression)
            val mathLog = MathGameLog(
                currStepsNumber = currSteps,
                nextStepsNumber = currSteps,
                currAwardCoef = award.coeff.toFloat(),
                currExpression = exprStr,
                nextExpression = exprStr
            )
            val activity = PlayScene.shared.playActivity!!
            mathLog.addInfoFrom(activity, LevelScene.shared.currentLevel!!, Action.WIN)
            sendLog(mathLog, activity)
            startTime = 0
        }

        fun logLoose(currSteps: Float, currExpr: ExpressionNode, currPlace: ExpressionNode?) {
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
            val activity = PlayScene.shared.playActivity!!
            mathLog.addInfoFrom(activity, LevelScene.shared.currentLevel!!, Action.LOOSE)
            sendLog(mathLog, activity)
            startTime = 0
        }

        fun getHwInfo(): String {
            return "Model: ${Build.MODEL}; Id: ${Build.ID}; Manufacture: ${Build.MANUFACTURER}; " +
                "Incremental: ${Build.VERSION.INCREMENTAL}; " +
                "Sdk: ${Build.VERSION.SDK}; Board: ${Build.BOARD}; Brand: ${Build.BRAND}; " +
                "Host: ${Build.HOST}; Fingerprint: ${Build.FINGERPRINT}; Version Code: ${Build.VERSION.RELEASE}"
        }

        fun logSign(context: Context) {
            val prefs = context.getSharedPreferences(Constants.storage, AppCompatActivity.MODE_PRIVATE)
            val id = prefs.getString(Constants.deviceId, "")!!
            val mathLog = MathGameLog(action = Action.SIGN.str, hardwareProperties = getHwInfo(),
                hardwareDeviceId = id)
            sendLog(mathLog, context)
        }

        fun logMark(context: Context, mark: Float, comment: String) {
            val mathLog = MathGameLog(action = Action.MARK.str, userMark = mark.toString(), userComment = comment)
            sendLog(mathLog, context, true)
        }

        fun logProblem(context: Context, comment: String) {
            val mathLog = MathGameLog(action = Action.PROBLEM.str, userComment = comment)
            sendLog(mathLog, context, true)
        }

        private fun sendLog(log: MathGameLog, context: Context, forced: Boolean = false) {
            Log.d("Statistics", "MathGameLog: $log}")
            setDefault(log, context)
            val prefs = context.getSharedPreferences(Constants.storage, AppCompatActivity.MODE_PRIVATE)
            //if (prefs.getBoolean(AuthInfo.STATISTICS.str, false) || forced) {
            sendOneLog(log, context)
            //}
        }

        private fun sendOneLog(log: MathGameLog, context: Context) {
            val req = RequestData()
            req.body = log.toString()
            req.headers["Content-type"] = "application/json"
            if (isConnectedToNetwork(context)) {
                Request.send(req)
            } else {
                Request.sendWithoutInternet(req)
            }
        }

        private fun isConnectedToNetwork(context: Context): Boolean {
            val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager?
            return connectivityManager?.activeNetworkInfo?.isConnected ?: false
        }

        private fun setDefault(log: MathGameLog, context: Context) {
            val prefs = context.getSharedPreferences(Constants.storage, AppCompatActivity.MODE_PRIVATE)
            val time = System.currentTimeMillis()
            log.deviceTs = time
            log.hardwareDeviceId = prefs.getString(Constants.deviceId, log.hardwareDeviceId)!!
            log.totalTimeMultCoef = prefs.getFloat(AuthInfo.TIME_COEFF.str, log.totalTimeMultCoef)
            log.totalAwardMultCoef = prefs.getFloat(AuthInfo.AWARD_COEFF.str, log.totalAwardMultCoef)
            log.login = prefs.getString(AuthInfo.LOGIN.str, log.login)!!
            log.name = prefs.getString(AuthInfo.NAME.str, log.name)!!
            log.surname = prefs.getString(AuthInfo.SURNAME.str, log.surname)!!
            log.secondName = prefs.getString(AuthInfo.SECOND_NAME.str, log.secondName )!!
            log.group = prefs.getString(AuthInfo.GROUP.str, log.group)!!
            log.institution = prefs.getString(AuthInfo.INSTITUTION.str, log.institution )!!
            log.age = prefs.getInt(AuthInfo.AGE.str, log.age)
            log.timeFromLastActionMS = time - lastActionTime
            lastActionTime = time
            if (startTime > 0) { // Level was created and set
                log.currTimeMS = time - startTime
                log.leftTimeMS = log.totalTimeMS - log.currTimeMS
            }
        }
    }
}