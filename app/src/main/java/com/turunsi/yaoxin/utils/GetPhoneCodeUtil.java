package com.turunsi.yaoxin.utils;

import android.app.Activity;

import com.yaoxin.appbase.utils.CommonNetUtil;
import com.yaoxin.appbase.utils.ToastUtils;

public class GetPhoneCodeUtil {
    public static void getPhoneCode(Activity activity, String phone) {
        if (phone.length() != 11) {
            ToastUtils.toastMsg("手机格式错误");
            return;
        }
        if (activity == null) {
            ToastUtils.toastMsg("请重试");
            return;
        }
        CommonNetUtil.getPhoneCode(activity, phone);
    }

    // 保留旧签名，避免外部调用崩编译
    public static void getPhoneCode(String phone) {
        ToastUtils.toastMsg("请升级调用方式");
    }
}
