// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.turunsi.yaoxin.main.mine.setting;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.netease.nimlib.sdk.misc.DirCacheFileType;
import com.netease.yunxin.kit.corekit.im.IMKitClient;
import com.netease.yunxin.kit.corekit.im.repo.MiscRepo;
import com.turunsi.yaoxin.IMApplication;
import com.turunsi.yaoxin.databinding.ActivityMineSetNewBinding;
import com.turunsi.yaoxin.databinding.ActivityMineZhglNewBinding;
import com.turunsi.yaoxin.login.LoginActivity;
import com.turunsi.yaoxin.main.mine.DownLoadActivity;
import com.yaoxin.appbase.activity.BaseActivity;
import com.yaoxin.appbase.model.NetData;
import com.yaoxin.appbase.net.CommonCallback;
import com.yaoxin.appbase.net.HttpUtil;
import com.yaoxin.appbase.utils.DataUtil;
import com.yaoxin.appbase.utils.DialogAlertUtil;
import com.yaoxin.appbase.utils.ToastUtils;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Response;

public class ZHGLNewActivity extends BaseActivity implements View.OnClickListener {

    private ActivityMineZhglNewBinding viewBinding;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//    changeStatusBarColor(R.color.color_e9eff5);
        viewBinding = ActivityMineZhglNewBinding.inflate(getLayoutInflater());
        setContentView(viewBinding.getRoot());
        initView();
        transtStatusBar(viewBinding.activityMineSetNewNav);
    }

    private void initView() {

        viewBinding.activityMineSetNewNav.addCloseImageButton().setOnClickListener(this);
        viewBinding.activityMineZhglNewQhzh.setOnClickListener(this);
        viewBinding.activityMineZhglNewZx.setOnClickListener(this);

    }

    @Override
    public void onClick(View v) {
        if (v == viewBinding.activityMineZhglNewZx) {
            DialogAlertUtil.showAlert("确定注销账号吗？", type -> {
                if (type == 1) {
                    HttpUtil.apiW().home_logout()
                            .enqueue(new CommonCallback<NetData>() {
                                @Override
                                public void Successful(Call<NetData> call, Response<NetData> response, NetData body) {
                                    ToastUtils.toastMsg("注销成功");
                                    showLogin();
                                }

                                @Override
                                public void Failure(Call<NetData> call, Throwable t) {

                                }
                            });
                }
            }, getSupportFragmentManager());
        } else if (v == viewBinding.activityMineZhglNewQhzh) {
            ExchangeAccountActivity.start(ExchangeAccountActivity.class, this, null);
        } else if (v == viewBinding.activityMineSetNewNav.addCloseImageButton()) {
            finish();
        }
    }

    void showLogin() {
        IMKitClient.logoutIM(
                new com.netease.yunxin.kit.corekit.im.login.LoginCallback<Void>() {
                    @Override
                    public void onError(int errorCode, @NonNull String errorMsg) {
                        Toast.makeText(
                                        ZHGLNewActivity.this,
                                        "error code is " + errorCode + ", message is " + errorMsg,
                                        Toast.LENGTH_SHORT)
                                .show();
                    }

                    @Override
                    public void onSuccess(@Nullable Void data) {
                        if (getApplicationContext() instanceof IMApplication) {
                            ((IMApplication) getApplicationContext())
                                    .clearActivity(ZHGLNewActivity.this);
                        }
                        DataUtil.deleteLoginUserInfoList(DataUtil.getUserInfo());
                        DataUtil.deleteData();
                        startActivity(new Intent(ZHGLNewActivity.this, LoginActivity.class));
                        finish();
                    }
                });
    }

}
