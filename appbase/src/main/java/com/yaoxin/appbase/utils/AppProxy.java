package com.yaoxin.appbase.utils;

import android.app.Application;
import android.content.Context;
import android.text.TextUtils;
import android.util.Log;


import java.util.Map;

/*
 * Copyright (C) 2018 pasc Licensed under the Apache License, Version 2.0 (the "License");
 * @author chenshangyong872
 * @date 2018-06-25
 * @des 应用程序在底层LibBase处的代理，方便上层调用应用程序的基础数据：1、application 2、isDbug 3、Context
 *      另外，在这里会对LibBase的基础库功能进行初始化
 * @version V1.0
 * @modify On 2018-08-28 by author for reason ...
 */
public class AppProxy {
    private static final String TAG = AppProxy.class.getSimpleName();

    // 注意这里的值,和主工程的渠道配置是对应的
    public static final int TYPE_PRODUCT_PRODUCT = 1;
    public static final int TYPE_PRODUCT_BETA = 2;

    private String versionName;
    private static Application mApplication;

    private static boolean sIsDebug = false;

    // 默认是beta（测试环境）
    private static int sProductType = TYPE_PRODUCT_BETA;

    private static String sHost = null;

    private static String h5Host = null;
    private static String teamId = null;
    public static String searchKeyWord0 = "";
    public static String searchKeyWord1 = "";
    public int showType = 0;

    public static AppProxy getInstance() {
        return SingletonHolder.instance;
    }

    /**
     * 静态内部类,只有在装载该内部类时才会去创建单例对象
     */
    private static class SingletonHolder {
        private static final AppProxy instance = new AppProxy();
    }

    public AppProxy init(Application application) {
        if (null == application) {
            throw new IllegalArgumentException("Illega application Exception, please check~ !");
        }

        AppProxy.mApplication = application;


        return this;
    }

//    public AppProxy setUserManager(IUserManager userManager) {
//        if (userManager == null) {
//            throw new NullPointerException("IUserManager为空");
//        }
//        this.iUserManager = userManager;
//        return this;
//    }
//
//    public IUserManager getUserManager() {
//        if (iUserManager == null) {
//            throw new NullPointerException("IUserManager为空");
//        }
//        return iUserManager;
//    }


    public AppProxy setIsDebug(boolean isDebug) {
        sIsDebug = isDebug;
        return this;
    }

    public AppProxy setProductType(int productType) {
        sProductType = productType;

        return this;
    }

    public AppProxy setHost(String host) {
        sHost = host;

        return this;
    }

    public AppProxy setH5Host(String host) {
        h5Host = host;
        return this;
    }

    public AppProxy setTeamId(String teamId) {
        AppProxy.teamId = teamId;
        return this;
    }

    public Application getApplication() {
        if (null == mApplication) {
            throw new IllegalAccessError("Please initialize the AppProxy first.");
        }
        return mApplication;
    }

    public boolean isDebug() {
        return sIsDebug;
    }

    public Context getContext() {
        if (null == mApplication) {
            throw new IllegalAccessError("Please initialize the AppProxy first.");
        }

        return mApplication.getApplicationContext();
    }

    public int getProductType() {
        return sProductType;
    }


    public void setVersionName(String versionName) {
        this.versionName = versionName;
    }

    public String getVersionName() {
        return versionName;
    }

    public final String getHost() {
        if (TextUtils.isEmpty(sHost)) {
            throw new IllegalAccessError("Please call setHost to initialize the Host first.");
        }

        return sHost;
    }

    public final String getYCHost() {
        return "https://biz.myyancheng.com.cn";
    }

    public final String getH5Host() {
        if (TextUtils.isEmpty(h5Host)) {
            throw new IllegalAccessError("Please call setH5Host to initialize the h5Host first.");
        }
        return h5Host;
    }

    public final String getTeamId() {
        if (TextUtils.isEmpty(teamId)) {
            throw new IllegalAccessError("Please call teamId to initialize the teamId first.");
        }
        return teamId;
    }

    // 正式环境
    public boolean isProductionEvn() { //
        return TYPE_PRODUCT_PRODUCT == sProductType;
    }

    // 测试环境
    public boolean isBetaEvn() {
        return TYPE_PRODUCT_BETA == sProductType;
    }


}
