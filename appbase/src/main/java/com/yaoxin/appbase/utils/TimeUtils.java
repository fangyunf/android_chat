package com.yaoxin.appbase.utils;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class TimeUtils {
    public static String getTodayDateString(String format) {
        Calendar calendar = Calendar.getInstance();
        Date todayDate = calendar.getTime();
        SimpleDateFormat formatter = new SimpleDateFormat(format);
        return formatter.format(todayDate);
    }
}
