// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.turunsi.yaoxin.welcome;


import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.netease.nimlib.sdk.auth.LoginInfo;
import com.turunsi.yaoxin.IMApplication;
import com.turunsi.yaoxin.R;
import com.turunsi.yaoxin.databinding.ActivityWelcomeBinding;
import com.turunsi.yaoxin.main.MainActivity;
import com.turunsi.yaoxin.utils.ChatHistoryCleaner;
import com.turunsi.yaoxin.utils.Constant;
import com.turunsi.yaoxin.utils.DataUtils;
import com.netease.yunxin.kit.alog.ALog;
import com.netease.yunxin.kit.common.ui.activities.BaseActivity;
import com.netease.yunxin.kit.common.ui.utils.ToastX;
import com.netease.yunxin.kit.corekit.im.IMKitClient;
import com.netease.yunxin.kit.corekit.im.login.LoginCallback;
import com.yaoxin.appbase.utils.DataUtil;

/** Welcome Page is launch page */
public class WelcomeActivity extends BaseActivity {

  private static final String TAG = "WelcomeActivity";
  private ActivityWelcomeBinding activityWelcomeBinding;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    ALog.d(Constant.PROJECT_TAG, TAG, "onCreateView");
    IMApplication.setColdStart(true);
    activityWelcomeBinding = ActivityWelcomeBinding.inflate(getLayoutInflater());
    setContentView(activityWelcomeBinding.getRoot());
    if (TextUtils.isEmpty(IMKitClient.account())) {
      startLogin();
    } else {
      showMainActivityAndFinish();
    }
  }

  private void showMainActivityAndFinish() {
    ALog.d(Constant.PROJECT_TAG, TAG, "showMainActivityAndFinish");
    Intent intent = new Intent();
    intent.setClass(this, MainActivity.class);
    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
    this.startActivity(intent);
    finish();
  }

  /** start login page, you can use to launch your own login */
  private void startLogin() {
    ALog.d(Constant.PROJECT_TAG, TAG, "startLogin");

      //填入你的 account and token
      //curl -X POST -H "AppKey: go9***3mgq3" -H "Nonce: 4t***23t23t" -H "CurTime: 1443592222" -H "CheckSum: 9e9db3b***583f86" -H "Content-Type: application/x-www-form-urlencoded" -d 'accid=123456&name=zhangsan' 'https://api.netease.im/nimserver/user/create.action'
      String account = DataUtil.getUserid();
      String token = DataUtil.getUserInfo().imToken;

      if (!TextUtils.isEmpty(account) && !TextUtils.isEmpty(token)) {
          loginIM(account,token);
    } else {
      showLoginView();
    }
  }

  private void showLoginView() {
    ALog.d(Constant.PROJECT_TAG, TAG, "showLoginView");
    activityWelcomeBinding.appDesc.setVisibility(View.GONE);
    activityWelcomeBinding.loginButton.setVisibility(View.VISIBLE);
    activityWelcomeBinding.appBottomIcon.setVisibility(View.GONE);
    activityWelcomeBinding.appBottomName.setVisibility(View.GONE);
    activityWelcomeBinding.tvEmailLogin.setVisibility(View.VISIBLE);
    activityWelcomeBinding.tvServerConfig.setVisibility(View.VISIBLE);
    activityWelcomeBinding.vEmailLine.setVisibility(View.VISIBLE);
    activityWelcomeBinding.loginButton.setOnClickListener(
        view -> {

          launchLoginPage();
        });
    activityWelcomeBinding.tvEmailLogin.setOnClickListener(
        view -> {

          launchLoginPage();
        });
    activityWelcomeBinding.tvServerConfig.setOnClickListener(
        view -> {
          Intent intent = new Intent(WelcomeActivity.this, ServerActivity.class);
          startActivity(intent);
        });
  }

  /** launch login activity */
  private void launchLoginPage() {
    ALog.d(Constant.PROJECT_TAG, TAG, "launchLoginPage");

  }

  /** when your own page login success, you should login IM SDK */
  private void loginIM(String account, String token) {
    ALog.d(Constant.PROJECT_TAG, TAG, "loginIM");
    activityWelcomeBinding.getRoot().setVisibility(View.GONE);
    LoginInfo loginInfo =
        LoginInfo.LoginInfoBuilder.loginInfoDefault(account, token)
            .withAppKey(DataUtils.readAppKey(this))
            .build();
    IMKitClient.loginIM(
        loginInfo,
        new LoginCallback<LoginInfo>() {
          @Override
          public void onError(int errorCode, @NonNull String errorMsg) {
            ToastX.showShortToast(
                String.format(getResources().getString(R.string.login_fail), errorCode));
            launchLoginPage();
          }

          @Override
          public void onSuccess(@Nullable LoginInfo data) {
            ChatHistoryCleaner.clearOldHistoryIfNeeded();
            showMainActivityAndFinish();
          }
        });
  }
}
