// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.turunsi.yaoxin.main.mine.account;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.Nullable;

import com.google.gson.Gson;
import com.netease.yunxin.kit.common.ui.dialog.ChoiceListener;
import com.netease.yunxin.kit.common.ui.dialog.CommonChoiceDialog;
import com.netease.yunxin.kit.teamkit.ui.databinding.FunTeamSettingGroupManagerActivityBinding;
import com.netease.yunxin.kit.teamkit.ui.fun.activity.FunTeamSettingNew_ForbiddenListActivity;
import com.netease.yunxin.kit.teamkit.ui.fun.activity.FunTeamSettingNew_TeamUsersActivity;
import com.netease.yunxin.kit.teamkit.ui.fun.activity.adapter.TeamSettingUserInfoAdapter;
import com.turunsi.yaoxin.databinding.ActivityAccountAnquanManagerBinding;
import com.yaoxin.appbase.activity.BaseActivity;
import com.yaoxin.appbase.model.GroupInfoBean;
import com.yaoxin.appbase.model.NetData;
import com.yaoxin.appbase.model.RegisterBean;
import com.yaoxin.appbase.net.CommonCallback;
import com.yaoxin.appbase.net.HttpUtil;
import com.yaoxin.appbase.utils.StatusBarUtils;
import com.yaoxin.appbase.utils.ToastUtils;

import java.util.ArrayList;
import java.util.HashMap;

import retrofit2.Call;
import retrofit2.Response;

/**
 * team setting activity
 */
public class AccountAnQuanManagerActivity extends BaseActivity implements View.OnClickListener {

    ActivityAccountAnquanManagerBinding binding;
    GroupInfoBean groupInfoBean = new GroupInfoBean();
    protected ActivityResultLauncher<Intent> launcher;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding =
                ActivityAccountAnquanManagerBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        _initView();

    }

    @Override
    protected void _initView() {
        StatusBarUtils.transtStatusBar(this, binding.activityAccountAnquanManagerNav);
        binding.activityAccountAnquanManagerNav.addCloseImageButton().setOnClickListener(this);
        // 第一部分：加我为朋友时需要验证
        binding.activityAccountAnquanManagerCell1.viewTitleDetailArrowTemplateTitleTv.setText("加我为朋友时需要验证");
        binding.activityAccountAnquanManagerCell1.viewTitleDetailArrowTemplateDetailTv.setVisibility(View.GONE);
        binding.activityAccountAnquanManagerCell1.viewTitleDetailArrowTemplateSwitch.setVisibility(View.VISIBLE);
        binding.activityAccountAnquanManagerCell1.viewTitleDetailArrowTemplateSwitch.setOnClickListener(this);

        // 第二部分：可通过以下方式找到我
        // 手机号
        binding.activityAccountAnquanManagerCell2.viewTitleDetailArrowTemplateTitleTv.setText("手机号");
        binding.activityAccountAnquanManagerCell2.viewTitleDetailArrowTemplateDetailTv.setVisibility(View.GONE);
        binding.activityAccountAnquanManagerCell2.viewTitleDetailArrowTemplateSwitch.setVisibility(View.VISIBLE);
        binding.activityAccountAnquanManagerCell2.viewTitleDetailArrowTemplateSwitch.setOnClickListener(this);

        // ID
        binding.activityAccountAnquanManagerCell3.viewTitleDetailArrowTemplateTitleTv.setText("ID");
        binding.activityAccountAnquanManagerCell3.viewTitleDetailArrowTemplateDetailTv.setVisibility(View.GONE);
        binding.activityAccountAnquanManagerCell3.viewTitleDetailArrowTemplateSwitch.setVisibility(View.VISIBLE);
        binding.activityAccountAnquanManagerCell3.viewTitleDetailArrowTemplateSwitch.setOnClickListener(this);

        // 第三部分：可通过以下方式添加我
        // 二维码
        binding.activityAccountAnquanManagerCell4.viewTitleDetailArrowTemplateTitleTv.setText("二维码");
        binding.activityAccountAnquanManagerCell4.viewTitleDetailArrowTemplateDetailTv.setVisibility(View.GONE);
        binding.activityAccountAnquanManagerCell4.viewTitleDetailArrowTemplateSwitch.setVisibility(View.VISIBLE);
        binding.activityAccountAnquanManagerCell4.viewTitleDetailArrowTemplateSwitch.setOnClickListener(this);

        // 群聊
        binding.activityAccountAnquanManagerCell5.viewTitleDetailArrowTemplateTitleTv.setText("群聊");
        binding.activityAccountAnquanManagerCell5.viewTitleDetailArrowTemplateDetailTv.setVisibility(View.GONE);
        binding.activityAccountAnquanManagerCell5.viewTitleDetailArrowTemplateSwitch.setVisibility(View.VISIBLE);
        binding.activityAccountAnquanManagerCell5.viewTitleDetailArrowTemplateSwitch.setOnClickListener(this);

        // 名片
        binding.activityAccountAnquanManagerCell6.viewTitleDetailArrowTemplateTitleTv.setText("名片");
        binding.activityAccountAnquanManagerCell6.viewTitleDetailArrowTemplateDetailTv.setVisibility(View.GONE);
        binding.activityAccountAnquanManagerCell6.viewTitleDetailArrowTemplateSwitch.setVisibility(View.VISIBLE);
        binding.activityAccountAnquanManagerCell6.viewTitleDetailArrowTemplateSwitch.setOnClickListener(this);
    }

    @Override
    protected void _requestData() {
//        RegisterBean bean = new RegisterBean();
//        bean.groupId = groupId;
        HttpUtil.apiW().home_securityPrivacy()
                .enqueue(new CommonCallback<NetData>() {
                    @Override
                    public void Successful(Call<NetData> call, Response<NetData> response, NetData body) {

                        groupInfoBean = new Gson().fromJson(body.data.toString(), GroupInfoBean.class);
                        updateUI();
                    }

                    @Override
                    public void Failure(Call<NetData> call, Throwable t) {

                    }
                });
    }

    void updateUI() {
        binding.activityAccountAnquanManagerCell1.viewTitleDetailArrowTemplateSwitch.setSelected(groupInfoBean.check == 1);
        binding.activityAccountAnquanManagerCell2.viewTitleDetailArrowTemplateSwitch.setSelected(groupInfoBean.phoneAdd == 1);
        binding.activityAccountAnquanManagerCell3.viewTitleDetailArrowTemplateSwitch.setSelected(groupInfoBean.idAdd == 1);
        binding.activityAccountAnquanManagerCell4.viewTitleDetailArrowTemplateSwitch.setSelected(groupInfoBean.qrAdd == 1);
        binding.activityAccountAnquanManagerCell5.viewTitleDetailArrowTemplateSwitch.setSelected(groupInfoBean.addGroupState == 1);
        binding.activityAccountAnquanManagerCell6.viewTitleDetailArrowTemplateSwitch.setSelected(groupInfoBean.cardAdd == 1);
    }


    @Override
    public void onClick(View view) {
        if (view == binding.activityAccountAnquanManagerNav.addCloseImageButton()) {
            finish();
        } else if (view == binding.activityAccountAnquanManagerCell1.viewTitleDetailArrowTemplateSwitch) {
            doOptWithType(0);
        } else if (view == binding.activityAccountAnquanManagerCell2.viewTitleDetailArrowTemplateSwitch) {
            doOptWithType(1);
        } else if (view == binding.activityAccountAnquanManagerCell3.viewTitleDetailArrowTemplateSwitch) {
            doOptWithType(2);
        } else if (view == binding.activityAccountAnquanManagerCell4.viewTitleDetailArrowTemplateSwitch) {
            doOptWithType(4); // 二维码
        } else if (view == binding.activityAccountAnquanManagerCell5.viewTitleDetailArrowTemplateSwitch) {
            doOptWithType(6); // 群聊
        } else if (view == binding.activityAccountAnquanManagerCell6.viewTitleDetailArrowTemplateSwitch) {
            doOptWithType(3); // 名片
        }
    }


    void doOptWithType(int type) {
        RegisterBean bean = new RegisterBean();
        if (type == 0) {
            bean.check = binding.activityAccountAnquanManagerCell1.viewTitleDetailArrowTemplateSwitch.isSelected() ? "0" : "1";
        }
        if (type == 1) {
            bean.phoneAdd = binding.activityAccountAnquanManagerCell2.viewTitleDetailArrowTemplateSwitch.isSelected() ? "0" : "1";
        }
        if (type == 2) {
            bean.idAdd = binding.activityAccountAnquanManagerCell3.viewTitleDetailArrowTemplateSwitch.isSelected() ? "0" : "1";
        }
        if (type == 3) {
            bean.cardAdd = binding.activityAccountAnquanManagerCell6.viewTitleDetailArrowTemplateSwitch.isSelected() ? "0" : "1";
        }
        if (type == 4) {
            bean.qrAdd = binding.activityAccountAnquanManagerCell4.viewTitleDetailArrowTemplateSwitch.isSelected() ? "0" : "1";
        }
        if (type == 6) {
            bean.addGroupState = binding.activityAccountAnquanManagerCell5.viewTitleDetailArrowTemplateSwitch.isSelected() ? "0" : "1";
        }

        HttpUtil.apiW().home_changeSecurityPrivacy(bean)
                .enqueue(new CommonCallback<NetData>() {
                    @Override
                    public void Successful(Call<NetData> call, Response<NetData> response, NetData body) {
                        ToastUtils.toastMsg(body.msg);
                        // 切换开关状态
                        if (type == 0) {
                            binding.activityAccountAnquanManagerCell1.viewTitleDetailArrowTemplateSwitch.setSelected(!binding.activityAccountAnquanManagerCell1.viewTitleDetailArrowTemplateSwitch.isSelected());
                        }
                        if (type == 1) {
                            binding.activityAccountAnquanManagerCell2.viewTitleDetailArrowTemplateSwitch.setSelected(!binding.activityAccountAnquanManagerCell2.viewTitleDetailArrowTemplateSwitch.isSelected());
                        }
                        if (type == 2) {
                            binding.activityAccountAnquanManagerCell3.viewTitleDetailArrowTemplateSwitch.setSelected(!binding.activityAccountAnquanManagerCell3.viewTitleDetailArrowTemplateSwitch.isSelected());
                        }
                        if (type == 3) {
                            binding.activityAccountAnquanManagerCell6.viewTitleDetailArrowTemplateSwitch.setSelected(!binding.activityAccountAnquanManagerCell6.viewTitleDetailArrowTemplateSwitch.isSelected());
                        }
                        if (type == 4) {
                            binding.activityAccountAnquanManagerCell4.viewTitleDetailArrowTemplateSwitch.setSelected(!binding.activityAccountAnquanManagerCell4.viewTitleDetailArrowTemplateSwitch.isSelected());
                        }
                        if (type == 6) {
                            binding.activityAccountAnquanManagerCell5.viewTitleDetailArrowTemplateSwitch.setSelected(!binding.activityAccountAnquanManagerCell5.viewTitleDetailArrowTemplateSwitch.isSelected());
                        }
                    }

                    @Override
                    public void Failure(Call<NetData> call, Throwable t) {
                        // 如果失败，重新请求数据以恢复状态
                        _requestData();
                    }
                });
    }

}
