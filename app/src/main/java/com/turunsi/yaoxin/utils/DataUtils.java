// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.turunsi.yaoxin.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;

public class DataUtils {

  private static volatile String appKey = null;
  private static int serverConfig = -1;

  /** 仅返回服务端 /customer/yunxinId 下发的 AppKey，不读本地 Manifest */
  public static String readAppKey(Context context) {
    return appKey;
  }

  public static void setYunxinAppKey(String key) {
    if (!TextUtils.isEmpty(key)) {
      appKey = key.trim();
    }
  }

  public static int getServerConfigType(Context context) {
    if (serverConfig < 0) {
      serverConfig = getConfigShared(context).getInt(Constant.SERVER_CONFIG, Constant.CHINA_CONFIG);
    }
    return serverConfig;
  }

  public static SharedPreferences getConfigShared(Context context) {
    SharedPreferences sharedPreferences =
        context.getSharedPreferences(Constant.SERVER_CONFIG_FILE, Context.MODE_MULTI_PROCESS);
    return sharedPreferences;
  }

  public static float getSizeToM(long size) {
    return size / (1024.0f * 1024.0f);
  }
}
