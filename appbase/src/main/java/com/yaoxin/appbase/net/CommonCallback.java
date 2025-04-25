package com.yaoxin.appbase.net;


import androidx.annotation.NonNull;


import com.netease.yunxin.kit.corekit.route.XKitRouter;
import com.yaoxin.appbase.BuildConfig;
import com.yaoxin.appbase.model.NetData;
import com.yaoxin.appbase.utils.AESUtil;
import com.yaoxin.appbase.utils.AppProxy;
import com.yaoxin.appbase.utils.BaseEvent;
import com.yaoxin.appbase.utils.DataUtil;
import com.yaoxin.appbase.utils.ToastUtils;

import org.greenrobot.eventbus.EventBus;

import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;

import okhttp3.HttpUrl;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Author will
 * Created at 2016/10/12.
 * retrofit 统一回调
 * 处理应与NetObserver保持一致
 */
public abstract class CommonCallback<T> implements Callback<T> {

    @Override
    public void onResponse(@NonNull Call<T> call, @NonNull Response<T> response) {
        if (response.isSuccessful()) {
            T t = response.body();
            if (t instanceof NetData) {
                NetData netData = (NetData) t;
                switch (netData.code) {
                    case 200:
                        try {
                            if (netData.data != null) {
                                netData.data = AESUtil.aseDecrypt(netData.data.toString());
                            }
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }
                        Successful(call, response, t);
                        break;
                    case 900:// 版本更新
                        String msg = ((NetData) t).msg;
//                        if (msg != null) {
//                            UpdateVersionBean versionBean = new Gson().fromJson(msg, UpdateVersionBean.class);
//                            UpdateDialog.showDialog(((AppCompatActivity) MyActivityManager.getInstance().getCurrentActivity()).getSupportFragmentManager(), versionBean);
//                        }
                        break;
                    case 406:
                        if (!Constant.isRunningLoginView) {
                            Constant.isRunningLoginView = true;
                            EventBus.getDefault().post(new BaseEvent("login_out"));
                        }
                        break;
                    case 777:// 版本更新
//                        if (BuildConfig.DEBUG) {
//
//                        } else {

                        if (!Constant.isRunningRealName) {
                            Constant.isRunningRealName = true;
                            XKitRouter.withKey(Constant.RealName_Router)
                                    .withContext(AppProxy.getInstance().getContext())
                                    .navigate();
//                        }


                        }
                        break;
                    case -101://认证过期
                        ToastUtils.toastMsg(((NetData) t).msg);
                        DataUtil.putUserInfo(null);
                        DataUtil.putToken(null);
//                        LoginActivity.cleanStart(App.getContext());
                        break;
                    default:
                        boolean isSkip = false;
                        try {
                            String url = response.raw().request().url().url().toString();
                            if (url.contains("caidan/groupCaidan")) {
                                isSkip = true;
                            }
                        } catch (Exception e) {

                        }
                        if (!isSkip) {
                            onFailure(call, new NetServerException(netData.msg != null ? netData.msg : "", netData.code));
                        }
                        return;
                }

            } else {
                onFailure(call, new RuntimeException("response error,detail = " + response.raw().toString()));
                return;
            }
        } else {
            onFailure(call, new RuntimeException("response error,detail = " + response.raw().toString()));
            return;
        }
        end();
    }

    @Override
    public void onFailure(@NonNull Call<T> call, @NonNull Throwable t) {
        if (t instanceof UnknownHostException) {
            ToastUtils.toastMsg("没有网络");
        } else if (t instanceof ConnectException) {
            ToastUtils.toastMsg("网络连接错误");
        } else if (t instanceof SocketTimeoutException) {
            ToastUtils.toastMsg("请求超时");
        } else if (t instanceof NetServerException) {
            ToastUtils.toastMsg(t.getMessage());
        } else {
            ToastUtils.toastMsg("网络错误");
//            Logger.e(t.toString());
        }
        Failure(call, t);
        end();
    }

    public abstract void Successful(Call<T> call, Response<T> response, T body);

    public abstract void Failure(Call<T> call, Throwable t);

    public void end() {
    }
}
