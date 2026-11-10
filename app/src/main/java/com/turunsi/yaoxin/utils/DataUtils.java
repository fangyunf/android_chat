// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.turunsi.yaoxin.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.text.TextUtils;

public class DataUtils {

  private static String appKey = null;
  private static int serverConfig = -1;

  /** Prefer server-issued AppKey; fall back to manifest. */
  public static String readAppKey(Context context) {
    if (!TextUtils.isEmpty(appKey)) {
      return appKey;
    }
    String cached = readCachedYunxinAppKey(context);
    if (!TextUtils.isEmpty(cached)) {
      appKey = cached;
      return appKey;
    }
    return readManifestAppKey(context);
  }

  public static void setYunxinAppKey(Context context, String key) {
    if (TextUtils.isEmpty(key)) {
      return;
    }
    appKey = key.trim();
    getConfigShared(context)
        .edit()
        .putString(Constant.YUNXIN_APPKEY_CACHE, appKey)
        .apply();
  }

  public static String readCachedYunxinAppKey(Context context) {
    if (context == null) {
      return null;
    }
    return getConfigShared(context).getString(Constant.YUNXIN_APPKEY_CACHE, null);
  }

  /** read appKey from manifest */
  public static String readManifestAppKey(Context context) {
    if (context == null) {
      return appKey;
    }
    try {
      ApplicationInfo appInfo =
          context
              .getPackageManager()
              .getApplicationInfo(context.getPackageName(), PackageManager.GET_META_DATA);
      if (appInfo != null) {
        String keyStr =
            getServerConfigType(context) == Constant.CHINA_CONFIG
                ? Constant.CONFIG_APPKEY_KEY
                : Constant.CONFIG_APPKEY_KEY_OVERSEA;
        return appInfo.metaData.getString(keyStr);
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
    return null;
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
