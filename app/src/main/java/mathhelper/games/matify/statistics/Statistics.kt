package mathhelper.games.matify.statistics

import android.content.Context
import android.net.ConnectivityManager
import android.os.Build
import android.util.Log
import com.twf.api.expressionToString
import com.twf.expressiontree.ExpressionNode
import com.twf.expressiontree.ExpressionSubstitution
import mathhelper.games.matify.BuildConfig
import mathhelper.games.matify.LevelScene
import mathhelper.games.matify.PlayScene
import mathhelper.games.matify.common.Storage
import mathhelper.games.matify.level.Award

class Statistics {
    companion object {
        private var startTime: Long = -1
        private var lastActionTime: Long = -1

        private var logArray: ArrayList<ActivityLog> = ArrayList()

        fun setStartTime() {
            startTime = System.currentTimeMillis()
            lastActionTime = startTime
        }

        fun getTimeDiff(): Long {
            return System.currentTimeMillis() - startTime
        }

        fun logRule(
            currSteps: Float, nextSteps: Float, currExpr: ExpressionNode, nextExpr: ExpressionNode,
            currRule: ExpressionSubstitution?, place: ExpressionNode
        ) {
            val activity = PlayScene.shared.playActivity!!
            val rule = if (currRule == null) {
                ""
            } else {
                expressionToString(currRule.left) + " : " + expressionToString(currRule.right)
            }
            val activityLog = ActivityLog(
                currStepsNumber = currSteps,
                nextStepsNumber = nextSteps,
                currentExpression = expressionToString(currExpr),
                nextExpression = expressionToString(nextExpr),
                appliedRule = rule,
                selectedPlace = expressionToString(place)
            )
            activityLog.additionalFrom(activity, LevelScene.shared.currentLevel!!, Action.RULE)
            sendLog(activityLog, activity)
        }

        fun logPlace(currSteps: Float, currExpr: ExpressionNode, place: ExpressionNode) {
            val currExprStr = expressionToString(currExpr)
            val activityLog = ActivityLog(
                currStepsNumber = currSteps,
                nextStepsNumber = currSteps,
                currentExpression = currExprStr,
                nextExpression = currExprStr,
                selectedPlace = expressionToString(place)
            )
            val activity = PlayScene.shared.playActivity!!
            activityLog.additionalFrom(activity, LevelScene.shared.currentLevel!!, Action.PLACE)
            sendLog(activityLog, activity)
        }

        fun logStart() {
            val exprStr = expressionToString(LevelScene.shared.currentLevel!!.startExpression)
            val activityLog = ActivityLog(
                currStepsNumber = 0f,
                nextStepsNumber = 0f,
                currentExpression = exprStr,
                nextExpression = exprStr
            )
            val activity = PlayScene.shared.playActivity!!
            activityLog.additionalFrom(activity, LevelScene.shared.currentLevel!!, Action.START)
            sendLog(activityLog, activity)
        }

        fun logUndo(
            currSteps: Float, nextSteps: Float, currExpr: ExpressionNode,
            nextExpr: ExpressionNode, currPlace: ExpressionNode?
        ) {
            val curr = expressionToString(currExpr)
            val next = expressionToString(nextExpr)
            val place = if (currPlace == null) {
                ""
            } else {
                expressionToString(currPlace)
            }
            val activityLog = ActivityLog(
                currStepsNumber = currSteps,
                nextStepsNumber = nextSteps,
                currentExpression = curr,
                nextExpression = next,
                selectedPlace = place
            )
            val activity = PlayScene.shared.playActivity!!
            activityLog.additionalFrom(activity, LevelScene.shared.currentLevel!!, Action.UNDO)
            sendLog(activityLog, activity)
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
            val activityLog = ActivityLog(
                currStepsNumber = currSteps,
                nextStepsNumber = 0f,
                currentExpression = curr,
                nextExpression = next,
                selectedPlace = place
            )
            activityLog.additionalFrom(activity, LevelScene.shared.currentLevel!!, Action.RESTART)
            sendLog(activityLog, activity)
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
            val activityLog = ActivityLog(
                currStepsNumber = currSteps,
                nextStepsNumber = currSteps,
                currentExpression = curr,
                nextExpression = curr,
                selectedPlace = place
            )
            activityLog.additionalFrom(activity, LevelScene.shared.currentLevel!!, Action.MENU)
            sendLog(activityLog, activity)
            startTime = 0
        }

        fun logWin(currSteps: Float, award: Award) {
            val exprStr = expressionToString(LevelScene.shared.currentLevel!!.endExpression)
            val activityLog = ActivityLog(
                currStepsNumber = currSteps,
                nextStepsNumber = currSteps,
                currAwardCoef = award.coeff.toFloat(),
                currentExpression = exprStr,
                nextExpression = exprStr
            )
            val activity = PlayScene.shared.playActivity!!
            activityLog.additionalFrom(activity, LevelScene.shared.currentLevel!!, Action.WIN)
            sendLog(activityLog, activity)
            startTime = 0
        }

        fun logLoose(currSteps: Float, currExpr: ExpressionNode, currPlace: ExpressionNode?) {
            val exprStr = expressionToString(currExpr)
            val place = if (currPlace == null) {
                ""
            } else {
                expressionToString(currPlace)
            }
            val activityLog = ActivityLog(
                currStepsNumber = currSteps,
                nextStepsNumber = currSteps,
                currentExpression = exprStr,
                nextExpression = exprStr,
                selectedPlace = place
            )
            val activity = PlayScene.shared.playActivity!!
            activityLog.additionalFrom(activity, LevelScene.shared.currentLevel!!, Action.LOOSE)
            sendLog(activityLog, activity)
            startTime = 0
        }

        fun getHwInfo(): String {
            return "Model: ${Build.MODEL}; Id: ${Build.ID}; Manufacture: ${Build.MANUFACTURER}; " +
                "Incremental: ${Build.VERSION.INCREMENTAL}; " +
                "Sdk: ${Build.VERSION.SDK}; Board: ${Build.BOARD}; Brand: ${Build.BRAND}; " +
                "Host: ${Build.HOST}; Fingerprint: ${Build.FINGERPRINT}; Version Code: ${Build.VERSION.RELEASE}"
        }

        fun logSign(context: Context) {
//            val activityLog = ActivityLog(action = Action.SIGN.str, hardwareProperties = getHwInfo(),
//                hardwareDeviceId = Storage.shared.deviceId(context))
//            sendLog(activityLog, context)
        }

        fun logMark(context: Context, mark: Float, comment: String) {
            //TODO: switch on '/api/comments/create'
            //val activityLog = ActivityLog(action = Action.MARK.str, userMark = mark.toString(), userComment = comment)
            //sendLog(activityLog, context, true)
        }

        fun logProblem(context: Context, comment: String) {
            //TODO: switch on '/api/comments/create'
//            val activityLog = ActivityLog(action = Action.PROBLEM.str, userComment = comment)
//            sendLog(activityLog, context, true)
        }

        private fun sendLog(log: ActivityLog, context: Context, forced: Boolean = false) {
            Log.d("Statistics", "ActivityLog: $log}")
            setDefault(log, context)
            sendOneLog(log, context)
        }

        private fun sendOneLog(log: ActivityLog, context: Context) {
            val req = RequestData(Pages.ACTIVITY_LOG.value, Storage.shared.serverToken(context))
            req.body = log.toString()
            Request.sendStatisticRequest(req)
        }

        private fun isConnectedToNetwork(context: Context): Boolean {
            val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager?
            return connectivityManager?.activeNetworkInfo?.isConnected ?: false
        }

        private fun setDefault(log: ActivityLog, context: Context) {
            val fullInfo = Storage.shared.getFullUserInfo(context)
            val time = System.currentTimeMillis()
            log.clientActionTime = time
            log.additionalData.put("hardwareDeviceId", Storage.shared.deviceId(context))
            log.additionalData.put("totalTimeMultCoef", fullInfo.coeffs.timeCoeff ?: log.totalTimeMultCoef)
            log.additionalData.put("totalAwardMultCoef", fullInfo.coeffs.awardCoeff ?: log.totalAwardMultCoef)
            log.applicationVersion = BuildConfig.VERSION_NAME
            log.timeFromLastActionMS = time - lastActionTime
            lastActionTime = time
            if (startTime > 0) { // Level was created and set
                log.currTimeMS = time - startTime
                log.additionalData.put("leftTimeMS", log.totalTimeMS - log.currTimeMS)
            }
        }
    }
}