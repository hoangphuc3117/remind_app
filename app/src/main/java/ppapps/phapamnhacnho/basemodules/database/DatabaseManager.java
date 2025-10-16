package ppapps.phapamnhacnho.basemodules.database;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;


import java.io.File;
import java.io.IOException;

import ppapps.phapamnhacnho.model.AlarmModel;
import ppapps.phapamnhacnho.model.AlarmModelList;

/**
 * Created by PhucHN on 5/6/2016
 */
public class DatabaseManager {
    private static DatabaseManager databaseConnection = null;

    private SQLiteDatabase database = null;

    private Context mContext;

    private DatabaseManager() {
    }

    public static DatabaseManager getInstance() {
        if (databaseConnection == null) {
            databaseConnection = new DatabaseManager();
        }
        return databaseConnection;
    }

    public Context getContext() {
        return mContext;
    }

    public void setContext(Context context) {
        this.mContext = context;
    }

    @SuppressLint("WrongConstant")
    private synchronized SQLiteDatabase openDatabase() {
        return mContext.openOrCreateDatabase(SQLConst.DATABASE_NAME, SQLiteDatabase.CREATE_IF_NECESSARY, null);
    }

    public void initialDatabase() throws IOException {
        File databaseFile = mContext.getApplicationContext()
                .getDatabasePath(SQLConst.DATABASE_NAME);
        database = openDatabase();

        if (!isTableExist(SQLConst.TABLE_ALARM)) {
            database.execSQL(SQLConst.SQL_CREATE_TABLE_ALARM);
        } else {
            // Migration: Add ALARM_SELECTED_DAYS column if it doesn't exist
            addColumnIfNotExists(SQLConst.TABLE_ALARM, SQLConst.ALARM_SELECTED_DAYS, "INTEGER DEFAULT 0");
        }
    }
    
    private void addColumnIfNotExists(String tableName, String columnName, String columnType) {
        Cursor cursor = null;
        try {
            cursor = database.rawQuery("PRAGMA table_info(" + tableName + ")", null);
            boolean columnExists = false;
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    String name = cursor.getString(cursor.getColumnIndex("name"));
                    if (columnName.equals(name)) {
                        columnExists = true;
                        break;
                    }
                } while (cursor.moveToNext());
            }
            
            if (!columnExists) {
                String alterTableQuery = "ALTER TABLE " + tableName + " ADD COLUMN '" + columnName + "' " + columnType;
                database.execSQL(alterTableQuery);
                android.util.Log.d("DatabaseManager", "Added column: " + columnName + " to table: " + tableName);
            }
        } catch (Exception e) {
            android.util.Log.e("DatabaseManager", "Error adding column: " + columnName, e);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    private boolean isTableExist(String tableName) {
        String[] params = new String[]{tableName};
        Cursor cursor = database.rawQuery(SQLConst.SQL_CHECK_TABLE_EXIST, params);
        if (cursor != null) {
            if (cursor.getCount() > 0) {
                cursor.close();
                return true;
            }
            cursor.close();
        }
        return false;
    }

    long addAlarm(AlarmModel alarm) {
        if (database == null || !database.isOpen()) {
            database = openDatabase();
        }
        try {
            Log.d("DatabaseManager", "addAlarm: name=" + alarm.getName() + ", time=" + alarm.getTime());
            database.beginTransaction();
            ContentValues contentValues = new ContentValues();
            contentValues.put(SQLConst.ALARM_NAME, alarm.getName());
            contentValues.put(SQLConst.ALARM_TIME, alarm.getTime());
            contentValues.put(SQLConst.ALARM_STATUS, alarm.getStatus());
            contentValues.put(SQLConst.ALARM_FILE, alarm.getUriFileFolder());
            contentValues.put(SQLConst.ALARM_TIME_ALARM, alarm.getTimeAlarm());
            contentValues.put(SQLConst.ALARM_FILE_TYPE, alarm.getFileType());
            contentValues.put(SQLConst.ALARM_LOOP_TIMES, alarm.getLoopTime());
            contentValues.put(SQLConst.ALARM_COUNT_LOOP_TIMES, alarm.getCountLoopTimes());
            contentValues.put(SQLConst.ALARM_FILE_INDEX, alarm.getFileIndex());
            contentValues.put(SQLConst.ALARM_PLAYING_POSITION, alarm.getPlayingPosition());
            contentValues.put(SQLConst.ALARM_PLAY_TYPE, alarm.getPlayType());
            contentValues.put(SQLConst.ALARM_LOOP_TYPE, alarm.getLoopType());
            contentValues.put(SQLConst.ALARM_SELECTED_DAYS, alarm.getSelectedDays());
            long id = database.insert(SQLConst.TABLE_ALARM, null, contentValues);
            database.setTransactionSuccessful();
            Log.d("DatabaseManager", "addAlarm: inserted with id=" + id);
            return id;
        } catch (Exception ex) {
            Log.e("DatabaseManager", "addAlarm error: " + ex.getMessage(), ex);
            return -1;
        } finally {
            database.endTransaction();
        }
    }

    AlarmModelList getAlarms() {
        Cursor cursor = null;
        if (database == null || !database.isOpen()) {
            database = openDatabase();
        }
        AlarmModelList alarmList = new AlarmModelList();
        try {
            cursor = database.rawQuery(SQLConst.SQL_GET_ALARMS, null);
            if (cursor.moveToFirst()) {
                do {
                    AlarmModel alarm = new AlarmModel();
                    String alarmName = cursor.getString(cursor.getColumnIndex(SQLConst.ALARM_NAME));
                    alarm.setName(alarmName);
                    long alarmCode = cursor.getLong(cursor.getColumnIndex(SQLConst.ALARM_CODE));
                    alarm.setCode(alarmCode);
                    long time = cursor.getLong(cursor.getColumnIndex(SQLConst.ALARM_TIME));
                    alarm.setTime(time);
                    int status = cursor.getInt(cursor.getColumnIndex(SQLConst.ALARM_STATUS));
                    alarm.setStatus(status);
                    String file = cursor.getString(cursor.getColumnIndex(SQLConst.ALARM_FILE));
                    alarm.setUriFileFolder(file);
                    long timeAlarm = cursor.getLong(cursor.getColumnIndex(SQLConst.ALARM_TIME_ALARM));
                    alarm.setTimeAlarm(timeAlarm);
                    int typeFile = cursor.getInt(cursor.getColumnIndex(SQLConst.ALARM_FILE_TYPE));
                    alarm.setFileType(typeFile);
                    int loopTimes = cursor.getInt(cursor.getColumnIndex(SQLConst.ALARM_LOOP_TIMES));
                    alarm.setLoopTime(loopTimes);
                    int countLoopTimes = cursor.getInt(cursor.getColumnIndex(SQLConst.ALARM_COUNT_LOOP_TIMES));
                    alarm.setCountLoopTimes(countLoopTimes);
                    int fileIndex = cursor.getInt(cursor.getColumnIndex(SQLConst.ALARM_FILE_INDEX));
                    alarm.setFileIndex(fileIndex);
                    int playingPosition = cursor.getInt(cursor.getColumnIndex(SQLConst.ALARM_PLAYING_POSITION));
                    alarm.setPlayingPosition(playingPosition);
                    int playType = cursor.getInt(cursor.getColumnIndex(SQLConst.ALARM_PLAY_TYPE));
                    alarm.setPlayType(playType);
                    int loopType = cursor.getInt(cursor.getColumnIndex(SQLConst.ALARM_LOOP_TYPE));
                    alarm.setLoopType(loopType);
                    int selectedDays = cursor.getInt(cursor.getColumnIndex(SQLConst.ALARM_SELECTED_DAYS));
                    alarm.setSelectedDays(selectedDays);
                    alarmList.add(alarm);
                } while (cursor.moveToNext());
            }
        } catch (Exception ex) {
            Log.d("ERROR", "Cannot get alarms from database");
            Log.d("ERROR", ex.getMessage());
        } finally {
            if (cursor != null)
                cursor.close();
        }
        return alarmList;
    }

    AlarmModel getAlarm(long alarmCode) {
        Cursor cursor = null;
        if (database == null || !database.isOpen()) {
            database = openDatabase();
        }
        AlarmModel alarm = null;
        try {
            cursor = database.rawQuery(SQLConst.SQL_GET_ALARM, new String[]{String.valueOf(alarmCode)});
            if (cursor.moveToFirst()) {
                alarm = new AlarmModel();
                String alarmName = cursor.getString(cursor.getColumnIndex(SQLConst.ALARM_NAME));
                alarm.setName(alarmName);
                long code = cursor.getLong(cursor.getColumnIndex(SQLConst.ALARM_CODE));
                alarm.setCode(code);
                long time = cursor.getLong(cursor.getColumnIndex(SQLConst.ALARM_TIME));
                alarm.setTime(time);
                String file = cursor.getString(cursor.getColumnIndex(SQLConst.ALARM_FILE));
                alarm.setUriFileFolder(file);
                long timeAlarm = cursor.getLong(cursor.getColumnIndex(SQLConst.ALARM_TIME_ALARM));
                alarm.setTimeAlarm(timeAlarm);
                int typeFile = cursor.getInt(cursor.getColumnIndex(SQLConst.ALARM_FILE_TYPE));
                alarm.setFileType(typeFile);
                int loopTimes = cursor.getInt(cursor.getColumnIndex(SQLConst.ALARM_LOOP_TIMES));
                alarm.setLoopTime(loopTimes);
                int countLoopTimes = cursor.getInt(cursor.getColumnIndex(SQLConst.ALARM_COUNT_LOOP_TIMES));
                alarm.setCountLoopTimes(countLoopTimes);
                int fileIndex = cursor.getInt(cursor.getColumnIndex(SQLConst.ALARM_FILE_INDEX));
                alarm.setFileIndex(fileIndex);
                int playingPosition = cursor.getInt(cursor.getColumnIndex(SQLConst.ALARM_PLAYING_POSITION));
                alarm.setPlayingPosition(playingPosition);
                int playType = cursor.getInt(cursor.getColumnIndex(SQLConst.ALARM_PLAY_TYPE));
                alarm.setPlayType(playType);
                int loopType = cursor.getInt(cursor.getColumnIndex(SQLConst.ALARM_LOOP_TYPE));
                alarm.setLoopType(loopType);
                int selectedDays = cursor.getInt(cursor.getColumnIndex(SQLConst.ALARM_SELECTED_DAYS));
                alarm.setSelectedDays(selectedDays);
            }
        } catch (Exception ex) {
            Log.d("ERROR", "Cannot get alarms from database");
            Log.d("ERROR", ex.getMessage());
        } finally {
            if (cursor != null)
                cursor.close();
        }
        return alarm;
    }

    public boolean updateAlarm(AlarmModel alarmModel) {
        if (database == null || !database.isOpen()) {
            database = openDatabase();
        }
        try {
            Log.d("DatabaseManager", "updateAlarm: code=" + alarmModel.getCode() + ", name=" + alarmModel.getName());
            database.beginTransaction();
            ContentValues contentValues = new ContentValues();
            contentValues.put(SQLConst.ALARM_NAME, alarmModel.getName());
            contentValues.put(SQLConst.ALARM_TIME, alarmModel.getTime());
            contentValues.put(SQLConst.ALARM_STATUS, alarmModel.getStatus());
            contentValues.put(SQLConst.ALARM_FILE, alarmModel.getUriFileFolder());
            contentValues.put(SQLConst.ALARM_TIME_ALARM, alarmModel.getTimeAlarm());
            contentValues.put(SQLConst.ALARM_FILE_TYPE, alarmModel.getFileType());
            contentValues.put(SQLConst.ALARM_LOOP_TIMES, alarmModel.getLoopTime());
            contentValues.put(SQLConst.ALARM_COUNT_LOOP_TIMES, alarmModel.getCountLoopTimes());
            contentValues.put(SQLConst.ALARM_PLAY_TYPE, alarmModel.getPlayType());
            contentValues.put(SQLConst.ALARM_LOOP_TYPE, alarmModel.getLoopType());
            contentValues.put(SQLConst.ALARM_FILE_INDEX, alarmModel.getFileIndex());
            contentValues.put(SQLConst.ALARM_PLAYING_POSITION, alarmModel.getPlayingPosition());
            contentValues.put(SQLConst.ALARM_SELECTED_DAYS, alarmModel.getSelectedDays());
            int value = database.update(SQLConst.TABLE_ALARM, contentValues, SQLConst.ALARM_CODE + "=?",
                    new String[]{String.valueOf(alarmModel.getCode())});
            database.setTransactionSuccessful();
            Log.d("DatabaseManager", "updateAlarm: rows updated=" + value);
            return value != 0;
        } catch (Exception ex) {
            Log.e("DatabaseManager", "updateAlarm error: " + ex.getMessage(), ex);
            return false;
        } finally {
            database.endTransaction();
        }
    }

    public boolean deleteAlarm(long alarmId) {
        if (database == null || !database.isOpen()) {
            database = openDatabase();
        }
        try {
            database.beginTransaction();
            database.delete(SQLConst.TABLE_ALARM, SQLConst.ALARM_CODE + "=?", new String[]{alarmId + ""});
            database.setTransactionSuccessful();
        } catch (Exception ex) {
            return false;
        } finally {
            database.endTransaction();
        }
        return true;
    }

}
