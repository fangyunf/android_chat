package com.turunsi.yaoxin.utils;

import com.netease.yunxin.kit.alog.ALog;
import com.yaoxin.appbase.model.NetData;
import com.yaoxin.appbase.model.RegisterBean;
import com.yaoxin.appbase.net.CommonCallback;
import com.yaoxin.appbase.net.HttpUtil;
import com.yaoxin.appbase.utils.ToastUtils;

import retrofit2.Call;
import retrofit2.Response;

public class GetPhoneCodeUtil {
    public static void getPhoneCode(String phone) {
        if (phone.length() != 11) {
            ToastUtils.toastMsg("手机格式错误");
            return;
        }
        RegisterBean registerBean = new RegisterBean();
        registerBean.phoneNo = phone;
        HttpUtil.apiW().customer_smsCode(registerBean)
                .enqueue(new CommonCallback<NetData>() {
                    @Override
                    public void Successful(Call<NetData> call, Response<NetData> response, NetData body) {
                        ToastUtils.toastMsg("发送成功");
                    }

                    @Override
                    public void Failure(Call<NetData> call, Throwable t) {

                    }

                    @Override
                    public void end() {
                        super.end();
                    }
                });
    }
}
