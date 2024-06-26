package com.turunsi.yaoxin.splash;

import android.app.Activity;
import android.os.Bundle;
import android.text.TextUtils;

import androidx.annotation.Nullable;

import com.turunsi.yaoxin.utils.IMUtil;
import com.yaoxin.appbase.activity.BaseActivity;
import com.turunsi.yaoxin.databinding.ActivitySplashBinding;
import com.turunsi.yaoxin.login.LoginActivity;
import com.yaoxin.appbase.utils.DataUtil;
import com.yaoxin.appbase.utils.StatusBarUtils;

import java.util.Timer;
import java.util.TimerTask;

public class SplashActivity extends BaseActivity {
    ActivitySplashBinding binding;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySplashBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        StatusBarUtils.setStatusBarLightMode(this, true, true);


//        LoginActivity.start(SplashActivity.this);
//        LoginActivity.start(LoginActivity.class,this,null);
//        finish();
        Activity that = this;
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                // 这里写你想延时执行的代码
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        String account = DataUtil.getUserid();
                        String token = DataUtil.getUserInfo().imToken;

                        if (account != null && token != null) {
                            IMUtil.loginIM(that,account,token);
                        } else {
                            LoginActivity.start(LoginActivity.class, SplashActivity.this,null);
                        }
                    }
                });
            }
        }, 500);

    }
}
