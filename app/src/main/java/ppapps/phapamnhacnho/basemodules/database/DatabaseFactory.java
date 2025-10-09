package ppapps.phapamnhacnho.basemodules.database;

import androidx.annotation.NonNull;

import ppapps.phapamnhacnho.model.AlarmModel;
import ppapps.phapamnhacnho.model.AlarmModelList;
import rx.Observable;

/**
 * Created by PhucHN on 3/30/2017
 */

public class DatabaseFactory {
    private DatabaseFactory mInstance = null;

    private DatabaseFactory() {
    }

    @NonNull
    public static Observable<Long> addAlarm(AlarmModel alarm) {
        return Observable.just(DatabaseManager.getInstance().
                addAlarm(alarm));
    }

    @NonNull
    public static Observable<AlarmModelList> getAlarms() {
        return Observable.just(DatabaseManager.getInstance().
                getAlarms());
    }

    @NonNull
    public static Observable<AlarmModel> getAlarm(long alarmCode) {
        return Observable.just(DatabaseManager.getInstance().
                getAlarm(alarmCode));
    }

    @NonNull
    public static Observable<Boolean> updateAlarm(AlarmModel alarm) {
        return Observable.just(DatabaseManager.getInstance().
                updateAlarm(alarm));
    }

    @NonNull
    public static Observable<Boolean> deleteAlarm(long alarmId) {
        return Observable.just(DatabaseManager.getInstance().
                deleteAlarm(alarmId));
    }

    public DatabaseFactory getInstance() {
        if (mInstance == null) {
            mInstance = new DatabaseFactory();
        }
        return mInstance;
    }
}
