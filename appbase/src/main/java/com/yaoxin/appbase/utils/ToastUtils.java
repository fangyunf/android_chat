package com.yaoxin.appbase.utils;

import android.content.Context;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.yaoxin.appbase.R;

/**
 * Created by yintangwen952 on 2018/9/2.
 */
public class ToastUtils {

    private static Context sCtx;
    private static Toast sToast;

    public static void toastMsg(String msg) {
        if (TextUtils.isEmpty(msg)) {
            return;
        }
        cancel();
        checkContext();
        showCustomToast(msg, Toast.LENGTH_SHORT);
    }

    public static void toastLongMsg(String msg) {
        if (TextUtils.isEmpty(msg)) {
            return;
        }
        cancel();
        checkContext();
        showCustomToast(msg, Toast.LENGTH_LONG);
    }

    public static void toastMsg(int msgId) {
        checkContext();
        toastMsg(sCtx.getResources().getString(msgId));
    }

    private static void checkContext() {
        if (sCtx == null) {
            sCtx = AppProxy.getInstance().getApplication();
        }
    }

    /** 居中、圆角黑底、白字 */
    private static void showCustomToast(String msg, int duration) {
        View root = LayoutInflater.from(sCtx).inflate(R.layout.layout_toast, null, false);
        TextView tv = root.findViewById(R.id.appbase_toast_message_tv);
        tv.setText(msg);
        sToast = new Toast(sCtx);
        sToast.setDuration(duration);
        sToast.setGravity(Gravity.CENTER, 0, 0);
        sToast.setView(root);
        sToast.show();
    }

    /**
     * 自定义toast样式
     */
    public static void toastMsgWithStyle(View toastVieww) {
        if (sToast != null) {
            sToast.cancel();
        }
        checkContext();
        sToast = new Toast(sCtx);
        sToast.setDuration(Toast.LENGTH_SHORT);
        sToast.setGravity(Gravity.CENTER, 0, 0);
        sToast.setView(toastVieww);
        sToast.show();
    }

    /**
     * 取消toast
     */
    public static void cancel() {
        if (sToast != null) {
            sToast.cancel();
            sToast = null;
        }
    }
}
