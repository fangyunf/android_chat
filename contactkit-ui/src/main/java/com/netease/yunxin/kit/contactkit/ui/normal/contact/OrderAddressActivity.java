// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.contactkit.ui.normal.contact;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.netease.yunxin.kit.contactkit.ui.databinding.ActivityOrderAddressListBinding;
import com.netease.yunxin.kit.contactkit.ui.databinding.ActivitySubmitOrderBinding;
import com.netease.yunxin.kit.contactkit.ui.normal.contact.adapter.ShopaddressListAdapter;
import com.yaoxin.appbase.activity.BaseActivity;

public class OrderAddressActivity extends BaseActivity implements View.OnClickListener {

  private ActivityOrderAddressListBinding  viewBinding;

  @Override
  protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
//    changeStatusBarColor(R.color.color_e9eff5);
    viewBinding = ActivityOrderAddressListBinding.inflate(getLayoutInflater());
    viewBinding.activityOrderAddressListRv.setLayoutManager(new LinearLayoutManager(this));
    ShopaddressListAdapter adapter = new ShopaddressListAdapter();
    viewBinding.activityOrderAddressListRv.setAdapter(adapter);
    setContentView(viewBinding.getRoot());
    initView();
  }

  private void initView() {

    viewBinding.activityOrderAddressListNav.addCloseImageButton().setOnClickListener(this);

  }

  @Override
  public void onClick(View v) {
   if (v == viewBinding.activityOrderAddressListNav.addCloseImageButton()) {
      finish();

    }
  }

}
