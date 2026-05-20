// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.chatkit.ui.page;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.Nullable;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;
import com.netease.yunxin.kit.chatkit.ui.databinding.ActivityWatchImageVideoBinding;
import com.netease.yunxin.kit.chatkit.ui.page.viewmodel.WatchImageVideoViewModel;
import com.netease.yunxin.kit.common.ui.activities.BaseActivity;

/** BaseActivity for Watch picture or video */
public abstract class WatchBaseActivity extends BaseActivity {

  WatchImageVideoViewModel viewModel;
  ActivityWatchImageVideoBinding binding;

  @Override
  protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
    binding = ActivityWatchImageVideoBinding.inflate(getLayoutInflater());
    setContentView(binding.getRoot());
    applySystemBarInsets();
    initData(getIntent());
    initViewModel();
    initDataObserver();
    initView();
  }

  private void applySystemBarInsets() {
    ViewCompat.setOnApplyWindowInsetsListener(
        binding.getRoot(),
        (view, windowInsets) -> {
          Insets insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars());
          ViewGroup.LayoutParams lp = binding.mediaToolbar.getLayoutParams();
          if (lp instanceof ViewGroup.MarginLayoutParams) {
            ViewGroup.MarginLayoutParams marginLp = (ViewGroup.MarginLayoutParams) lp;
            marginLp.topMargin = insets.top;
            marginLp.leftMargin = insets.left;
            marginLp.rightMargin = insets.right;
            binding.mediaToolbar.setLayoutParams(marginLp);
          }
          return windowInsets;
        });
    ViewCompat.requestApplyInsets(binding.getRoot());
  }

  public void initData(Intent intent) {}

  public void initViewModel() {
    viewModel = new ViewModelProvider(this).get(WatchImageVideoViewModel.class);
  }

  public void initView() {
    binding.mediaClose.setOnClickListener(v -> finish());
    binding.mediaDownload.setOnClickListener(v -> saveMedia());
    binding.mediaContainer.addView(initMediaView());
  }

  public void initDataObserver() {}

  public abstract View initMediaView();

  public abstract void saveMedia();
}
