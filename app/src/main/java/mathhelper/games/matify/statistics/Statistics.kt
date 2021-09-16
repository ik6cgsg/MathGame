package mathhelper.games.matify.statistics

import android.content.Context
import android.net.ConnectivityManager
import android.os.Build
import android.util.Log
import api.expressionToStructureString
import com.google.gson.Gson
import expressiontree.ExpressionNode
import expressiontree.ExpressionSubstitution
import mathhelper.games.matify.BuildConfig
import mathhelper.games.matify.LevelScene
import mathhelper.games.matify.PlayScene
import mathhelper.games.matify.common.Logger
import mathhelper.games.matify.common.Storage
import mathhelper.games.matify.level.Award
import java.sql.Timestamp

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
            currSteps: Double, nextSteps: Double, currExpr: ExpressionNode, nextExpr: ExpressionNode,
            currRule: ExpressionSubstitution?, places: List<ExpressionNode>
        ) {
            val activity = PlayScene.shared.playActivity!!
            val rule: MutableMap<String, String>? = if (currRule == null) { null } else {
                mutableMapOf(
                    "left" to expressionToStructureString(currRule.left),
                    "right" to expressionToStructureString(currRule.right)
                )
            }
            val activityLog = ActivityLog(
                currStepsNumber = currSteps,
                nextStepsNumber = nextSteps,
                currExpression = expressionToStructureString(currExpr),
                nextExpression = expressionToStructureString(nextExpr),
                appliedRule = rule,
                selectedPlace = (places.map { expressionToStructureString(it) }).toString()
            )
            activityLog.additionalFrom(LevelScene.shared.currentLevel!!, Action.RULE)
            sendLog(activityLog, activity)
        }

        fun logPlace(currSteps: Double, currExpr: ExpressionNode, places: List<ExpressionNode>) {
            val currExprStr = expressionToStructureString(currExpr)
            val activityLog = ActivityLog(
                currStepsNumber = currSteps,
                nextStepsNumber = currSteps,
                currExpression = currExprStr,
                nextExpression = currExprStr,
                selectedPlace = (places.map { expressionToStructureString(it) }).toString()
            )
            val activity = PlayScene.shared.playActivity!!
            activityLog.additionalFrom(LevelScene.shared.currentLevel!!, Action.PLACE)
            sendLog(activityLog, activity)
        }

        fun logInterim(currSteps: Double, currExpr: ExpressionNode) {
            val currExprStr = expressionToStructureString(currExpr)
            val activityLog = ActivityLog(
                currStepsNumber = currSteps,
                nextStepsNumber = currSteps,
                currExpression = currExprStr,
                nextExpression = currExprStr
            )
            val activity = PlayScene.shared.playActivity!!
            activityLog.additionalFrom(LevelScene.shared.currentLevel!!, Action.INTERIM)
            sendLog(activityLog, activity)
        }

        fun logStart() {
            val exprStr = expressionToStructureString(LevelScene.shared.currentLevel!!.startExpression)
            val activityLog = ActivityLog(
                currStepsNumber = 0.0,
                nextStepsNumber = 0.0,
                currExpression = exprStr,
                nextExpression = exprStr
            )
            val activity = PlayScene.shared.playActivity!!
            activityLog.additionalFrom(LevelScene.shared.currentLevel!!, Action.START)
            sendLog(activityLog, activity)
        }

        fun logUndo(
            currSteps: Double, nextSteps: Double, currExpr: ExpressionNode,
            nextExpr: ExpressionNode, currPlaces: List<ExpressionNode>
        ) {
            val curr = expressionToStructureString(currExpr)
            val next = expressionToStructureString(nextExpr)
            val places = if (currPlaces.isEmpty()) {
                listOf("")
            } else {
                currPlaces.map { expressionToStructureString(it) }
            }
            val activityLog = ActivityLog(
                currStepsNumber = currSteps,
                nextStepsNumber = nextSteps,
                currExpression = curr,
                nextExpression = next,
                selectedPlace = places.toString()
            )
            val activity = PlayScene.shared.playActivity!!
            activityLog.additionalFrom(LevelScene.shared.currentLevel!!, Action.UNDO)
            sendLog(activityLog, activity)
        }

        fun logRestart(currSteps: Double, currExpr: ExpressionNode, currPlaces: List<ExpressionNode>) {
            val activity = PlayScene.shared.playActivity!!
            val curr = expressionToStructureString(currExpr)
            val next = expressionToStructureString(LevelScene.shared.currentLevel!!.startExpression)
            val places = if (currPlaces.isEmpty()) {
                listOf("")
            } else {
                currPlaces.map { expressionToStructureString(it) }
            }
            val activityLog = ActivityLog(
                currStepsNumber = currSteps,
                nextStepsNumber = 0.0,
                currExpression = curr,
                nextExpression = next,
                selectedPlace = places.toString()
            )
            activityLog.additionalFrom(LevelScene.shared.currentLevel!!, Action.RESTART)
            sendLog(activityLog, activity)
            startTime = 0
        }

        fun logMenu(currSteps: Double, currExpr: ExpressionNode, currPlaces: List<ExpressionNode>) {
            val activity = PlayScene.shared.playActivity!!
            val curr = expressionToStructureString(currExpr)
            val places = if (currPlaces.isEmpty()) {
                listOf("")
            } else {
                currPlaces.map { expressionToStructureString(it) }
            }
            val activityLog = ActivityLog(
                currStepsNumber = currSteps,
                nextStepsNumber = currSteps,
                currExpression = curr,
                nextExpression = curr,
                selectedPlace = places.toString()
            )
            activityLog.additionalFrom(LevelScene.shared.currentLevel!!, Action.MENU)
            sendLog(activityLog, activity)
            startTime = 0
        }

        fun logWin(currSteps: Double/*, award: Award*/) {
            val exprStr = expressionToStructureString(LevelScene.shared.currentLevel!!.endExpression)
            val activityLog = ActivityLog(
                currStepsNumber = currSteps,
                nextStepsNumber = currSteps,
                currExpression = exprStr,
                nextExpression = exprStr
            )
            val activity = PlayScene.shared.playActivity!!
            activityLog.additionalFrom(LevelScene.shared.currentLevel!!, Action.WIN)
            sendLog(activityLog, activity)
            startTime = 0
        }

        fun logLoose(currSteps: Double, currExpr: ExpressionNode, currPlaces: List<ExpressionNode>) {
            val exprStr = expressionToStructureString(currExpr)
            val places = if (currPlaces.isEmpty()) {
                listOf("")
            } else {
                currPlaces.map { expressionToStructureString(it) }
            }
            val activityLog = ActivityLog(
                currStepsNumber = currSteps,
                nextStepsNumber = currSteps,
                currExpression = exprStr,
                nextExpression = exprStr,
                selectedPlace = places.toString()
            )
            val activity = PlayScene.shared.playActivity!!
            activityLog.additionalFrom(LevelScene.shared.currentLevel!!, Action.LOOSE)
            sendLog(activityLog, activity)
            startTime = 0
        }

        private fun getHwInfo(): String {
            return "Model: ${Build.MODEL}; Id: ${Build.ID}; Manufacture: ${Build.MANUFACTURER}; " +
                "Incremental: ${Build.VERSION.INCREMENTAL}; " +
                "Sdk: ${Build.VERSION.SDK}; Board: ${Build.BOARD}; Brand: ${Build.BRAND}; " +
                "Host: ${Build.HOST}; Fingerprint: ${Build.FINGERPRINT}; Version Code: ${Build.VERSION.RELEASE}"
        }

        fun logSign(context: Context) {
            val activityLog = ActivityLog(
                activityTypeCode = Action.SIGN.str,
                otherData = mutableMapOf(
                    "hardwareProperties" to getHwInfo(),
                    "hardwareDeviceId" to Storage.shared.deviceId(context)
                )
            )
            sendLog(activityLog, context)
        }

        private fun sendLog(log: ActivityLog, context: Context, forced: Boolean = false) {
            Logger.d("Statistics", "ActivityLog: $log}")
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
            log.clientActionTs = Timestamp(time)
            val otherData = mutableMapOf<String, Any?>()
            otherData["hardwareDeviceId"] = Storage.shared.deviceId(context)
            otherData["applicationVersion"] = BuildConfig.VERSION_NAME
            otherData["userId"] = fullInfo.uuid
            log.timeFromLastActionMs = time - lastActionTime
            lastActionTime = time
            if (startTime > 0) { // Level was created and set
                log.currTimeMs = PlayScene.shared.currentTime * 1000
                val timeFromLastStart = time - startTime
                otherData["leftTimeMS"] = LevelScene.shared.currentLevel!!.time.toLong() * 1000 - timeFromLastStart
                otherData["timeFromLevelLastStart"] = timeFromLastStart
            }
            log.otherData = otherData
        }
    }
}