// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.turunsi.yaoxin.main.mine.setting;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;

import com.google.gson.Gson;
import com.netease.nimlib.sdk.misc.DirCacheFileType;
import com.netease.yunxin.kit.common.ui.viewmodel.LoadStatus;
import com.netease.yunxin.kit.corekit.im.IMKitClient;
import com.netease.yunxin.kit.corekit.im.repo.MiscRepo;
import com.turunsi.yaoxin.IMApplication;
import com.turunsi.yaoxin.R;
import com.turunsi.yaoxin.databinding.ActivityMineSetNewBinding;
import com.turunsi.yaoxin.login.LoginActivity;
import com.turunsi.yaoxin.main.mine.AppUpdateActivity;
import com.turunsi.yaoxin.main.mine.DownLoadBean;
import com.turunsi.yaoxin.main.mine.account.AccountAnQuanManagerActivity;
import com.yaoxin.appbase.activity.BaseActivity;
import com.yaoxin.appbase.model.NetData;
import com.yaoxin.appbase.model.RegisterBean;
import com.yaoxin.appbase.net.CommonCallback;
import com.yaoxin.appbase.net.HttpUtil;
import com.yaoxin.appbase.utils.DataUtil;
import com.yaoxin.appbase.utils.DialogAlertUtil;
import com.yaoxin.appbase.utils.StatusBarUtils;
import com.yaoxin.appbase.utils.ToastUtils;
import com.turunsi.yaoxin.utils.DataUtils;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Response;

public class SettingNewActivity extends BaseActivity implements View.OnClickListener {

    private ActivityMineSetNewBinding viewBinding;
    private ClearCacheViewModel cacheViewModel;
    private String iosDownloadUrl = "";
    private String androidDownloadUrl = "";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewBinding = ActivityMineSetNewBinding.inflate(getLayoutInflater());
        setContentView(viewBinding.getRoot());
        cacheViewModel = new ViewModelProvider(this).get(ClearCacheViewModel.class);
        StatusBarUtils.transtStatusBar(this, viewBinding.activityMineSetNewNav);
        initView();
        loadCacheSize();
        loadWebsiteUrls();
    }

    private void initView() {
        viewBinding.activityMineSetNewNav.addCloseImageButton().setOnClickListener(this);
        viewBinding.activityMineSetNewChatView.setOnClickListener(this);
        viewBinding.activityMineSetNewPrivacyView.setOnClickListener(this);
        viewBinding.activityMineSetNewShareView.setOnClickListener(this);
        viewBinding.activityMineSetNewUpdateView.setOnClickListener(this);
        viewBinding.activityMineSetNewDeleteCacheView.setOnClickListener(this);
        viewBinding.activityMineSetNewDeleteRecordView.setOnClickListener(this);
        viewBinding.activityMineSetNewAboutUsView.setOnClickListener(this);
        viewBinding.getRoot().findViewById(R.id.view_setting_website_ios_copy_tv).setOnClickListener(this);
        viewBinding.getRoot().findViewById(R.id.view_setting_website_android_copy_tv).setOnClickListener(this);

        viewBinding.activityMineSetNewDeleteCacheView.rightTv.setVisibility(View.VISIBLE);
        viewBinding.activityMineSetNewDeleteCacheView.rightIv.setVisibility(View.GONE);
    }

    private void loadCacheSize() {
        cacheViewModel
                .getSdkCacheLiveData()
                .observe(this, result -> {
                    if (result.getLoadStatus() == LoadStatus.Success) {
                        long size = result.getData() != null ? result.getData() : 0;
                        viewBinding.activityMineSetNewDeleteCacheView.rightTv.setText(
                                String.format("%.2fM", DataUtils.getSizeToM(size)));
                    }
                });
        cacheViewModel.getSdkCacheSize();
    }

    private void loadWebsiteUrls() {
        HttpUtil.apiW().customer_about(new RegisterBean())
                .enqueue(new CommonCallback<NetData>() {
                    @Override
                    public void Successful(Call<NetData> call, Response<NetData> response, NetData body) {
                        DownLoadBean downLoadBean = new Gson().fromJson(body.data.toString(), DownLoadBean.class);
                        if (downLoadBean.linkUrl == null) {
                            return;
                        }
                        for (DownLoadBean tempBean : downLoadBean.linkUrl) {
                            if ("IOS".equals(tempBean.appType)) {
                                iosDownloadUrl = tempBean.downloadUrl;
                            }
                            if ("ANDROID".equals(tempBean.appType)) {
                                androidDownloadUrl = tempBean.downloadUrl;
                            }
                        }
                    }

                    @Override
                    public void Failure(Call<NetData> call, Throwable t) {
                    }
                });
    }

    @Override
    public void onClick(View v) {
        if (v == viewBinding.activityMineSetNewNav.addCloseImageButton()) {
            finish();
        } else if (v == viewBinding.activityMineSetNewChatView) {
            startActivity(new Intent(this, SettingNotifyNewActivity.class));
        } else if (v == viewBinding.activityMineSetNewPrivacyView) {
            AccountAnQuanManagerActivity.start(AccountAnQuanManagerActivity.class, this, null);
        } else if (v == viewBinding.activityMineSetNewShareView) {
            copyShareUrl(androidDownloadUrl);
        } else if (v == viewBinding.activityMineSetNewUpdateView) {
            AppUpdateActivity.start(AppUpdateActivity.class, this, null);
        } else if (v == viewBinding.activityMineSetNewDeleteCacheView) {
            startActivity(new Intent(this, ClearCacheActivity.class));
        } else if (v == viewBinding.activityMineSetNewDeleteRecordView) {
            DialogAlertUtil.showAlert("确定清空聊天记录吗？", type -> {
                if (type == 1) {
                    MiscRepo.INSTANCE.clearMessageCache();
                }
            }, getSupportFragmentManager());
        } else if (v == viewBinding.activityMineSetNewAboutUsView) {
            AboutUsNewActivity.start(AboutUsNewActivity.class, this, null);
        } else if (v.getId() == R.id.view_setting_website_ios_copy_tv) {
            copyShareUrl(iosDownloadUrl);
        } else if (v.getId() == R.id.view_setting_website_android_copy_tv) {
            copyShareUrl(androidDownloadUrl);
        }
    }

    private void copyShareUrl(String url) {
        if (url == null || url.isEmpty()) {
            ToastUtils.toastMsg("暂无下载地址");
            return;
        }
        ClipboardManager clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("label", url);
        clipboard.setPrimaryClip(clip);
        ToastUtils.toastMsg("已复制到剪切板");
    }

    void showLogin() {
        IMKitClient.logoutIM(
                new com.netease.yunxin.kit.corekit.im.login.LoginCallback<Void>() {
                    @Override
                    public void onError(int errorCode, @NonNull String errorMsg) {
                        Toast.makeText(
                                        SettingNewActivity.this,
                                        "error code is " + errorCode + ", message is " + errorMsg,
                                        Toast.LENGTH_SHORT)
                                .show();
                    }

                    @Override
                    public void onSuccess(@Nullable Void data) {
                        if (getApplicationContext() instanceof IMApplication) {
                            ((IMApplication) getApplicationContext())
                                    .clearActivity(SettingNewActivity.this);
                        }
                        DataUtil.deleteLoginUserInfoList(DataUtil.getUserInfo());
                        DataUtil.deleteData();
                        startActivity(new Intent(SettingNewActivity.this, LoginActivity.class));
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
