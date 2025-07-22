// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.teamkit.ui.fun.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.Nullable;

import com.netease.yunxin.kit.teamkit.ui.databinding.ActivityModifyInfoBinding;
import com.yaoxin.appbase.activity.BaseActivity;
import com.yaoxin.appbase.model.NetData;
import com.yaoxin.appbase.model.RegisterBean;
import com.yaoxin.appbase.model.UserBean;
import com.yaoxin.appbase.net.CommonCallback;
import com.yaoxin.appbase.net.HttpUtil;
import com.yaoxin.appbase.utils.DataUtil;
import com.yaoxin.appbase.utils.ToastUtils;

import retrofit2.Call;
import retrofit2.Response;

public class ModifyInfoActivity extends BaseActivity implements View.OnClickListener {

    ActivityModifyInfoBinding binding;
    String type;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        String title = getIntent().getStringExtra("title");
        type = getIntent().getStringExtra("type");
        String hint = getIntent().getStringExtra("hint");
        binding = ActivityModifyInfoBinding.inflate(getLayoutInflater());
        transtStatusBar(binding.activityModifyInfoNav);
        setContentView(binding.getRoot());
        binding.activityModifyInfoNav.addCloseImageButton().setOnClickListener(this);
        binding.activityModifyInfoSaveRl.setOnClickListener(this);
        if (title != null) {
            binding.activityModifyInfoNav.getTitleView().setText(title);
        }
        if (hint != null) {
            binding.activityModifyInfoEt.setText(hint);
        }
    }

    @Override
    public void onClick(View v) {
        if (v == binding.activityModifyInfoNav.addCloseImageButton()) {
            finish();
        } else if (v == binding.activityModifyInfoSaveRl) {
            String textStr = getTextStr(binding.activityModifyInfoEt);
            if (textStr == null || textStr.isEmpty()) {
                ToastUtils.toastMsg("请输入");
                return;
            }
            Intent resultIntent = new Intent();
            resultIntent.putExtra("type", type);
            resultIntent.putExtra("result", textStr);
            setResult(Activity.RESULT_OK, resultIntent);
            finish();
        }
    }


}
