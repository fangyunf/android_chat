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

    public static String formartLocalMoney(String money) {
        String formattedValue;

        if (!money.isEmpty()) {
            formattedValue = String.format("%.2f", Double.parseDouble(money));
        } else {
            formattedValue = "0.00";
        }
        return formattedValue;
    }
    public static int formartUploadMoney(String money) {
        int formattedValue;

        if (money != null && !money.isEmpty()) {
            double value = Double.parseDouble(money);
            formattedValue = (int) Math.round(value * 100);
        } else {
            formattedValue = 0;
        }
        return formattedValue;
    }
}
