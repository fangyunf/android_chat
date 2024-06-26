// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.turunsi.yaoxin.main.mine.setting;

import android.os.Bundle;
import androidx.annotation.Nullable;
import com.turunsi.yaoxin.AppSkinConfig;
import com.turunsi.yaoxin.R;
import com.turunsi.yaoxin.databinding.ActivitySkinSettingBinding;
import com.turunsi.yaoxin.main.MainActivity;
import com.netease.yunxin.kit.common.ui.activities.BaseActivity;
import com.netease.yunxin.kit.corekit.event.EventCenter;

public class SkinActivity extends BaseActivity {

  private ActivitySkinSettingBinding viewBinding;

  @Override
  protected void onCreate(@Nullable Bundle savedInstanceState) {
    changeStatusBarColor(R.color.color_ededed);
    super.onCreate(savedInstanceState);
    viewBinding = ActivitySkinSettingBinding.inflate(getLayoutInflater());
    setContentView(viewBinding.getRoot());
    initView();
  }

  private void initView() {
    AppSkinConfig.AppSkin skin = AppSkinConfig.getInstance().getAppSkinStyle();
    setRadioSelected(skin);
    viewBinding.rgSkin.setOnCheckedChangeListener(
        (group, checkedId) -> {
          if (checkedId == viewBinding.rbBaseSkin.getId()) {
            AppSkinConfig.getInstance().setAppSkinStyle(AppSkinConfig.AppSkin.baseSkin);
          } else if (checkedId == viewBinding.rbCommonSkin.getId()) {
            AppSkinConfig.getInstance().setAppSkinStyle(AppSkinConfig.AppSkin.commonSkin);
          }
          EventCenter.notifyEvent(new MainActivity.SkinEvent());
          finish();
        });
    viewBinding.settingTitleBar.setOnBackIconClickListener(v -> onBackPressed());
  }

  private void setRadioSelected(AppSkinConfig.AppSkin skin) {
    if (skin == AppSkinConfig.AppSkin.baseSkin) {
      viewBinding.rbBaseSkin.setChecked(true);
    } else if (skin == AppSkinConfig.AppSkin.commonSkin) {
      viewBinding.rbCommonSkin.setChecked(true);
    }
  }
}
