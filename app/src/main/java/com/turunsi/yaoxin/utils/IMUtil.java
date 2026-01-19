package com.turunsi.yaoxin.utils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.gson.Gson;
import com.netease.nimlib.sdk.auth.LoginInfo;
import com.netease.yunxin.kit.alog.ALog;
import com.netease.yunxin.kit.common.ui.utils.ToastX;
import com.netease.yunxin.kit.corekit.im.IMKitClient;
import com.netease.yunxin.kit.corekit.im.login.LoginCallback;
import com.turunsi.yaoxin.IMApplication;
import com.turunsi.yaoxin.R;
import com.turunsi.yaoxin.login.LoginActivity;
import com.turunsi.yaoxin.login.RealNameSetActivity;
import com.turunsi.yaoxin.main.MainActivity;
import com.turunsi.yaoxin.main.mine.setting.SettingNewActivity;
import com.yaoxin.appbase.model.NetData;
import com.yaoxin.appbase.model.RegisterBean;
import com.yaoxin.appbase.model.UserBean;
import com.yaoxin.appbase.net.CommonCallback;
import com.yaoxin.appbase.net.HttpUtil;
import com.yaoxin.appbase.net.NetServerException;
import com.yaoxin.appbase.utils.AppProxy;
import com.yaoxin.appbase.utils.DataUtil;
import com.yaoxin.appbase.utils.ToastUtils;

import retrofit2.Call;
import retrofit2.Response;

public class IMUtil {
    public static void loginIM(Activity context, String account, String token) {

        LoginInfo loginInfo =
                LoginInfo.LoginInfoBuilder.loginInfoDefault(account, token)
                        .withAppKey(DataUtils.readAppKey(context))
                        .build();
        IMKitClient.loginIM(
                loginInfo,
                new LoginCallback<LoginInfo>() {
                    @Override
                    public void onError(int errorCode, @NonNull String errorMsg) {
                        if (context.getClass() != LoginActivity.class) {
                            Intent intent = new Intent();
                            intent.setClass(context, LoginActivity.class);
//                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            context.startActivity(intent);
                            context.finish();
                        }
                    }

                    @Override
                    public void onSuccess(@Nullable LoginInfo data) {
                        showMainActivityAndFinish(context);
                    }
                });
    }

    public static void getToken() {
        RegisterBean bean = new RegisterBean();
//        bean.token = token;
        HttpUtil.apiW().home_getUserByToken(bean)
                .enqueue(new CommonCallback<NetData>() {
                    @Override
                    public void Successful(Call<NetData> call, Response<NetData> response, NetData body) {
                        UserBean userBean = new Gson().fromJson((String) body.data, UserBean.class);
                        DataUtil.putUserInfo(userBean);
                        DataUtil.putToken(userBean.token);

                    }

                    @Override
                    public void Failure(Call<NetData> call, Throwable t) {
                    }
                });
    }

    public static void showMainActivityAndFinish(Activity context) {
        Intent intent = new Intent();
        intent.setClass(context, MainActivity.class);
//        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        context.startActivity(intent);
        context.finish();
        IMUtil.getToken();
    }

    public static void loginOut(Activity activity) {
        IMKitClient.logoutIM(
                new com.netease.yunxin.kit.corekit.im.login.LoginCallback<Void>() {
                    @Override
                    public void onError(int errorCode, @NonNull String errorMsg) {
                        Toast.makeText(
                                        activity,
                                        "error code is " + errorCode + ", message is " + errorMsg,
                                        Toast.LENGTH_SHORT)
                                .show();
                    }

                    @Override
                    public void onSuccess(@Nullable Void data) {
                        if (activity.getApplicationContext() instanceof IMApplication) {
                            ((IMApplication) activity.getApplicationContext())
                                    .clearActivity(activity);
                        }
                        DataUtil.deleteLoginUserInfoList(DataUtil.getUserInfo());
                        DataUtil.deleteData();
                        activity.startActivity(new Intent(activity, LoginActivity.class));
                        activity.finish();
                    }
                });
    }
}
