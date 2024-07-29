// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.turunsi.yaoxin.main.mine.setting;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.google.gson.Gson;
import com.netease.yunxin.kit.corekit.route.XKitRouter;
import com.turunsi.yaoxin.databinding.ActivityMineAboutUsNewBinding;
import com.turunsi.yaoxin.databinding.ActivityMineDownLoadBinding;
import com.yaoxin.appbase.activity.BaseActivity;
import com.yaoxin.appbase.model.NetData;
import com.yaoxin.appbase.model.RegisterBean;
import com.yaoxin.appbase.net.CommonCallback;
import com.yaoxin.appbase.net.Constant;
import com.yaoxin.appbase.net.HttpUtil;
import com.yaoxin.appbase.utils.ToastUtils;

import retrofit2.Call;
import retrofit2.Response;

public class DownLoadActivity extends BaseActivity implements View.OnClickListener {

  private ActivityMineDownLoadBinding viewBinding;

  @Override
  protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
//    changeStatusBarColor(R.color.color_e9eff5);
    viewBinding = ActivityMineDownLoadBinding.inflate(getLayoutInflater());
    setContentView(viewBinding.getRoot());
    initView();
  }

  private void initView() {

    viewBinding.activityMineDownLoadNav.addCloseImageButton().setOnClickListener(this);
    viewBinding.activityMineDownLoadIos.setOnClickListener(this);
    viewBinding.activityMineDownLoadAndroid.setOnClickListener(this);


  }


  @Override
  protected void _requestData() {

    HttpUtil.apiW().customer_about(new RegisterBean())
            .enqueue(new CommonCallback<NetData>() {
              @Override
              public void Successful(Call<NetData> call, Response<NetData> response, NetData body) {

                DownLoadBean downLoadBean = new Gson().fromJson(body.data.toString(),DownLoadBean.class);
                for (DownLoadBean tempBean : downLoadBean.linkUrl) {
                  if (tempBean.appType.equals("IOS")) {
                    viewBinding.activityMineDownLoadIos.setText(tempBean.downloadUrl);

                  }
                  if (tempBean.appType.equals("ANDROID")) {
                    viewBinding.activityMineDownLoadAndroid.setText(tempBean.downloadUrl);
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
    if (v == viewBinding.activityMineDownLoadIos) {
      // 获取文本内容
      String textToCopy = viewBinding.activityMineDownLoadIos.getText().toString();

      // 获取剪切板管理器
      ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);

      // 创建ClipData对象并将文本复制到剪切板
      ClipData clip = ClipData.newPlainText("label", textToCopy);
      clipboard.setPrimaryClip(clip);

      // 提示用户内容已复制
      ToastUtils.toastMsg("已复制到剪切板");
    } else if (v == viewBinding.activityMineDownLoadAndroid) {
      // 获取文本内容
      String textToCopy = viewBinding.activityMineDownLoadAndroid.getText().toString();

      // 获取剪切板管理器
      ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);

      // 创建ClipData对象并将文本复制到剪切板
      ClipData clip = ClipData.newPlainText("label", textToCopy);
      clipboard.setPrimaryClip(clip);

      // 提示用户内容已复制
      ToastUtils.toastMsg("已复制到剪切板");

    } else if (v == viewBinding.activityMineDownLoadNav.addCloseImageButton()) {
      finish();
    }
  }

}
