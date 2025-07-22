// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.contactkit.ui.fun.userinfo;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.netease.yunxin.kit.common.ui.widgets.BackTitleBar;
import com.netease.yunxin.kit.common.utils.NetworkUtils;
import com.netease.yunxin.kit.contactkit.ui.R;
import com.netease.yunxin.kit.contactkit.ui.databinding.FunCommentActivityLayoutBinding;
import com.netease.yunxin.kit.contactkit.ui.userinfo.BaseCommentActivity;
import com.yaoxin.appbase.activity.BaseActivity;
import com.yaoxin.appbase.model.NetData;
import com.yaoxin.appbase.model.RegisterBean;
import com.yaoxin.appbase.net.CommonCallback;
import com.yaoxin.appbase.net.HttpUtil;
import com.yaoxin.appbase.utils.StatusBarUtils;

import retrofit2.Call;
import retrofit2.Response;

public class FunCommentActivity extends BaseActivity {
    public static final String REQUEST_COMMENT_NAME_KEY = "comment";
    FunCommentActivityLayoutBinding binding;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = FunCommentActivityLayoutBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        String comment = getIntent().getStringExtra(REQUEST_COMMENT_NAME_KEY);
        comment = comment == null ? "" : comment;
        binding.edtComment.setText(comment);
        StatusBarUtils.transtStatusBar(this, binding.funChatSettingActivityNav);
        binding.funChatSettingActivityNav.addCloseImageButton().setOnClickListener(view -> finish());
        binding.tvSave.setOnClickListener(view -> {
            if (!NetworkUtils.isConnected()) {
                Toast.makeText(FunCommentActivity.this, R.string.contact_network_error_tip, Toast.LENGTH_SHORT).show();
                return;
            }
            Intent intent = new Intent();
            if (!TextUtils.isEmpty(binding.edtComment.getText())) {
                intent.putExtra(REQUEST_COMMENT_NAME_KEY, binding.edtComment.getText());
            }
            setResult(RESULT_OK, intent);
            finish();
        });
    }

    //    @Override
//    protected View initViewAndGetRootView(Bundle savedInstanceState) {
//        FunCommentActivityLayoutBinding binding =
//                FunCommentActivityLayoutBinding.inflate(getLayoutInflater());
////        titleBar = binding.title;
////        titleBar.setActionTextColor(getResources().getColor(com.yaoxin.appbase.R.color.black));
//        binding.funChatSettingActivityNav.getTitleView().setText("");
//        edtComment = binding.edtComment;
//        return binding.getRoot();
//    }
//
//    @Override
//    protected void onCreate(@Nullable Bundle savedInstanceState) {
//        changeStatusBarColor(R.color.color_ededed);
//        super.onCreate(savedInstanceState);
//    }
//
//    protected void configTitle(BackTitleBar titleBar) {
//        super.configTitle(titleBar);
//        titleBar.getTitleTextView().setTextSize(17);
//        titleBar.getTitleTextView().setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));
//        RegisterBean bean = new RegisterBean();
//    }
}
