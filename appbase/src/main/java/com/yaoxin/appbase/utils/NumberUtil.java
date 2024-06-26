package com.yaoxin.appbase.utils;

public class NumberUtil {
    public static String formartMoney(String money) {
        String formattedValue;

        if (!money.isEmpty()) {
            formattedValue = String.format("%.2f", (Double.parseDouble(money) / 100));
        } else {
            formattedValue = "0.00";
        }
        return formattedValue;
    }
    public static int formartUploadMoney(String money) {
        int formattedValue;

        if (!money.isEmpty()) {
            formattedValue = (int) (Double.parseDouble(money) * 100);
        } else {
            formattedValue = 0;
        }
        return formattedValue;
    }
}
