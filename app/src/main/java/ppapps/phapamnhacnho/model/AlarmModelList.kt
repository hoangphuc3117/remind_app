package ppapps.phapamnhacnho.model

class AlarmModelList : ArrayList<AlarmModel>() {
    fun removeAlarm(alarmCode: Long) {
        var i = 0
        var j = size - 1
        while (i <= j) {
            if (alarmCode == get(i).code) {
                removeAt(i)
                return
            }
            if (alarmCode == get(j).code) {
                removeAt(j)
                return
            }
            i++
            j--
        }
    }
}