package ppapps.phapamnhacnho;

import android.app.Application;
import android.content.Context;

import androidx.multidex.MultiDex;

import java.io.IOException;

import ppapps.phapamnhacnho.basemodules.database.DatabaseManager;

/**
 * Created by PhucHN1 on 3/28/2017
 */

public class AlarmApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        MultiDex.install(this);
        DatabaseManager.getInstance().setContext(getApplicationContext());
        try {
            DatabaseManager.getInstance().initialDatabase();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
    }
}
