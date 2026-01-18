// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.turunsi.yaoxin.main.mine.account;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.gson.Gson;
import com.netease.yunxin.kit.corekit.im.IMKitClient;
import com.turunsi.yaoxin.IMApplication;
import com.turunsi.yaoxin.databinding.ActivityAccountAnquanManagerBinding;
import com.turunsi.yaoxin.databinding.ActivityMinePwdSetMagerBinding;
import com.turunsi.yaoxin.login.LoginActivity;
import com.turunsi.yaoxin.login.WelcomeLoginActivity;
import com.turunsi.yaoxin.main.mine.purse.pwdmanager.PursePwdManagerSetActivity;
import com.turunsi.yaoxin.main.mine.setting.SettingNewActivity;
import com.turunsi.yaoxin.main.mine.setting.ZhuXiaoConfrimActivity;
import com.turunsi.yaoxin.register.ForgetPwdActivity;
import com.yaoxin.appbase.activity.BaseActivity;
import com.yaoxin.appbase.model.GroupInfoBean;
import com.yaoxin.appbase.model.NetData;
import com.yaoxin.appbase.model.RegisterBean;
import com.yaoxin.appbase.net.CommonCallback;
import com.yaoxin.appbase.net.HttpUtil;
import com.yaoxin.appbase.utils.DataUtil;
import com.yaoxin.appbase.utils.DialogAlertUtil;
import com.yaoxin.appbase.utils.StatusBarUtils;
import com.yaoxin.appbase.utils.ToastUtils;

import java.util.HashMap;

import retrofit2.Call;
import retrofit2.Response;

/**
 * team setting activity
 */
public class Mine_Pwd_Set_ManagerActivity extends BaseActivity implements View.OnClickListener {

    ActivityMinePwdSetMagerBinding binding;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding =
                ActivityMinePwdSetMagerBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.activityMinePwdSetMagerNav.addCloseImageButton().setOnClickListener(this);
        binding.activityMinePwdSetMagerSetLoginZxLl.setOnClickListener(this);
        binding.activityMinePwdSetMagerSetLoginPwdLl.setOnClickListener(this);
        binding.activityMinePwdSetMagerSetPayPwdLl.setOnClickListener(this);

        StatusBarUtils.transtStatusBar(this, binding.activityMinePwdSetMagerNav);
    }


    @Override
    public void onClick(View view) {
        if (view == binding.activityMinePwdSetMagerNav.addCloseImageButton()) {
            finish();
        } else if (view == binding.activityMinePwdSetMagerSetLoginZxLl) {
            ZhuXiaoConfrimActivity.start(ZhuXiaoConfrimActivity.class, this, null);
        } else if (view == binding.activityMinePwdSetMagerSetLoginPwdLl) {
            ForgetPwdActivity.start(ForgetPwdActivity.class, this, null);
        } else if (view == binding.activityMinePwdSetMagerSetPayPwdLl) {

            HashMap map = new HashMap();
            map.put("type", "0");
            PursePwdManagerSetActivity.start(PursePwdManagerSetActivity.class, this, map);
        }
    }

    void showLogin() {
        IMKitClient.logoutIM(
                new com.netease.yunxin.kit.corekit.im.login.LoginCallback<Void>() {
                    @Override
                    public void onError(int errorCode, @NonNull String errorMsg) {
                        Toast.makeText(
                                        Mine_Pwd_Set_ManagerActivity.this,
                                        "error code is " + errorCode + ", message is " + errorMsg,
                                        Toast.LENGTH_SHORT)
                                .show();
                    }

                    @Override
                    public void onSuccess(@Nullable Void data) {
                        if (getApplicationContext() instanceof IMApplication) {
                            ((IMApplication) getApplicationContext())
                                    .clearActivity(Mine_Pwd_Set_ManagerActivity.this);
                        }
                        DataUtil.deleteLoginUserInfoList(DataUtil.getUserInfo());
                        DataUtil.deleteData();
                        startActivity(new Intent(Mine_Pwd_Set_ManagerActivity.this, WelcomeLoginActivity.class));
                        finish();
                    }
                });
    }


}
