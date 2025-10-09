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
        DatabaseFactory.addAlarm(alarm)
            .subscribeOn(Schedulers.newThread())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(object : Subscriber<Long>() {
                override fun onCompleted() {
                    Log.d("Add Alarm", "Success")
                }

                override fun onError(e: Throwable) {}
                override fun onNext(o: Long) {
                    liveDataSetAlarmResult.postValue(o)
                }
            })
    }

    fun updateAlarm(alarmModel: AlarmModel?) {
        DatabaseFactory.updateAlarm(alarmModel)
            .subscribeOn(Schedulers.newThread())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(object : Subscriber<Boolean>() {
                override fun onCompleted() {}
                override fun onError(e: Throwable) {}
                override fun onNext(aBoolean: Boolean) {
                    liveDataUpdateAlarmResult.postValue(aBoolean)
                }
            })
    }

    override fun onCleared() {
        super.onCleared()
    }
}