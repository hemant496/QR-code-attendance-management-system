package utils;

import java.text.SimpleDateFormat;
import java.util.Date;

public class DateUtility {
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");
    private static final SimpleDateFormat TIME_FORMAT = new SimpleDateFormat("HH:mm:ss");
    private static final SimpleDateFormat DATE_TIME_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    public static String getCurrentDate() {
        return DATE_FORMAT.format(new Date());
    }

    public static String getCurrentTime() {
        return TIME_FORMAT.format(new Date());
    }

    public static String getCurrentDateTime() {
        return DATE_TIME_FORMAT.format(new Date());
    }

    public static String formatDate(Date date) {
        if (date == null) return "";
        return DATE_FORMAT.format(date);
    }

    public static String formatTime(Date time) {
        if (time == null) return "";
        return TIME_FORMAT.format(time);
    }

    public static String formatDateTime(Date dateTime) {
        if (dateTime == null) return "";
        return DATE_TIME_FORMAT.format(dateTime);
    }
}
