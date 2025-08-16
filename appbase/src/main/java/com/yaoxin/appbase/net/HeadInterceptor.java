package com.yaoxin.appbase.net;


import android.util.Log;

import androidx.annotation.NonNull;

import com.yaoxin.appbase.utils.AppProxy;
import com.yaoxin.appbase.utils.DataUtil;
import com.yaoxin.appbase.utils.DeviceUtils;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

class HeadInterceptor implements Interceptor {

    @NonNull
    @Override
    public Response intercept(Chain chain) throws IOException {
        Request original = chain.request();
        //请求定制：添加请求头
        Request.Builder requestBuilder = original.newBuilder();
        Log.i("fanbo", DeviceUtils.getDeviceId(AppProxy.getInstance().getContext()));
        requestBuilder.addHeader("deviceId", DeviceUtils.getDeviceId(AppProxy.getInstance().getContext()));
        original = requestBuilder.build();
        return chain.proceed(original);
    }
}