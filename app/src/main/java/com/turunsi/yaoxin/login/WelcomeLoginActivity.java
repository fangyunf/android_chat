package com.turunsi.yaoxin.login;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.Nullable;

import com.turunsi.yaoxin.databinding.ActivityWelcomeLoginBinding;
import com.turunsi.yaoxin.register.RegisterActivity;
import com.yaoxin.appbase.activity.BaseActivity;
import com.yaoxin.appbase.utils.StatusBarUtils;

public class WelcomeLoginActivity extends BaseActivity implements View.OnClickListener {

    private ActivityWelcomeLoginBinding binding;

    public static void start(Context context) {
        Intent intent = new Intent(context, WelcomeLoginActivity.class);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityWelcomeLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        // 设置状态栏透明
        StatusBarUtils.setStatusBarLightMode(this, true, true);
        initView();
    }

    private void initView() {
        // 设置点击事件
        binding.btnLogin.setOnClickListener(this);
        binding.btnRegister.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if (v == binding.btnLogin) {
            // 跳转到登录页面
            LoginActivity.start(LoginActivity.class, this, null);
        } else if (v == binding.btnRegister) {
            // 跳转到注册页面
            RegisterActivity.start(RegisterActivity.class, this, null);
        }
    }
}
