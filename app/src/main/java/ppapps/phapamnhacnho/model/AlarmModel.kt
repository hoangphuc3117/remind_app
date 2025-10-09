package ppapps.phapamnhacnho.model

class AlarmModel {
    companion object {
        const val STATUS_IN_PROGRESS = 0

        const val STATUS_DONE = 1
    }

    var time: Long = 0

    var date: String? = null

    var name: String? = null

    var code: Long = 0

    var status = 0

    var uriFileFolder: String? = null

    var timeAlarm: Long = 0

    var fileType = 0

    var countLoopTimes = 0

    var loopTime = 0

    var fileIndex = 0

    var playingPosition = 0

    var playType = 0

    var loopType = 0

    var nextTime: Long = 0
}