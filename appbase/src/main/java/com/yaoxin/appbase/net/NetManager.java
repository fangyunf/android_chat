package com.yaoxin.appbase.net;





import com.google.gson.GsonBuilder;
import com.google.gson.ToNumberPolicy;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by will
 * on 2018/5/25.
 */
public class NetManager {
    private NetManager() {
    }

    private static class NetManagerHolder {
        private static OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .addNetworkInterceptor(new HttpLoggingInterceptor()
                        .setLevel(Constant.DEBUG ? HttpLoggingInterceptor.Level.BODY : HttpLoggingInterceptor.Level.NONE))
                .addInterceptor(new TokenInterceptor())
                .connectTimeout(30, TimeUnit.SECONDS) //超时时间
                .readTimeout(30, TimeUnit.SECONDS)
                .build();

        private static Retrofit INSTANCE = new Retrofit.Builder()
                .client(okHttpClient)
                .baseUrl(Constant.BASE_URL)
                .addConverterFactory(GsonConverterFactory.create(
                        new GsonBuilder()
                                .setObjectToNumberStrategy(ToNumberPolicy.LONG_OR_DOUBLE)
                                .setNumberToNumberStrategy(ToNumberPolicy.LONG_OR_DOUBLE)
                                .create()
                ))
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .build();
        private static Retrofit INSTANCE1 = new Retrofit.Builder()
                .client(okHttpClient)
                .baseUrl(Constant.BASE_URL_8444)
                .addConverterFactory(GsonConverterFactory.create(
                        new GsonBuilder()
                                .setObjectToNumberStrategy(ToNumberPolicy.LONG_OR_DOUBLE)
                                .setNumberToNumberStrategy(ToNumberPolicy.LONG_OR_DOUBLE)
                                .create()
                ))
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .build();
        /**
         * 仅在需要拦截jwt时使用的网络配置（如登录）
         */
        private static OkHttpClient tokenClient = new OkHttpClient.Builder()
                .addNetworkInterceptor(new HttpLoggingInterceptor()
                        .setLevel(Constant.DEBUG ? HttpLoggingInterceptor.Level.BODY : HttpLoggingInterceptor.Level.NONE))
                .addInterceptor(new TokenInterceptor())
                .addInterceptor(new ReceivedJwtInterceptor())
                .connectTimeout(30, TimeUnit.SECONDS) //超时时间
                .readTimeout(30, TimeUnit.SECONDS)
                .build();

        private static Retrofit TOKEN_INSTANCE = new Retrofit.Builder()
                .client(tokenClient)
                .baseUrl(Constant.BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .build();
    }

    public static Retrofit getInstance() {
        return NetManagerHolder.INSTANCE;
    }
    public static Retrofit getInstance1() {
        return NetManagerHolder.INSTANCE1;
    }

    public static Retrofit getTokenInstance() {
        return NetManagerHolder.TOKEN_INSTANCE;
    }

}
