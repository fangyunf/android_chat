package com.yaoxin.appbase.utils;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.util.DisplayMetrics;

public class DensityUtils {

    public static final int VALID_VALUE_ZERO = 0;
    public static final float MIN_TEXT_SIZE = 5.0F;
    public static float RATIO = 0.95F;

    public DensityUtils() {
    }

    public static int px2dipRatio(Context context, float pxValue) {
        float scale = getScreenDendity(context) * RATIO;
        return (int)(pxValue / scale + 0.5F);
    }

    public static int dip2pxRatio(Context context, float dpValue) {
        float scale = getScreenDendity(context) * RATIO;
        return (int)(dpValue * scale + 0.5F);
    }

    public static int px2dip(Context context, float pxValue) {
        float scale = getScreenDendity(context);
        return (int)(pxValue / scale + 0.5F);
    }

    public static int dip2px(Context context, float dpValue) {
        float scale = getScreenDendity(context);
        return (int)(dpValue * scale + 0.5F);
    }

    public static int getScreenWidth(Context context) {
        return context.getResources().getDisplayMetrics().widthPixels;
    }

    public static int getScreenWidthDp(Context context) {
        float scale = getScreenDendity(context);
        return (int)((float)context.getResources().getDisplayMetrics().widthPixels / scale);
    }

    public static int getScreenHeight(Context context) {
        return context.getResources().getDisplayMetrics().heightPixels;
    }

    public static int getScreenHeightDp(Context context) {
        float scale = getScreenDendity(context);
        return (int)((float)context.getResources().getDisplayMetrics().heightPixels / scale);
    }

    public static float getScreenDendity(Context context) {
        return context.getResources().getDisplayMetrics().density;
    }

    public static int getStatusBarHeight(Context context) {
        int statusHeight = -1;

        try {
            Class<?> aClass = Class.forName("com.android.internal.R$dimen");
            Object object = aClass.newInstance();
            int height = Integer.parseInt(aClass.getField("status_bar_height").get(object).toString());
            statusHeight = context.getResources().getDimensionPixelSize(height);
        } catch (Exception var5) {
            var5.printStackTrace();
        }

        return statusHeight;
    }

    public static int dpToPx(Context context, int dp) {
        return Math.round((float)dp * getPixelScaleFactor(context));
    }

    public static int pxToDp(Context context, int px) {
        return Math.round((float)px / getPixelScaleFactor(context));
    }

    public static float getPixelScaleFactor(Context context) {
        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        return displayMetrics.xdpi / 160.0F;
    }

    public static boolean effectiveValue(int dpValue) {
        return 0 < dpValue;
    }

    public static boolean effectiveValue(float spValue) {
        return 5.0F < spValue;
    }

    public static int dp2px(float dpValue) {
        float scale = Resources.getSystem().getDisplayMetrics().density;
        return (int)(dpValue * scale + 0.5F);
    }

    public static int px2dp(float pxValue) {
        float scale = Resources.getSystem().getDisplayMetrics().density;
        return (int)(pxValue / scale + 0.5F);
    }

    public static int sp2px(float spValue) {
        float fontScale = Resources.getSystem().getDisplayMetrics().scaledDensity;
        return (int)(spValue * fontScale + 0.5F);
    }

    public static int px2sp(float pxValue) {
        float fontScale = Resources.getSystem().getDisplayMetrics().scaledDensity;
        return (int)(pxValue / fontScale + 0.5F);
    }

    public static int getDisplayWidth(Activity activity) {
        DisplayMetrics dm = new DisplayMetrics();
        activity.getWindowManager().getDefaultDisplay().getMetrics(dm);
        return dm.widthPixels;
    }

    /**
     * 获取 DisplayMetrics
     *
     * @return
     */
    public static DisplayMetrics getDisplayMetrics(Context context) {
        return context.getResources().getDisplayMetrics();
    }
}
