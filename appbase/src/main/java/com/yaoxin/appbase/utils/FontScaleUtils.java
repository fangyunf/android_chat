package com.yaoxin.appbase.utils;

import android.content.Context;
import android.content.res.Configuration;

/** 固定 App 内字体缩放为 1.0，不跟随系统「显示大小 / 字体大小」设置。 */
public final class FontScaleUtils {

    private FontScaleUtils() {}

    public static Context attach(Context context) {
        if (context == null) {
            return null;
        }
        Configuration configuration = context.getResources().getConfiguration();
        if (configuration.fontScale == 1.0f) {
            return context;
        }
        Configuration fixed = new Configuration(configuration);
        fixed.fontScale = 1.0f;
        return context.createConfigurationContext(fixed);
    }

    public static Configuration fixedConfiguration(Configuration source) {
        Configuration fixed = new Configuration(source);
        fixed.fontScale = 1.0f;
        return fixed;
    }
}
