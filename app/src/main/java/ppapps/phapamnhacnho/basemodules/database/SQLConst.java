package ppapps.phapamnhacnho.basemodules.database;

/**
 * Created by PhucHN1 on 3/28/2017
 */

class SQLConst {
    static final String DATABASE_NAME = "phapamnhacnho";

    static final String TABLE_ALARM = "PHAP_AM_NHAC_NHO";

    static final String ALARM_CODE = "AlarmCode";

    static final String ALARM_NAME = "AlarmName";

    static final String ALARM_TIME = "AlarmTime";

    static final String ALARM_FILE = "AlarmFile";

    static final String ALARM_FILE_TYPE = "AlarmFileType";

    static final String ALARM_TYPE = "AlarmType";

    static final String ALARM_TIME_ALARM = "AlarmTypeAlarm";

    static final String ALARM_LOOP = "AlarmLoop";

    static final String ALARM_LOOP_TIMES = "AlarmLoopTimes";

    static final String ALARM_COUNT_LOOP_TIMES = "AlarmCountLoopTimes";

    static final String ALARM_FILE_INDEX = "AlarmFileIndex";

    static final String ALARM_PLAYING_POSITION = "AlarmPlayingPosition";

    static final String ALARM_PLAY_TYPE = "AlarmPlayType";

    static final String ALARM_LOOP_TYPE = "AlarmLoopType";
    
    static final String ALARM_SELECTED_DAYS = "AlarmSelectedDays";

    static final String ALARM_STATUS = "AlarmStatus";

    private static final String LF = "\n";

    static final String SQL_CHECK_TABLE_EXIST = "SELECT DISTINCT tbl_name" + LF +
            "FROM sqlite_master" + LF + "WHERE tbl_name = ? ;";

    static final String SQL_CREATE_TABLE_ALARM = "CREATE TABLE " + TABLE_ALARM + " (" +
            "'" + ALARM_CODE + "' INTEGER PRIMARY KEY AUTOINCREMENT," +
            "'" + ALARM_NAME + "' TEXT," +
            "'" + ALARM_TIME + "' INTEGER," +
            "'" + ALARM_FILE + "' TEXT," +
            "'" + ALARM_FILE_TYPE + "' INTEGER," +
            "'" + ALARM_TYPE + "' INTEGER," +
            "'" + ALARM_TIME_ALARM + "' INTEGER," +
            "'" + ALARM_LOOP + "' INTEGER," +
            "'" + ALARM_LOOP_TIMES + "' INTEGER," +
            "'" + ALARM_COUNT_LOOP_TIMES + "' INTEGER," +
            "'" + ALARM_STATUS + "' INTEGER," +
            "'" + ALARM_FILE_INDEX + "' INTEGER," +
            "'" + ALARM_PLAYING_POSITION + "' INTEGER," +
            "'" + ALARM_PLAY_TYPE + "' INTEGER," +
            "'" + ALARM_LOOP_TYPE + "' INTEGER," +
            "'" + ALARM_SELECTED_DAYS + "' INTEGER DEFAULT 0);";


    static final String SQL_GET_ALARMS = "Select * " + LF +
            "From " + TABLE_ALARM;

    static final String SQL_GET_ALARM = "Select * " + LF +
            "From " + TABLE_ALARM + LF +
            "Where " + ALARM_CODE + "=?;";
}
