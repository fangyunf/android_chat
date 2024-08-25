// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.turunsi.yaoxin.main.mine;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.Nullable;

import com.google.gson.Gson;
import com.turunsi.yaoxin.BuildConfig;
import com.turunsi.yaoxin.databinding.ActivityAppUpdateBinding;
import com.turunsi.yaoxin.databinding.ActivityMineDownLoadBinding;
import com.turunsi.yaoxin.main.MainActivity;
import com.yaoxin.appbase.activity.BaseActivity;
import com.yaoxin.appbase.model.NetData;
import com.yaoxin.appbase.model.ParamsBean;
import com.yaoxin.appbase.model.RegisterBean;
import com.yaoxin.appbase.net.CommonCallback;
import com.yaoxin.appbase.net.HttpUtil;
import com.yaoxin.appbase.update.ycupdatelib.UpdateFragment;
import com.yaoxin.appbase.utils.ToastUtils;

import retrofit2.Call;
import retrofit2.Response;

public class AppUpdateActivity extends BaseActivity implements View.OnClickListener {

  private ActivityAppUpdateBinding binding;

  String _currentVersion;
    DownLoadBean _downLoadBean;

  @Override
  protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
//    changeStatusBarColor(R.color.color_e9eff5);
    binding = ActivityAppUpdateBinding.inflate(getLayoutInflater());
    setContentView(binding.getRoot());
    initView();
  }

  private void initView() {

    _currentVersion = BuildConfig.VERSION_NAME;
    binding.activityAppUpdateNav.addCloseImageButton().setOnClickListener(this);

    binding.activityAppUpdateOptTv.setOnClickListener(this);


    binding.activityAppUpdateCurrentVersionTv.setText("当前版本:" + _currentVersion);
  }

  @Override
  protected void _requestData() {

      HttpUtil.apiW().customer_about(new RegisterBean())
              .enqueue(new CommonCallback<NetData>() {
                  @Override
                  public void Successful(Call<NetData> call, Response<NetData> response, NetData body) {

                      DownLoadBean downLoadBean = new Gson().fromJson(body.data.toString(),DownLoadBean.class);
                      for (DownLoadBean tempBean : downLoadBean.linkUrl) {
                          if (tempBean.appType.equals("ANDROID")) {

                              _downLoadBean = tempBean;
                              if (_currentVersion.equals(_downLoadBean.version)) {
                                  binding.activityAppUpdateOptTv.setText("已是最新版本");
                              } else {
                                  binding.activityAppUpdateOptTv.setText("去更新");
                              }
                              binding.activityAppUpdateLastedVersionTv.setText(_downLoadBean.version);

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
    if (v == binding.activityAppUpdateNav.addCloseImageButton()) {
      finish();
    } else if (v == binding.activityAppUpdateOptTv) {
        String textStr = getTextStr(binding.activityAppUpdateOptTv);
        if ("去更新".equals(textStr)) {
            if (_downLoadBean.downloadUrl != null) {
                try {
                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    // 设置URL，替换为你想打开的网页地址
                    intent.setData(Uri.parse(_downLoadBean.downloadUrl));

                    // 启动Intent，跳转到浏览器
                    startActivity(intent);
                } catch (Exception e) {
                }
            }
        }

    }
  }

}
