// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.turunsi.yaoxin.main.mine.setting;

import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.netease.yunxin.kit.common.ui.dialog.ChoiceListener;
import com.netease.yunxin.kit.common.ui.dialog.CommonChoiceDialog;
import com.netease.yunxin.kit.corekit.im.IMKitClient;
import com.netease.yunxin.kit.corekit.route.XKitRouter;
import com.turunsi.yaoxin.IMApplication;
import com.turunsi.yaoxin.databinding.ActivityMineAboutUsNewBinding;
import com.turunsi.yaoxin.databinding.ActivityMineSetNewBinding;
import com.turunsi.yaoxin.login.LoginActivity;
import com.turunsi.yaoxin.register.ForgetPwdActivity;
import com.yaoxin.appbase.activity.BaseActivity;
import com.yaoxin.appbase.model.NetData;
import com.yaoxin.appbase.net.CommonCallback;
import com.yaoxin.appbase.net.Constant;
import com.yaoxin.appbase.net.HttpUtil;
import com.yaoxin.appbase.utils.DataUtil;
import com.yaoxin.appbase.utils.StatusBarUtils;
import com.yaoxin.appbase.utils.ToastUtils;

import retrofit2.Call;
import retrofit2.Response;

public class AboutUsNewActivity extends BaseActivity implements View.OnClickListener {

    private ActivityMineAboutUsNewBinding viewBinding;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//    changeStatusBarColor(R.color.color_e9eff5);
        viewBinding = ActivityMineAboutUsNewBinding.inflate(getLayoutInflater());
        setContentView(viewBinding.getRoot());
        StatusBarUtils.transtStatusBar(this, viewBinding.activityMineAboutUsNewNav);
        initView();
    }

    private void initView() {
        viewBinding.activityMineAboutUsNewNav.addCloseImageButton().setOnClickListener(this);

        // 设置版本号
        String versionName = getAppVersionName();
        viewBinding.tvVersion.setText("版本号" + versionName);
        // 设置点击事件
        viewBinding.tvShareApp.setOnClickListener(this);
        viewBinding.tvServiceAgreement.setOnClickListener(this);
        viewBinding.tvPrivacyPolicy.setOnClickListener(this);
        viewBinding.tvVersionUpdate.setOnClickListener(this);
    }

    private String getAppVersionName() {
        String versionName = "";
        try {
            PackageInfo packageInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
            versionName = packageInfo.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return versionName;
    }

    @Override
    public void onClick(View v) {
        if (v == viewBinding.activityMineAboutUsNewNav.addCloseImageButton()) {
            finish();
        } else if (v == viewBinding.tvShareApp) {
            // TODO: 实现分享APP功能
            ToastUtils.toastMsg("分享APP功能待实现");
        } else if (v == viewBinding.tvServiceAgreement) {
            // 服务协议
            XKitRouter.withKey(Constant.BaseWebViewActivityKey).withParam("type", "2").withParam("title", "服务协议").withContext(this).navigate();
        } else if (v == viewBinding.tvPrivacyPolicy) {
            // 隐私政策
            XKitRouter.withKey(Constant.BaseWebViewActivityKey).withParam("type", "1").withParam("title", "隐私政策").withContext(this).navigate();
        } else if (v == viewBinding.tvVersionUpdate) {
            // TODO: 实现版本更新功能
            ToastUtils.toastMsg("版本更新功能待实现");
        }
    }

}
