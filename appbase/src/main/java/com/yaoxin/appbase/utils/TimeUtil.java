package com.yaoxin.appbase.utils;

import java.text.SimpleDateFormat;
import java.util.Date;

public class TimeUtil {
    /*
     * 将时间戳转换为时间
     */
    public static String stampToDate(String s) {
        String res = s;
        try {
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            long lt = new Long(s);
            Date date = new Date(lt);
            res = simpleDateFormat.format(date);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return res;
    }

    public static String stampToTime(String s) {
        String res = s;
        try {
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH:mm");
            long lt = new Long(s);
            Date date = new Date(lt);
            res = simpleDateFormat.format(date);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return res;
    }
}
