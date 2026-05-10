package com.turunsi.yaoxin.utils;

import androidx.fragment.app.FragmentActivity;

import com.yaoxin.appbase.utils.CommonNetUtil;
import com.yaoxin.appbase.view.loginlib.view.CountDownView;

/**
 * @deprecated 请使用 {@link CommonNetUtil#getPhoneCode(FragmentActivity, String, CountDownView)}
 */
@Deprecated
public class GetPhoneCodeUtil {

    public static void getPhoneCode(FragmentActivity activity, String phone, CountDownView countDownView) {
        CommonNetUtil.getPhoneCode(activity, phone, countDownView);
    }
}
