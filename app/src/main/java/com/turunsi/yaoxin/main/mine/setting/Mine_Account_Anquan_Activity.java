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
import com.netease.yunxin.kit.common.ui.dialog.ChoiceListener;
import com.netease.yunxin.kit.common.ui.dialog.CommonChoiceDialog;
import com.netease.yunxin.kit.corekit.im.IMKitClient;
import com.netease.yunxin.kit.corekit.im.provider.FetchCallback;
import com.netease.yunxin.kit.corekit.im.repo.MiscRepo;
import com.turunsi.yaoxin.IMApplication;
import com.turunsi.yaoxin.databinding.ActivityAccountAnquanManagerNewBinding;
import com.turunsi.yaoxin.databinding.ActivityMineSetNewBinding;
import com.turunsi.yaoxin.login.LoginActivity;
import com.turunsi.yaoxin.register.ForgetPwdActivity;
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

public class Mine_Account_Anquan_Activity extends BaseActivity implements View.OnClickListener {

  private ActivityAccountAnquanManagerNewBinding viewBinding;

  @Override
  protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
//    changeStatusBarColor(R.color.color_e9eff5);
    viewBinding = ActivityAccountAnquanManagerNewBinding.inflate(getLayoutInflater());
    setContentView(viewBinding.getRoot());
    initView();
  }

  private void initView() {

    viewBinding.activityAccountAnquanManagerNav.addCloseImageButton().setOnClickListener(this);
    viewBinding.activityMineSetNewPhone.viewTitleArrowLl.setOnClickListener(this);
      viewBinding.activityMineSetNewPhone.viewTitleArrowArrowIv.setVisibility(View.GONE);
      viewBinding.activityMineSetNewPhone.viewTitleArrowRightTv.setText(DataUtil.getUserInfo().phoneNo);
    viewBinding.activityMineSetNewModifyPwd.viewTitleArrowLl.setOnClickListener(this);


    viewBinding.activityMineSetNewPhone.viewTitleArrowTv.setText("当前手机号码");
      viewBinding.activityMineSetNewPhone.viewTitleArrowRightTv.setVisibility(View.VISIBLE);
      viewBinding.activityMineSetNewPhone.viewTitleArrowRightTv.setText(DataUtil.getUserInfo().phoneNo);
    viewBinding.activityMineSetNewModifyPwd.viewTitleArrowTv.setText("修改登录密码");
  }

  @Override
  public void onClick(View v) {
    if (v == viewBinding.activityMineSetNewPhone.viewTitleArrowLl) {

    } else if (v == viewBinding.activityMineSetNewModifyPwd.viewTitleArrowLl) {
      ForgetPwdActivity.start(ForgetPwdActivity.class, this, null);

    } else if (v == viewBinding.activityAccountAnquanManagerNav.addCloseImageButton()) {
      finish();

    }
  }

}
