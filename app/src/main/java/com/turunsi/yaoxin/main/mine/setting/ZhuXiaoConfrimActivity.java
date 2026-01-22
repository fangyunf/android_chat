// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.turunsi.yaoxin.main.mine.setting;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.netease.nimlib.sdk.misc.DirCacheFileType;
import com.netease.yunxin.kit.corekit.im.IMKitClient;
import com.netease.yunxin.kit.corekit.im.provider.FetchCallback;
import com.netease.yunxin.kit.corekit.im.repo.MiscRepo;
import com.turunsi.yaoxin.IMApplication;
import com.turunsi.yaoxin.databinding.ActivityMineSetNewBinding;
import com.turunsi.yaoxin.databinding.ActivityMineZhuxiaoConfirmBinding;
import com.turunsi.yaoxin.login.LoginActivity;
import com.turunsi.yaoxin.main.mine.DownLoadActivity;
import com.yaoxin.appbase.activity.BaseActivity;
import com.yaoxin.appbase.model.NetData;
import com.yaoxin.appbase.net.CommonCallback;
import com.yaoxin.appbase.net.HttpUtil;
import com.yaoxin.appbase.utils.DataUtil;
import com.yaoxin.appbase.utils.DialogAlertUtil;
import com.yaoxin.appbase.utils.StatusBarUtils;
import com.yaoxin.appbase.utils.ToastUtils;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Response;

public class ZhuXiaoConfrimActivity extends BaseActivity implements View.OnClickListener {

    private ActivityMineZhuxiaoConfirmBinding viewBinding;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewBinding = ActivityMineZhuxiaoConfirmBinding.inflate(getLayoutInflater());
        setContentView(viewBinding.getRoot());
        StatusBarUtils.transtStatusBar(this, viewBinding.activityMineZhuxiaoConfirmNav);
        initView();
    }

    private void initView() {
        viewBinding.activityMineZhuxiaoConfirmNav.addCloseImageButton().setOnClickListener(this);
        viewBinding.activityMineZhuxiaoConfirmTv.setOnClickListener(this);

        // 设置标题：申请注销 + 掩码手机号
        String phone = DataUtil.getUserInfo().phone;
        if (TextUtils.isEmpty(phone)) {
            phone = DataUtil.getUserInfo().phoneNo;
        }
        if (TextUtils.isEmpty(phone)) {
            phone = DataUtil.getUserInfo().phoneFix;
        }

        String maskedPhone = maskPhone(phone);
        viewBinding.activityMineZhuxiaoConfirmTitleTv.setText("申请注销" + maskedPhone + "账号");
    }

    /**
     * 掩码手机号，格式：185****8999
     */
    private String maskPhone(String phone) {
        if (TextUtils.isEmpty(phone) || phone.length() < 7) {
            return "****";
        }
        // 保留前3位和后4位，中间用****替代
        String prefix = phone.substring(0, 3);
        String suffix = phone.substring(phone.length() - 4);
        return prefix + "****" + suffix;
    }

    @Override
    public void onClick(View v) {
        if (v == viewBinding.activityMineZhuxiaoConfirmNav.addCloseImageButton()) {
            finish();

        } else if (v == viewBinding.activityMineZhuxiaoConfirmTv) {
            DialogAlertUtil.showAlert("确定注销账号吗？", new DialogAlertUtil.DialogAlertUtilCallBack() {
                @Override
                public void clickType(int type) {
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
                }
            }, getSupportFragmentManager());
        }
    }

    void showLogin() {
        IMKitClient.logoutIM(
                new com.netease.yunxin.kit.corekit.im.login.LoginCallback<Void>() {
                    @Override
                    public void onError(int errorCode, @NonNull String errorMsg) {
                        Toast.makeText(
                                        ZhuXiaoConfrimActivity.this,
                                        "error code is " + errorCode + ", message is " + errorMsg,
                                        Toast.LENGTH_SHORT)
                                .show();
                    }

                    @Override
                    public void onSuccess(@Nullable Void data) {
                        if (getApplicationContext() instanceof IMApplication) {
                            ((IMApplication) getApplicationContext())
                                    .clearActivity(ZhuXiaoConfrimActivity.this);
                        }
                        DataUtil.deleteLoginUserInfoList(DataUtil.getUserInfo());
                        DataUtil.deleteData();
                        startActivity(new Intent(ZhuXiaoConfrimActivity.this, LoginActivity.class));
                        finish();
                    }
                });
    }

    private List<DirCacheFileType> getSDKFileType() {
        List<DirCacheFileType> types = new ArrayList<>();
        types.add(DirCacheFileType.AUDIO);
        types.add(DirCacheFileType.THUMB);
        types.add(DirCacheFileType.IMAGE);
        types.add(DirCacheFileType.VIDEO);
        types.add(DirCacheFileType.OTHER);
        return types;
    }
}
