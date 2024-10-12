package com.yaoxin.appbase.net;

import android.app.Activity;

import androidx.annotation.NonNull;


import com.google.gson.Gson;
import com.yaoxin.appbase.model.NetData;
import com.yaoxin.appbase.model.RegisterBean;
import com.yaoxin.appbase.model.UserBean;
import com.yaoxin.appbase.utils.AESUtil;
import com.yaoxin.appbase.utils.AppProxy;
import com.yaoxin.appbase.utils.DataUtil;
import com.yaoxin.appbase.utils.DeviceUtils;
import com.yaoxin.appbase.view.LoadingDialog;

import java.io.IOException;
import java.util.HashMap;
import java.util.concurrent.CountDownLatch;

import io.reactivex.Observable;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import retrofit2.Call;

/**
 * Author will·
 * Created at 2018/11/13.
 * okhttp拦截器,统一在请求头添加token
 */

public class TokenInterceptor implements Interceptor {
    @Override
    public Response intercept(@NonNull Chain chain) throws IOException {
        Request original = chain.request();
        RequestBody originalBody = original.body();
//        if (originalBody.contentType().subtype().equals("form-data")) {
//            return chain.proceed(original);
//        }
        if (originalBody != null && originalBody.contentType() != null && !originalBody.contentType().subtype().equals("form-data")) {
            // 将请求体转换为字符串
            okio.Buffer buffer = new okio.Buffer();
            originalBody.writeTo(buffer);
            String oldBody = buffer.readUtf8();
            // 创建新的请求体
            RequestBody newRequestBody = RequestBody.create(oldBody, originalBody.contentType());

            Request.Builder builder = original.newBuilder();
            if (!DataUtil.getToken().isEmpty()) {
                builder.addHeader("Authorization", "Bearer " + DataUtil.getToken());
            }
            // 创建新的请求
            Request newRequest = builder
                    .method(original.method(), newRequestBody)
                    .build();

            return chain.proceed(newRequest);
        }
        //请求定制：添加请求头
        Request.Builder requestBuilder = original.newBuilder();
        if (!DataUtil.getToken().isEmpty()) {
            requestBuilder.addHeader("Authorization", "Bearer " + DataUtil.getToken());
        }
        original = requestBuilder.build();
        return chain.proceed(original);
    }

}
