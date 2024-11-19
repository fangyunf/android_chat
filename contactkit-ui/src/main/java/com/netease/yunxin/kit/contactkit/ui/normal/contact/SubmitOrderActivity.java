// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.contactkit.ui.normal.contact;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.gson.Gson;
import com.netease.nimlib.sdk.misc.DirCacheFileType;
import com.netease.yunxin.kit.contactkit.ui.databinding.ActivitySubmitOrderBinding;
import com.netease.yunxin.kit.corekit.im.IMKitClient;
import com.netease.yunxin.kit.corekit.im.provider.FetchCallback;
import com.netease.yunxin.kit.corekit.im.repo.MiscRepo;
import com.yaoxin.appbase.activity.BaseActivity;
import com.yaoxin.appbase.model.GroupInfoBean;
import com.yaoxin.appbase.model.NetData;
import com.yaoxin.appbase.net.CommonCallback;
import com.yaoxin.appbase.net.HttpUtil;
import com.yaoxin.appbase.utils.DataUtil;
import com.yaoxin.appbase.utils.DialogAlertUtil;
import com.yaoxin.appbase.utils.GlideUtil;
import com.yaoxin.appbase.utils.ToastUtils;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Response;

public class SubmitOrderActivity extends BaseActivity implements View.OnClickListener {

  private ActivitySubmitOrderBinding viewBinding;
GroupInfoBean infoBean = new GroupInfoBean();
  @Override
  protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
//    changeStatusBarColor(R.color.color_e9eff5);
    viewBinding = ActivitySubmitOrderBinding.inflate(getLayoutInflater());
    viewBinding.activitySubmitOrderChooseAddressLl.setOnClickListener(this);
    setContentView(viewBinding.getRoot());
    if (extras != null && extras.get("data") != null) {
        infoBean = new Gson().fromJson((String) extras.get("data"), GroupInfoBean.class);
    }
    initView();
  }

  private void initView() {

    viewBinding.activitySubmitOrderNav.addCloseImageButton().setOnClickListener(this);
      GlideUtil.yh_loadImage(this, viewBinding.activitySubmitOrderShopIv, infoBean.avatar);
      viewBinding.activitySubmitOrderShopNameTv.setText(infoBean.name);
      viewBinding.activitySubmitOrderShopPriceTv.setText(infoBean.price1);
      viewBinding.activitySubmitOrderHejiTv.setText("合计" + infoBean.price1);
  }

  @Override
  public void onClick(View v) {
   if (v == viewBinding.activitySubmitOrderNav.addCloseImageButton()) {
      finish();

    }
   if (v == viewBinding.activitySubmitOrderChooseAddressLl) {
      OrderAddressActivity.start(OrderAddressActivity.class, this, null);

    }
  }

}
