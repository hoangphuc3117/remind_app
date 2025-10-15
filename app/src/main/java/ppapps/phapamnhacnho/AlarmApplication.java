package ppapps.phapamnhacnho;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;

import androidx.appcompat.app.AppCompatDelegate;
import androidx.multidex.MultiDex;

import java.io.IOException;
import java.util.Locale;

import ppapps.phapamnhacnho.basemodules.database.DatabaseManager;

/**
 * Created by PhucHN1 on 3/28/2017
 */

public class AlarmApplication extends Application {

    private static final String PREFS_NAME = "AppSettings";
    private static final String KEY_DARK_MODE = "dark_mode";
    private static final String KEY_LANGUAGE = "language";

    @Override
    public void onCreate() {
        super.onCreate();
        MultiDex.install(this);
        
        // Apply dark mode setting
        applyDarkModeSetting();
        
        // Apply language setting
        applyLanguageSetting();
        
        DatabaseManager.getInstance().setContext(getApplicationContext());
        try {
            DatabaseManager.getInstance().initialDatabase();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void applyDarkModeSetting() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        boolean isDarkMode = prefs.getBoolean(KEY_DARK_MODE, false);
        
        if (isDarkMode) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }
    }

    private void applyLanguageSetting() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String languageCode = prefs.getString(KEY_LANGUAGE, "vi"); // Default to Vietnamese
        
        Locale locale = new Locale(languageCode);
        Locale.setDefault(locale);
        
        Configuration config = new Configuration(getResources().getConfiguration());
        config.setLocale(locale);
        
        getResources().updateConfiguration(config, getResources().getDisplayMetrics());
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
    }
}
