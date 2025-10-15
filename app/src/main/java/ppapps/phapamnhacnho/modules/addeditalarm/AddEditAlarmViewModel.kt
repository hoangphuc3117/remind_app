package ppapps.phapamnhacnho.modules.addeditalarm

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import ppapps.phapamnhacnho.basemodules.database.DatabaseFactory
import ppapps.phapamnhacnho.model.AlarmModel
import rx.Subscriber
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers

class AddEditAlarmViewModel : ViewModel() {

    val liveDataSetAlarmResult = MutableLiveData<Long>()

    val liveDataUpdateAlarmResult = MutableLiveData<Boolean>()

    fun addAlarm(alarm: AlarmModel?) {
        Log.d("AddEditAlarmViewModel", "addAlarm called: name=${alarm?.name}, time=${alarm?.time}")
        DatabaseFactory.addAlarm(alarm)
            .subscribeOn(Schedulers.newThread())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(object : Subscriber<Long>() {
                override fun onCompleted() {
                    Log.d("AddEditAlarmViewModel", "addAlarm completed")
                }

                override fun onError(e: Throwable) {
                    Log.e("AddEditAlarmViewModel", "addAlarm error: ${e.message}", e)
                }
                
                override fun onNext(o: Long) {
                    Log.d("AddEditAlarmViewModel", "addAlarm success: alarm ID = $o")
                    liveDataSetAlarmResult.postValue(o)
                }
            })
    }

    fun updateAlarm(alarmModel: AlarmModel?) {
        Log.d("AddEditAlarmViewModel", "updateAlarm called: ${alarmModel?.code}")
        DatabaseFactory.updateAlarm(alarmModel)
            .subscribeOn(Schedulers.newThread())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(object : Subscriber<Boolean>() {
                override fun onCompleted() {
                    Log.d("AddEditAlarmViewModel", "updateAlarm completed")
                }
                override fun onError(e: Throwable) {
                    Log.e("AddEditAlarmViewModel", "updateAlarm error: ${e.message}", e)
                    liveDataUpdateAlarmResult.postValue(false)
                }
                override fun onNext(aBoolean: Boolean) {
                    Log.d("AddEditAlarmViewModel", "updateAlarm result: $aBoolean")
                    liveDataUpdateAlarmResult.postValue(aBoolean)
                }
            })
    }

    override fun onCleared() {
        super.onCleared()
    }
}