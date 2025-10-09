package ppapps.phapamnhacnho.basemodules.util;

import android.telecom.Call;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * Created by PhucHN on 3/29/2017
 */

public class TimeUtil {
    private static final String DATE_FORMAT_SERVER = "dd-MM-yyyy'T'kk:mm:ss";

    private static final String FORMAT_DATE = "dd-MM-yyyy";

    private static final String FORMAT_DATE_2 = "yyyy-MM-dd";

    private static final String FORMAT_DATE_TIME = "dd-MM-yyyy kk:mm";

    private static final String FORMAT_MONTH_YEAR = "MM-yyyy";

    private static final String FORMAT_YEAR = "yyyy";

    private static final String FORMAT_DAY = "dd";

    private static final String FORMAT_TIME = "kk:mm";

    public static String getCurrentDate() {
        Calendar calendar = Calendar.getInstance();
        Date today = calendar.getTime();
        SimpleDateFormat sdf = new SimpleDateFormat(FORMAT_DATE, Locale.getDefault());
        return sdf.format(today);
    }

    public static long getCurrentDateTimeStamp() {
        Calendar calendar = Calendar.getInstance();
        return calendar.getTimeInMillis();
    }

    public static String getCurrentTime() {
        Calendar calendar = Calendar.getInstance();
        Date today = calendar.getTime();
        SimpleDateFormat sdf = new SimpleDateFormat(FORMAT_TIME, Locale.getDefault());
        return sdf.format(today);
    }

    private static String concatDateAndTime(String date, String time) {
        return date + "T" + time + ":00";
    }

    public static String getDateFromTimeStamp(long timeStamp) {
        SimpleDateFormat outputFormat = new SimpleDateFormat(FORMAT_DATE, Locale.getDefault());
        return outputFormat.format(new Date(timeStamp));
    }

    public static String getDay(long timeStamp) {
        SimpleDateFormat outputFormat = new SimpleDateFormat(FORMAT_DAY, Locale.getDefault());
        return outputFormat.format(new Date(timeStamp));
    }

    public static String getTimeFromTimeStamp(long dateTime) {
        SimpleDateFormat outputFormat = new SimpleDateFormat(FORMAT_TIME, Locale.getDefault());
        return outputFormat.format(new Date(dateTime));
    }

    public static String getDateTimeFromTimeStamp(long dateTime) {
        SimpleDateFormat outputFormat = new SimpleDateFormat(FORMAT_DATE_TIME, Locale.getDefault());
        return outputFormat.format(new Date(dateTime));
    }

    public static String getTime(String time) {
        String[] times = time.split(" ");
        return times[0];
    }

    public static String getCurrentDay() {
        Calendar calendar = Calendar.getInstance();
        Date today = calendar.getTime();
        SimpleDateFormat sdf = new SimpleDateFormat(FORMAT_DAY, Locale.getDefault());
        return sdf.format(today);
    }

    public static String getCurrentMonthYear() {
        Calendar calendar = Calendar.getInstance();
        Date today = calendar.getTime();
        SimpleDateFormat sdf = new SimpleDateFormat(FORMAT_MONTH_YEAR, Locale.getDefault());
        return sdf.format(today);
    }

    public static String formatDate(String day, String month, String year) {
        if ("".equals(day) && "".equals(month)) {
            return year;
        } else if ("".equals(day)) {
            if (month.length() < 2)
                month = "0" + month;
            return month + "-" + year;
        } else {
            if (month.length() < 2)
                month = "0" + month;
            if (day.length() < 2)
                day = "0" + day;
            return day + "-" + month + "-" + year;
        }
    }

    /**
     * Get day
     *
     * @param date "dd-MM-yyyy"
     * @return dd
     */
    public static String getDay(String date) {
        String[] mDates = date.split("-");
        if (mDates.length > 0) {
            return mDates[0];
        }
        return "";
    }

    /**
     * Get year-month
     *
     * @param date "dd-MM-yyyy"
     * @return MM-yyyy
     */
    public static String getMonthYear(String date) {
        String[] mDates = date.split("-");
        if (mDates.length > 2) {
            return mDates[1] + "-" + mDates[2];

        }
        return "";
    }

    public static String getMonthYear(long date) {
        SimpleDateFormat outputFormat = new SimpleDateFormat(FORMAT_MONTH_YEAR, Locale.getDefault());
        return outputFormat.format(new Date(date));
    }

    /**
     * Format time
     *
     * @param hour   hour
     * @param minute minute
     * @param am_pm  am or pm
     * @return HH:MM AM
     */
    public static String formatTime(String hour, String minute, String am_pm) {
        return hour + ":" + minute + " " + am_pm;
    }

    /**
     * Format time without am and pm
     *
     * @param hour   hour
     * @param minute minute
     * @return HH:MM
     */
    public static String formatTime(String hour, String minute) {
        if (minute.length() < 2)
            minute = "0" + minute;
        if (hour.length() < 2)
            hour = "0" + hour;
        return hour + ":" + minute;
    }

//    /**
//     * Get time
//     *
//     * @param time hh:mm:ss am
//     * @return hh-mm
//     */
//    public static String getTime(String time) {
//        String[] times = time.split(":");
//        if (times.length > 2) {
//            return times[0] + ":" + times[1];
//        }
//        return "";
//    }


    public static String convertDateFormat1ToDateFormat2(String date) {
        SimpleDateFormat inputDateFormat = new SimpleDateFormat(FORMAT_DATE, Locale.getDefault());
        SimpleDateFormat outputDateFormat = new SimpleDateFormat(FORMAT_DATE_2, Locale.getDefault());
        try {
            return outputDateFormat.format(inputDateFormat.parse(date));
        } catch (ParseException e) {
            e.printStackTrace();
            return "";
        }
    }


    public static long convertDateToTimeStamp(String date) {
        String time = "00:00 am";
        return convertDateTimeToTimeStamp(date, time);
    }

    public static long convertEndDateToTimeStamp(String date) {
        String time = "11:59 pm";
        return convertDateTimeToTimeStamp(date, time);
    }

    public static long convertDateTimeToTimeStamp(String date, String time) {
        SimpleDateFormat dateFormat = new SimpleDateFormat(DATE_FORMAT_SERVER, Locale.getDefault());
        long lDateTime = 0;
        try {
            Date date1 = dateFormat.parse(concatDateAndTime(date, time));
            lDateTime = date1.getTime();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return lDateTime;
    }

    public static String formatDateFromTimeStamp(long timeStamp) {
        SimpleDateFormat outputFormat = new SimpleDateFormat(FORMAT_DATE_TIME, Locale.getDefault());
        return outputFormat.format(new Date(timeStamp));
    }

    public static boolean isTimeBeforeCurrentTime(long timeStamp) {
        Calendar calendar = Calendar.getInstance();
        Date currentTime = calendar.getTime();
        Date time = new Date(timeStamp);
        return time.before(currentTime);
    }
}
