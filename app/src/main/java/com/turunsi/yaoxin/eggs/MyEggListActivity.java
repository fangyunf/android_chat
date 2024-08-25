// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.turunsi.yaoxin.eggs;

import android.content.Context;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;

import androidx.annotation.Nullable;

import com.turunsi.yaoxin.databinding.ActivityEggListIndexBinding;
import com.turunsi.yaoxin.databinding.ActivityMineEggListBinding;
import com.yaoxin.appbase.activity.BaseActivity;
import com.yaoxin.appbase.pswkeyboard.OnPasswordInputFinish;
import com.yaoxin.appbase.pswkeyboard.widget.PopEnterPassword;
import com.yaoxin.appbase.utils.StatusBarUtils;

/**
 * team setting activity
 */
public class MyEggListActivity extends BaseActivity implements View.OnClickListener {

    ActivityMineEggListBinding binding;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding =
                ActivityMineEggListBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        _initView();
        StatusBarUtils.transtStatusBar(this,binding.activityMineEggListNav);


    }

    @Override
    protected void _initView() {


        binding.activityMineEggListNav.addCloseImageButton().setOnClickListener(this);


    }

    @Override
    protected void _requestData() {

    }



    @Override
    public void onClick(View view) {
        if (view == binding.activityMineEggListNav.addCloseImageButton()) {
            finish();
        }
    }

}
