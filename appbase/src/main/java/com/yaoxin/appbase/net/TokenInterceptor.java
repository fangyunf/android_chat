package com.yaoxin.appbase.net;

import android.util.Log;

import androidx.annotation.NonNull;


import com.google.gson.Gson;
import com.yaoxin.appbase.utils.AESUtil;
import com.yaoxin.appbase.utils.DataUtil;

import java.io.IOException;
import java.util.HashMap;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

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

            // 修改请求体
            String newBody = modifyRequestBody(oldBody);

            // 创建新的请求体
            RequestBody newRequestBody = RequestBody.create(newBody, originalBody.contentType());

            Request.Builder builder = original.newBuilder();
            if (!DataUtil.getToken().isEmpty() && !DataUtil.getUserInfo().phoneNo.isEmpty()) {
                builder.addHeader("Authorization", DataUtil.getToken());
                builder.addHeader("phone", DataUtil.getUserInfo().phoneNo);

                Log.i("fanbo", DataUtil.getToken());
                Log.i("phone", DataUtil.getUserInfo().phoneNo);
            }
            // 创建新的请求
            Request newRequest = builder
                    .method(original.method(), newRequestBody)
                    .build();

            return chain.proceed(newRequest);
        }
        //请求定制：添加请求头
        Request.Builder requestBuilder = original.newBuilder();
        if (!DataUtil.getToken().isEmpty() && !DataUtil.getUserInfo().phoneNo.isEmpty()) {
            requestBuilder.addHeader("Authorization", DataUtil.getToken());
            requestBuilder.addHeader("phone", DataUtil.getUserInfo().phoneNo);
        }
        original = requestBuilder.build();
        return chain.proceed(original);
    }

    // 模拟修改请求体的方法
    private String modifyRequestBody(String oldBody) {
        // 这里可以对请求体进行任何操作，比如增加、修改参数等
        String s;
        try {
            s = AESUtil.aesEncrypt(oldBody);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        HashMap map = new HashMap();
        map.put("param", s);
        return new Gson().toJson(map);
    }

}
