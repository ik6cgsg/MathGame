package spbpu.hsamcp.mathgame.statistics

import spbpu.hsamcp.mathgame.BuildConfig.LOG_STAT

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

        fun sendLog(log: MathGameLog) {
            if (LOG_STAT) {
                // TODO: if internet
                val req = RequestData()
                setDefault(log)
                req.body = log.toString()
                req.headers["Content-type"] = "application/json"
                Request.doRequest(req)
                // TODO: else
                logArray.add(log)
            }
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