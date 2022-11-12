package mathhelper.games.matify.statistics

import android.content.Context
import android.net.ConnectivityManager
import android.os.Build
import mathhelper.twf.api.expressionToStructureString
import mathhelper.twf.expressiontree.ExpressionNode
import mathhelper.twf.expressiontree.ExpressionSubstitution
import mathhelper.games.matify.BuildConfig
import mathhelper.games.matify.LevelScene
import mathhelper.games.matify.PlayScene
import mathhelper.games.matify.common.*
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
            val activity = PlayScene.shared.listenerRef.get()?:return
            sendLog(activityLog, activity.ctx)
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
            activityLog.additionalFrom(LevelScene.shared.currentLevel!!, Action.PLACE)
            val activity = PlayScene.shared.listenerRef.get()?:return
            sendLog(activityLog, activity.ctx)
        }

        fun logInterim(currSteps: Double, currExpr: ExpressionNode) {
            val currExprStr = expressionToStructureString(currExpr)
            val activityLog = ActivityLog(
                currStepsNumber = currSteps,
                nextStepsNumber = currSteps,
                currExpression = currExprStr,
                nextExpression = currExprStr
            )
            activityLog.additionalFrom(LevelScene.shared.currentLevel!!, Action.INTERIM)
            val activity = PlayScene.shared.listenerRef.get()?:return
            sendLog(activityLog, activity.ctx)
        }

        fun logStart() {
            val exprStr = expressionToStructureString(LevelScene.shared.currentLevel!!.startExpression)
            val activityLog = ActivityLog(
                currStepsNumber = 0.0,
                nextStepsNumber = 0.0,
                currExpression = exprStr,
                nextExpression = exprStr
            )
            activityLog.additionalFrom(LevelScene.shared.currentLevel!!, Action.START)
            val activity = PlayScene.shared.listenerRef.get()?:return
            sendLog(activityLog, activity.ctx)
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
            activityLog.additionalFrom(LevelScene.shared.currentLevel!!, Action.UNDO)
            val activity = PlayScene.shared.listenerRef.get()?:return
            sendLog(activityLog, activity.ctx)
        }

        fun logRestart(currSteps: Double, currExpr: ExpressionNode, currPlaces: List<ExpressionNode>) {
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
            startTime = 0
            activityLog.additionalFrom(LevelScene.shared.currentLevel!!, Action.RESTART)
            val activity = PlayScene.shared.listenerRef.get()?:return
            sendLog(activityLog, activity.ctx)
        }

        fun logMenu(currSteps: Double, currExpr: ExpressionNode, currPlaces: List<ExpressionNode>) {
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
            startTime = 0
            activityLog.additionalFrom(LevelScene.shared.currentLevel!!, Action.MENU)
            val activity = PlayScene.shared.listenerRef.get()?:return
            sendLog(activityLog, activity.ctx)
        }

        fun logWin(currSteps: Double, currExpr: ExpressionNode/*, award: Award*/) {
            val exprStr = expressionToStructureString(currExpr)
            val activityLog = ActivityLog(
                currStepsNumber = currSteps,
                nextStepsNumber = currSteps,
                currExpression = exprStr,
                nextExpression = exprStr
            )
            startTime = 0
            activityLog.additionalFrom(LevelScene.shared.currentLevel!!, Action.WIN)
            val activity = PlayScene.shared.listenerRef.get()?:return
            sendLog(activityLog, activity.ctx)
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
            startTime = 0
            activityLog.additionalFrom(LevelScene.shared.currentLevel!!, Action.LOOSE)
            val activity = PlayScene.shared.listenerRef.get()?:return
            sendLog(activityLog, activity.ctx)
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
                    "hardwareDeviceId" to Storage.shared.deviceId()
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
            val req = RequestData(RequestPage.ACTIVITY_LOG, Storage.shared.serverToken())
            req.body = log.toString()
            Request.sendStatisticRequest(req)
        }

        private fun isConnectedToNetwork(context: Context): Boolean {
            val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager?
            return connectivityManager?.activeNetworkInfo?.isConnected ?: false
        }

        private fun setDefault(log: ActivityLog, context: Context) {
            val fullInfo = Storage.shared.getFullUserInfo()
            val time = System.currentTimeMillis()
            log.clientActionTs = Timestamp(time)
            val otherData = mutableMapOf<String, Any?>()
            otherData["hardwareDeviceId"] = Storage.shared.deviceId()
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