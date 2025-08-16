package com.yaoxin.appbase.net;

import android.text.TextUtils;

import androidx.annotation.NonNull;


import com.yaoxin.appbase.utils.DataUtil;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Response;

/**
 * <pre>
 *     author : cango
 *     e-mail : lili92823@163.com
 *     time   : 2018/11/15
 *     desc   :
 * </pre>
 */
class ReceivedJwtInterceptor implements Interceptor {

    @Override
    public Response intercept(@NonNull Chain chain) throws IOException {
        Response originalResponse = chain.proceed(chain.request());
        String jwt = originalResponse.header("Authorization");
        if (!TextUtils.isEmpty(jwt)) {
            DataUtil.putToken(" Bearer " + jwt);
        }
        return originalResponse;
    }
}
