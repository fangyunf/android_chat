package com.turunsi.yaoxin.utils;

import android.content.Context;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.yaoxin.appbase.model.NetData;
import com.yaoxin.appbase.model.ParamsBean;
import com.yaoxin.appbase.model.RegisterBean;
import com.yaoxin.appbase.net.HttpUtil;
import com.yaoxin.appbase.utils.AESUtil;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import retrofit2.Response;

/**
 * Fetch Yunxin AppKey from /customer/yunxinId before NIM SDK init.
 */
public final class YunxinAppKeyHelper {

    private static final String TAG = "YunxinAppKeyHelper";
    private static final long FETCH_TIMEOUT_SECONDS = 8;

    private YunxinAppKeyHelper() {
    }

    /**
     * Fetch AppKey from server before NIM init. Network always runs off the main thread.
     * On failure, keeps cached/manifest key.
     */
    public static String fetchAndCacheAppKey(Context context) {
        String fallback = DataUtils.readAppKey(context);
        final Context appContext = context.getApplicationContext();
        final AtomicReference<String> result = new AtomicReference<>(fallback);

        if (Looper.myLooper() != Looper.getMainLooper()) {
            result.set(requestAppKey(appContext, fallback));
            return result.get();
        }

        CountDownLatch latch = new CountDownLatch(1);
        Thread worker =
                new Thread(
                        () -> {
                            try {
                                result.set(requestAppKey(appContext, fallback));
                            } finally {
                                latch.countDown();
                            }
                        },
                        "yunxin-appkey-fetch");
        worker.start();
        try {
            if (!latch.await(FETCH_TIMEOUT_SECONDS, TimeUnit.SECONDS)) {
                Log.w(TAG, "yunxinId fetch timeout, use fallback");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            Log.w(TAG, "yunxinId fetch interrupted, use fallback");
        }
        return result.get();
    }

    private static String requestAppKey(Context context, String fallback) {
        try {
            Response<NetData> response =
                    HttpUtil.apiW().customer_yunxinId(new RegisterBean()).execute();
            if (response == null || !response.isSuccessful() || response.body() == null) {
                Log.w(TAG, "yunxinId http failed, use fallback");
                return fallback;
            }
            NetData body = response.body();
            if (body.code != 200 || body.data == null) {
                Log.w(TAG, "yunxinId business code=" + body.code + ", use fallback");
                return fallback;
            }
            String decrypted = body.data.toString();
            try {
                decrypted = AESUtil.aseDecrypt(decrypted);
            } catch (Exception ignore) {
                // data may already be plain json
            }
            String key = parseKey(decrypted);
            if (TextUtils.isEmpty(key)) {
                Log.w(TAG, "yunxinId missing key field, use fallback");
                return fallback;
            }
            DataUtils.setYunxinAppKey(context, key);
            Log.d(TAG, "yunxinId appKey fetched" + key);
            return key;
        } catch (Exception e) {
            Log.e(TAG, "yunxinId fetch error, use fallback", e);
            return fallback;
        }
    }

    private static String parseKey(String raw) {
        if (TextUtils.isEmpty(raw)) {
            return null;
        }
        String trimmed = raw.trim();
        // plain key string
        if (!trimmed.startsWith("{") && !trimmed.startsWith("[")) {
            return trimmed.replace("\"", "");
        }
        try {
            ParamsBean bean = new Gson().fromJson(trimmed, ParamsBean.class);
            if (bean != null && !TextUtils.isEmpty(bean.key)) {
                return bean.key;
            }
        } catch (Exception ignore) {
        }
        try {
            JsonElement element = JsonParser.parseString(trimmed);
            if (element != null && element.isJsonObject()) {
                JsonObject obj = element.getAsJsonObject();
                if (obj.has("key") && !obj.get("key").isJsonNull()) {
                    return obj.get("key").getAsString();
                }
            }
        } catch (Exception ignore) {
        }
        return null;
    }
}
