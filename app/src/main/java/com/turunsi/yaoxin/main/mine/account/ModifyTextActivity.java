// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.turunsi.yaoxin.main.mine.account;

import android.os.Bundle;
import android.text.InputFilter;
import android.text.Spanned;
import android.view.View;

import androidx.annotation.Nullable;

import com.netease.yunxin.kit.common.ui.widgets.BackTitleBar;
import com.netease.yunxin.kit.contactkit.ui.R;
import com.netease.yunxin.kit.contactkit.ui.databinding.FunCommentActivityLayoutBinding;
import com.netease.yunxin.kit.contactkit.ui.userinfo.BaseCommentActivity;
import com.turunsi.yaoxin.databinding.ActivityMineAccountDetailBinding;
import com.turunsi.yaoxin.databinding.ActivityMineAccountModifyBinding;
import com.yaoxin.appbase.activity.BaseActivity;
import com.yaoxin.appbase.model.NetData;
import com.yaoxin.appbase.model.RegisterBean;
import com.yaoxin.appbase.model.UserBean;
import com.yaoxin.appbase.net.CommonCallback;
import com.yaoxin.appbase.net.HttpUtil;
import com.yaoxin.appbase.utils.BaseEvent;
import com.yaoxin.appbase.utils.DataUtil;
import com.yaoxin.appbase.utils.ToastUtils;

import org.greenrobot.eventbus.EventBus;

import retrofit2.Call;
import retrofit2.Response;

public class ModifyTextActivity extends BaseActivity implements View.OnClickListener {

    ActivityMineAccountModifyBinding binding;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMineAccountModifyBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        binding.activityMineAccountModifyNav.addCloseImageButton().setOnClickListener(this);
        binding.activityMineAccountModifyRl.setOnClickListener(this);
        // 输入时仅允许中文、字母、数字，禁止特殊字符\
        binding.activityMineAccountModifyEt.setFilter(new InputFilter[]{new InputFilter() {
            @Override
            public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
                StringBuilder sb = new StringBuilder();
                for (int i = start; i < end; i++) {
                    char c = source.charAt(i);
                    if (Character.isLetterOrDigit(c) || (c >= 0x4e00 && c <= 0x9fa5)) {
                        sb.append(c);
                    }
                }
                if (sb.length() == end - start) return null;
                if (sb.length() == 0) return "";
                return sb;
            }
        }});
    }

    @Override
    public void onClick(View v) {
        if (v == binding.activityMineAccountModifyNav.addCloseImageButton()) {
            finish();
        } else if (v == binding.activityMineAccountModifyRl) {
            String textStr = binding.activityMineAccountModifyEt.getText();
            if (textStr == null || textStr.isEmpty()) {
                ToastUtils.toastMsg("请输入用户名");
                return;
            }
            updatePersonInfo("", textStr);
        }
    }


    void updatePersonInfo(String headUrl, String name) {
        RegisterBean bean = new RegisterBean();
        if (!headUrl.isEmpty()) {
            bean.avatar = headUrl;
        }
        if (!name.isEmpty()) {
            bean.name = name;
        }
        HttpUtil.apiW().home_changeInfo(bean)
                .enqueue(new CommonCallback<NetData>() {
                    @Override
                    public void Successful(Call<NetData> call, Response<NetData> response, NetData body) {
                        ToastUtils.toastMsg(body.msg);
                        UserBean userInfo = DataUtil.getUserInfo();
                        userInfo.username = name;
                        DataUtil.putUserInfo(userInfo);
                        DataUtil.updateLoginUserInfoList(userInfo);
                        BaseEvent refreshUserInfo = new BaseEvent("refreshUserInfo");
                        refreshUserInfo.setText(name);
                        EventBus.getDefault().post(refreshUserInfo);
                        finish();
                    }

                    @Override
                    public void Failure(Call<NetData> call, Throwable t) {

                    }
                });
    }

}
