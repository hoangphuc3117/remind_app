package ppapps.phapamnhacnho.modules.alarmlist

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import ppapps.phapamnhacnho.basemodules.database.DatabaseFactory
import ppapps.phapamnhacnho.model.AlarmModelList
import rx.Subscriber
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers

class AlarmViewModel : ViewModel() {

    var liveDataAlarms = MutableLiveData<AlarmModelList>()

    var liveDataLoading = MutableLiveData<Boolean>()

    fun getAlarms() {
        liveDataLoading.postValue(true)
        DatabaseFactory.getAlarms()
            .subscribeOn(Schedulers.newThread())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(object : Subscriber<AlarmModelList?>() {
                override fun onCompleted() {}
                override fun onError(e: Throwable) {}
                override fun onNext(alarmList: AlarmModelList?) {
                    liveDataLoading.postValue(false)
                    liveDataAlarms.postValue(alarmList)
                }
            })
    }

    fun deleteAlarm(alarmCode: Long) {
        DatabaseFactory.deleteAlarm(alarmCode)
            .subscribeOn(Schedulers.newThread())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(object : Subscriber<Boolean>() {
                override fun onCompleted() {}
                override fun onError(e: Throwable) {}
                override fun onNext(success: Boolean) {
                }
            })
    }

    override fun onCleared() {
        super.onCleared()
    }
}