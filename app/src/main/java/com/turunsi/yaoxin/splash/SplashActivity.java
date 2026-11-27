package com.turunsi.yaoxin.splash;

import android.app.Activity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.Nullable;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.turunsi.yaoxin.IMApplication;
import com.turunsi.yaoxin.databinding.ActivitySplashBinding;
import com.turunsi.yaoxin.login.WelcomeLoginActivity;
import com.turunsi.yaoxin.utils.IMUtil;
import com.yaoxin.appbase.activity.BaseActivity;
import com.yaoxin.appbase.model.NetData;
import com.yaoxin.appbase.net.CommonCallback;
import com.yaoxin.appbase.net.HttpUtil;
import com.yaoxin.appbase.utils.DataUtil;
import com.yaoxin.appbase.utils.StatusBarUtils;
import com.yaoxin.appbase.utils.ToastUtils;

import java.util.Collections;

import retrofit2.Call;
import retrofit2.Response;

public class SplashActivity extends BaseActivity {
    ActivitySplashBinding binding;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySplashBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        StatusBarUtils.setStatusBarLightMode(this, true, true);

        fetchYunxinAppKeyAndInit();
    }

    /**
     * 启动时序：先调 POST /customer/yunxinId 拿 AppKey → 初始化 NIM（含 securityMode）→ 再自动登录或进登录页。
     * 失败时提示「获取云信配置失败」，不初始化 NIM，不进登录/主页。
     */
    private void fetchYunxinAppKeyAndInit() {
        HttpUtil.apiW()
                .customer_yunxinId(Collections.emptyMap())
                .enqueue(new CommonCallback<NetData>() {
                    @Override
                    public void Successful(Call<NetData> call, Response<NetData> response, NetData body) {
                        if (isFinishing() || isDestroyed()) {
                            return;
                        }
                        String rawData = body == null || body.data == null ? null : body.data.toString();
                        String appKey = parseAppKey(rawData);
                        Log.d("YunxinId", "/customer/yunxinId decrypted data=" + rawData + ", appKey=" + appKey);
                        if (TextUtils.isEmpty(appKey)) {
                            onFetchAppKeyFailed();
                            return;
                        }
                        IMApplication app = IMApplication.getInstance();
                        if (app == null || !app.initSdk(appKey)) {
                            onFetchAppKeyFailed();
                            return;
                        }
                        proceedAfterSdkInit();
                    }

                    @Override
                    public void Failure(Call<NetData> call, Throwable t) {
                        Log.e("YunxinId", "/customer/yunxinId failed", t);
                        if (isFinishing() || isDestroyed()) {
                            return;
                        }
                        onFetchAppKeyFailed();
                    }
                });
    }

    private void onFetchAppKeyFailed() {
        ToastUtils.toastMsg("获取云信配置失败");
    }

    private void proceedAfterSdkInit() {
        Activity that = this;
        String account = DataUtil.getUserid();
        String token = DataUtil.getUserInfo().imToken;
        if (!TextUtils.isEmpty(account) && !TextUtils.isEmpty(token)) {
            IMUtil.loginIM(that, account, token);
        } else {
            WelcomeLoginActivity.start(WelcomeLoginActivity.class, SplashActivity.this, null);
            finish();
        }
    }

    /**
     * 兼容两种解密后出参形态：
     * 形态 A（优先）：{"key": "<云信AppKey>"}
     * 形态 B："<云信AppKey>"
     */
    private static String parseAppKey(String raw) {
        if (TextUtils.isEmpty(raw)) {
            return null;
        }
        String trimmed = raw.trim();
        if (trimmed.startsWith("{")) {
            try {
                JsonElement el = JsonParser.parseString(trimmed);
                if (el != null && el.isJsonObject()) {
                    JsonObject obj = el.getAsJsonObject();
                    if (obj.has("key") && !obj.get("key").isJsonNull()) {
                        return obj.get("key").getAsString().trim();
                    }
                }
            } catch (Exception ignore) {
            }
            return null;
        }
        return trimmed.replace("\"", "").trim();
    }
}
