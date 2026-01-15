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
import com.turunsi.yaoxin.main.mine.setting.SettingNotifyNewActivity;
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
        StatusBarUtils.transtStatusBar(this, binding.activityAccountAnquanManagerNav);
        _initView();

    }

    @Override
    protected void _initView() {
        binding.activityAccountAnquanManagerNav.addCloseImageButton().setOnClickListener(this);

        binding.activityAccountAnquanManagerCell1.viewTitleArrowTv.setText("加我为好友时需要验证");
        binding.activityAccountAnquanManagerCell1.viewTitleArrowRightTvSwitch.setOnClickListener(this);
        binding.activityAccountAnquanManagerCell1.viewTitleArrowRightTvSwitch.setVisibility(View.VISIBLE);
        binding.activityAccountAnquanManagerCell1.viewTitleArrowArrowIv.setVisibility(View.GONE);

        binding.activityAccountAnquanManagerCell2.viewTitleArrowTv.setText("允许通过手机号搜索我");
        binding.activityAccountAnquanManagerCell2.viewTitleArrowRightTvSwitch.setOnClickListener(this);
        binding.activityAccountAnquanManagerCell2.viewTitleArrowRightTvSwitch.setVisibility(View.VISIBLE);
        binding.activityAccountAnquanManagerCell2.viewTitleArrowArrowIv.setVisibility(View.GONE);

        binding.activityAccountAnquanManagerCell3.viewTitleArrowTv.setText("允许通过ID号搜索我");
        binding.activityAccountAnquanManagerCell3.viewTitleArrowRightTvSwitch.setOnClickListener(this);
        binding.activityAccountAnquanManagerCell3.viewTitleArrowRightTvSwitch.setVisibility(View.VISIBLE);
        binding.activityAccountAnquanManagerCell3.viewTitleArrowArrowIv.setVisibility(View.GONE);

        binding.activityAccountAnquanManagerCell4.viewTitleArrowTv.setText("允许通过名片加我好友");
        binding.activityAccountAnquanManagerCell4.viewTitleArrowRightTvSwitch.setOnClickListener(this);
        binding.activityAccountAnquanManagerCell4.viewTitleArrowRightTvSwitch.setVisibility(View.VISIBLE);
        binding.activityAccountAnquanManagerCell4.viewTitleArrowArrowIv.setVisibility(View.GONE);

        binding.activityAccountAnquanManagerCell5.viewTitleArrowTv.setText("允许通过二维码加我好友");
        binding.activityAccountAnquanManagerCell5.viewTitleArrowRightTvSwitch.setOnClickListener(this);
        binding.activityAccountAnquanManagerCell5.viewTitleArrowRightTvSwitch.setVisibility(View.VISIBLE);
        binding.activityAccountAnquanManagerCell5.viewTitleArrowArrowIv.setVisibility(View.GONE);

        binding.activityAccountAnquanManagerCell6.viewTitleArrowTv.setText("是否开启被添加好友功能");
        binding.activityAccountAnquanManagerCell6.viewTitleArrowRightTvSwitch.setOnClickListener(this);
        binding.activityAccountAnquanManagerCell6.viewTitleArrowRightTvSwitch.setVisibility(View.VISIBLE);
        binding.activityAccountAnquanManagerCell6.viewTitleArrowArrowIv.setVisibility(View.GONE);

        binding.activityAccountAnquanManagerCell7.viewTitleArrowTv.setText("是否开启加入群聊功能");
        binding.activityAccountAnquanManagerCell7.viewTitleArrowRightTvSwitch.setOnClickListener(this);
        binding.activityAccountAnquanManagerCell7.viewTitleArrowRightTvSwitch.setVisibility(View.VISIBLE);
        binding.activityAccountAnquanManagerCell7.viewTitleArrowArrowIv.setVisibility(View.GONE);

        binding.activitySetting.setOnClickListener(view -> SettingNotifyNewActivity.start(SettingNotifyNewActivity.class, AccountAnQuanManagerActivity.this, null));

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

        binding.activityAccountAnquanManagerCell1.viewTitleArrowRightTvSwitch.setSelected(groupInfoBean.check == 1);
        binding.activityAccountAnquanManagerCell2.viewTitleArrowRightTvSwitch.setSelected(groupInfoBean.phoneAdd == 1);
        binding.activityAccountAnquanManagerCell3.viewTitleArrowRightTvSwitch.setSelected(groupInfoBean.idAdd == 1);
        binding.activityAccountAnquanManagerCell4.viewTitleArrowRightTvSwitch.setSelected(groupInfoBean.cardAdd == 1);
        binding.activityAccountAnquanManagerCell5.viewTitleArrowRightTvSwitch.setSelected(groupInfoBean.qrAdd == 1);
        binding.activityAccountAnquanManagerCell6.viewTitleArrowRightTvSwitch.setSelected(groupInfoBean.addState == 1);
        binding.activityAccountAnquanManagerCell7.viewTitleArrowRightTvSwitch.setSelected(groupInfoBean.addGroupState == 1);

    }


    @Override
    public void onClick(View view) {
        if (view == binding.activityAccountAnquanManagerNav.addCloseImageButton()) {
            finish();
        } else if (view == binding.activityAccountAnquanManagerCell1.viewTitleArrowRightTvSwitch) {
            doOptWithType(0);
        } else if (view == binding.activityAccountAnquanManagerCell2.viewTitleArrowRightTvSwitch) {
            doOptWithType(1);
        } else if (view == binding.activityAccountAnquanManagerCell3.viewTitleArrowRightTvSwitch) {
            doOptWithType(2);
        } else if (view == binding.activityAccountAnquanManagerCell4.viewTitleArrowRightTvSwitch) {
            doOptWithType(3);
        } else if (view == binding.activityAccountAnquanManagerCell5.viewTitleArrowRightTvSwitch) {
            doOptWithType(4);
        } else if (view == binding.activityAccountAnquanManagerCell6.viewTitleArrowRightTvSwitch) {
            doOptWithType(5);
        } else if (view == binding.activityAccountAnquanManagerCell7.viewTitleArrowRightTvSwitch) {
            doOptWithType(6);
        }
    }


    void doOptWithType(int type) {

        RegisterBean bean = new RegisterBean();
        if (type == 0) {
            bean.check = binding.activityAccountAnquanManagerCell1.viewTitleArrowRightTvSwitch.isSelected() ? "0" : "1";
        }
        if (type == 1) {
            bean.phoneAdd = binding.activityAccountAnquanManagerCell2.viewTitleArrowRightTvSwitch.isSelected() ? "0" : "1";
        }
        if (type == 2) {
            bean.idAdd = binding.activityAccountAnquanManagerCell3.viewTitleArrowRightTvSwitch.isSelected() ? "0" : "1";
        }
        if (type == 3) {
            bean.cardAdd = binding.activityAccountAnquanManagerCell4.viewTitleArrowRightTvSwitch.isSelected() ? "0" : "1";
        }
        if (type == 4) {
            bean.qrAdd = binding.activityAccountAnquanManagerCell5.viewTitleArrowRightTvSwitch.isSelected() ? "0" : "1";
        }
        if (type == 5) {
            bean.addState = binding.activityAccountAnquanManagerCell6.viewTitleArrowRightTvSwitch.isSelected() ? "0" : "1";
        }
        if (type == 6) {
            bean.addGroupState = binding.activityAccountAnquanManagerCell7.viewTitleArrowRightTvSwitch.isSelected() ? "0" : "1";
        }

        HttpUtil.apiW().home_changeSecurityPrivacy(bean)
                .enqueue(new CommonCallback<NetData>() {
                    @Override
                    public void Successful(Call<NetData> call, Response<NetData> response, NetData body) {

                        ToastUtils.toastMsg(body.msg);
                        if (type == 0) {
                            binding.activityAccountAnquanManagerCell1.viewTitleArrowRightTvSwitch.setSelected(!binding.activityAccountAnquanManagerCell1.viewTitleArrowRightTvSwitch.isSelected());
                        }
                        if (type == 1) {
                            binding.activityAccountAnquanManagerCell2.viewTitleArrowRightTvSwitch.setSelected(!binding.activityAccountAnquanManagerCell2.viewTitleArrowRightTvSwitch.isSelected());
                        }
                        if (type == 2) {
                            binding.activityAccountAnquanManagerCell3.viewTitleArrowRightTvSwitch.setSelected(!binding.activityAccountAnquanManagerCell3.viewTitleArrowRightTvSwitch.isSelected());
                        }
                        if (type == 3) {
                            binding.activityAccountAnquanManagerCell4.viewTitleArrowRightTvSwitch.setSelected(!binding.activityAccountAnquanManagerCell4.viewTitleArrowRightTvSwitch.isSelected());
                        }
                        if (type == 4) {
                            binding.activityAccountAnquanManagerCell5.viewTitleArrowRightTvSwitch.setSelected(!binding.activityAccountAnquanManagerCell5.viewTitleArrowRightTvSwitch.isSelected());
                        }
                        if (type == 5) {
                            binding.activityAccountAnquanManagerCell6.viewTitleArrowRightTvSwitch.setSelected(!binding.activityAccountAnquanManagerCell6.viewTitleArrowRightTvSwitch.isSelected());
                        }
                        if (type == 6) {
                            binding.activityAccountAnquanManagerCell7.viewTitleArrowRightTvSwitch.setSelected(!binding.activityAccountAnquanManagerCell7.viewTitleArrowRightTvSwitch.isSelected());
                        }
                    }

                    @Override
                    public void Failure(Call<NetData> call, Throwable t) {

                    }
                });
    }

}
