// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.teamkit.ui.fun.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.Nullable;

import com.google.gson.Gson;
import com.netease.yunxin.kit.common.ui.dialog.ChoiceListener;
import com.netease.yunxin.kit.common.ui.dialog.CommonChoiceDialog;
import com.netease.yunxin.kit.corekit.im.utils.RouterConstant;
import com.netease.yunxin.kit.corekit.route.XKitRouter;
import com.netease.yunxin.kit.teamkit.ui.databinding.FunTeamSettingGroupManagerActivityBinding;
import com.netease.yunxin.kit.teamkit.ui.fun.activity.adapter.TeamSettingUserInfoAdapter;
import com.yaoxin.appbase.activity.BaseActivity;
import com.yaoxin.appbase.model.GroupInfoBean;
import com.yaoxin.appbase.model.NetData;
import com.yaoxin.appbase.model.RegisterBean;
import com.yaoxin.appbase.net.CommonCallback;
import com.yaoxin.appbase.net.HttpUtil;
import com.yaoxin.appbase.utils.BaseEvent;
import com.yaoxin.appbase.utils.GlideUtil;
import com.yaoxin.appbase.utils.StatusBarUtils;
import com.yaoxin.appbase.utils.ToastUtils;
import com.yaoxin.appbase.view.LoadingDialog;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.HashMap;

import retrofit2.Call;
import retrofit2.Response;

/**
 * team setting activity
 */
public class FunTeamSetting_GroupManagerActivity extends BaseActivity implements View.OnClickListener {

    FunTeamSettingGroupManagerActivityBinding binding;
    String groupId;
    int rankState;
    GroupInfoBean groupInfoBean = new GroupInfoBean();
    TeamSettingUserInfoAdapter adapter;// = new TeamSettingUserInfoAdapter(true, new ArrayList<>());

    protected ActivityResultLauncher<Intent> launcher;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        _getParams();
        if (extras != null && extras.get("groupId") != null) {
            groupId = (String) extras.get("groupId");
        }
        if (extras != null && extras.get("rankState") != null) {
            String rankS1 = (String) extras.get("rankState");
            rankState = Integer.parseInt(rankS1);
        }
        super.onCreate(savedInstanceState);
        binding = FunTeamSettingGroupManagerActivityBinding.inflate(getLayoutInflater());
        applyManagerPermissionUi();
        setContentView(binding.getRoot());
        StatusBarUtils.transtStatusBar(this, binding.funTeamSettingGroupManagerActivityNav);
        _initView();
    }

    @Override
    protected void _initView() {
        binding.funTeamSettingGroupManagerActivityNav.addCloseImageButton().setOnClickListener(this);
        binding.funTeamSettingGroupManagerActivityJiesanTv.setOnClickListener(this);
        binding.funTeamSettingGroupManagerActivityYaoqing.viewTitleDetailArrowTemplateTitleTv.setText("入群需审核");
        binding.funTeamSettingGroupManagerActivityYaoqing.viewTitleDetailArrowTemplateDetailTv.setVisibility(View.GONE);
        binding.funTeamSettingGroupManagerActivityYaoqing.viewTitleDetailArrowTemplateDetailTv.setText("启用后，需群主管理员确认才能邀请群成员");
        binding.funTeamSettingGroupManagerActivityYaoqing.viewTitleDetailArrowTemplateLl.setBackgroundResource(com.yaoxin.appbase.R.drawable.bg_white_rounded_10);
        binding.funTeamSettingGroupManagerActivityYaoqing.viewTitleDetailArrowTemplateSwitch.setOnClickListener(this);

        binding.funTeamSettingGroupManagerActivityQunzhuZhuanrang.viewTitleArrowTv.setText("转让群主");
        binding.funTeamSettingGroupManagerActivityQunzhuZhuanrang.viewTitleArrowLl.setOnClickListener(this);
        binding.funTeamSettingGroupManagerActivityQunzhuZhuanrang.viewTitleArrowLl.setBackgroundResource(
                com.yaoxin.appbase.R.drawable.bg_white_rounded_10);

        binding.funTeamSettingGroupManagerActivityGuanliyuanSet.viewTitleArrowTv.setText("设置管理员");
        binding.funTeamSettingGroupManagerActivityGuanliyuanSet.viewTitleArrowLl.setOnClickListener(this);
        binding.funTeamSettingGroupManagerActivityGuanliyuanSet.viewTitleArrowLl.setBackgroundResource(
                com.yaoxin.appbase.R.drawable.bg_white_rounded_10);

        binding.funTeamSettingGroupManagerActivityChengyuanJinyan.viewTitleDetailArrowTemplateTitleTv.setText("全员禁言");
        binding.funTeamSettingGroupManagerActivityChengyuanJinyan.viewTitleDetailArrowTemplateDetailTv.setText("启用后，群成员无法发送消息，群主管理员除外");
        binding.funTeamSettingGroupManagerActivityChengyuanJinyan.viewTitleDetailArrowTemplateDetailTv.setVisibility(View.GONE);
        binding.funTeamSettingGroupManagerActivityChengyuanJinyan.viewTitleDetailArrowTemplateLl.setBackgroundResource(com.yaoxin.appbase.R.drawable.bg_white_rounded_10);
        binding.funTeamSettingGroupManagerActivityChengyuanJinyan.viewTitleDetailArrowTemplateSwitch.setOnClickListener(this);

        binding.funTeamSettingGroupManagerActivityJinzhiLingquGouwuquan.viewTitleDetailArrowTemplateTitleTv.setText("禁止领取红包");
        binding.funTeamSettingGroupManagerActivityJinzhiLingquGouwuquan.viewTitleDetailArrowTemplateDetailTv.setText("启用后，群成员无法通过该群领红包");
        binding.funTeamSettingGroupManagerActivityJinzhiLingquGouwuquan.viewTitleDetailArrowTemplateSwitch.setOnClickListener(this);

        binding.funTeamSettingGroupManagerActivityJinzhiLingquGouwuquan.viewTitleDetailArrowTemplateDetailTv.setVisibility(View.GONE);
        binding.funTeamSettingGroupManagerActivityJinzhiLingquGouwuquan.viewTitleDetailArrowTemplateLl.setBackgroundResource(com.yaoxin.appbase.R.drawable.bg_white_rounded_10);

        binding.funTeamSettingGroupManagerActivityQunchengyuanBaohu.viewTitleDetailArrowTemplateTitleTv.setText("群成员保护模式");
        binding.funTeamSettingGroupManagerActivityQunchengyuanBaohu.viewTitleDetailArrowTemplateDetailTv.setText("启用后，群成员无法通过该群互加好友");
        binding.funTeamSettingGroupManagerActivityQunchengyuanBaohu.viewTitleDetailArrowTemplateSwitch.setOnClickListener(this);
        binding.funTeamSettingGroupManagerActivityQunchengyuanBaohu.viewTitleDetailArrowTemplateDetailTv.setVisibility(View.GONE);
        binding.funTeamSettingGroupManagerActivityQunchengyuanBaohu.viewTitleDetailArrowTemplateLl.setBackgroundResource(com.yaoxin.appbase.R.drawable.bg_white_rounded_10);

        binding.funTeamSettingGroupManagerActivityJinzhiLingquMingdan.viewTitleArrowLl.setVisibility(View.VISIBLE);
        binding.funTeamSettingGroupManagerActivityJinzhiLingquMingdan.viewTitleArrowTv.setText("禁止领取红包名单");
        binding.funTeamSettingGroupManagerActivityJinzhiLingquMingdan.viewTitleArrowLl.setOnClickListener(this);
        binding.funTeamSettingGroupManagerActivityJinzhiLingquMingdan.viewTitleArrowLl.setBackgroundResource(
                com.yaoxin.appbase.R.drawable.bg_white_rounded_10);

        binding.funTeamSettingGroupManagerActivityDanrenJinyan.viewTitleArrowTv.setText("单人禁言");
        binding.funTeamSettingGroupManagerActivityDanrenJinyan.viewTitleArrowLl.setOnClickListener(this);
        binding.funTeamSettingGroupManagerActivityDanrenJinyan.viewTitleArrowLl.setBackgroundResource(
                com.yaoxin.appbase.R.drawable.bg_white_rounded_10);

        binding.funTeamSettingGroupManagerActivityQunshengji.viewTitleArrowTv.setText("群升级");
        binding.funTeamSettingGroupManagerActivityQunshengji.viewTitleArrowLl.setOnClickListener(this);

        binding.funTeamSettingGroupManagerActivityQunheimingdan.viewTitleArrowTv.setText("群黑名单");
        binding.funTeamSettingGroupManagerActivityQunheimingdan.viewTitleArrowLl.setOnClickListener(this);
        binding.funTeamSettingGroupManagerActivityQunheimingdan.viewTitleArrowLl.setVisibility(View.GONE);

    }

    @Override
    protected void _requestData() {
//        RegisterBean bean = new RegisterBean();
//        bean.groupId = groupId;
        HttpUtil.apiW().group_groupManage(groupId)
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

        //获取群信息
        HttpUtil.apiW().group_groupHomeInfo(groupId).enqueue(new CommonCallback<NetData>() {
            @Override
            public void Successful(Call<NetData> call, Response<NetData> response, NetData body) {
                GroupInfoBean groupInfoBean = new Gson().fromJson(body.data.toString(), GroupInfoBean.class);
                GlideUtil.yh_loadImageRoundedCorner(FunTeamSetting_GroupManagerActivity.this, binding.funTeamSettingNewActivityTeamIcon, groupInfoBean.head, 30);
                binding.tvName.setText(groupInfoBean.name);
                binding.funTeamSettingNewActivityIdTv.setText("共" + groupInfoBean.userInfos.size() + "人");
            }

            @Override
            public void Failure(Call<NetData> call, Throwable t) {
                LoadingDialog.dismissDialog();
            }
        });
    }

    /** 管理员与群主权限一致，仅转让群主、解散群聊、群升级为群主专属 */
    private void applyManagerPermissionUi() {
        boolean isOwner = rankState == 1;
        binding.funTeamSettingGroupManagerActivityQunzhuZhuanrang.viewTitleArrowLl.setVisibility(
                isOwner ? View.VISIBLE : View.GONE);
        binding.funTeamSettingGroupManagerActivityJiesanTv.setVisibility(isOwner ? View.VISIBLE : View.GONE);
        binding.funTeamSettingGroupManagerActivityQunshengji.viewTitleArrowLl.setVisibility(
                isOwner ? View.VISIBLE : View.GONE);
    }

    void updateUI() {
        binding.funTeamSettingGroupManagerActivityYaoqing.viewTitleDetailArrowTemplateSwitch.setSelected(groupInfoBean.inviteState == 0);
        binding.funTeamSettingGroupManagerActivityChengyuanJinyan.viewTitleDetailArrowTemplateSwitch.setSelected(groupInfoBean.shutupState == 0);
        binding.funTeamSettingGroupManagerActivityJinzhiLingquGouwuquan.viewTitleDetailArrowTemplateSwitch.setSelected(groupInfoBean.nonCollectionState == 0);
        binding.funTeamSettingGroupManagerActivityQunchengyuanBaohu.viewTitleDetailArrowTemplateSwitch.setSelected(groupInfoBean.addFriendsState == 0);
    }


    @Override
    public void onClick(View view) {
        if (view == binding.funTeamSettingGroupManagerActivityNav.addCloseImageButton()) {
            finish();
        } else if (view == binding.funTeamSettingGroupManagerActivityYaoqing.viewTitleDetailArrowTemplateSwitch) {
            doOptWithType(0);
        } else if (view == binding.funTeamSettingGroupManagerActivityChengyuanJinyan.viewTitleDetailArrowTemplateSwitch) {
            doOptWithType(1);
        } else if (view == binding.funTeamSettingGroupManagerActivityJinzhiLingquGouwuquan.viewTitleDetailArrowTemplateSwitch) {
            doOptWithType(2);
        } else if (view == binding.funTeamSettingGroupManagerActivityQunchengyuanBaohu.viewTitleDetailArrowTemplateSwitch) {
            doOptWithType(3);
        } else if (view == binding.funTeamSettingGroupManagerActivityJiesanTv) {
            CommonChoiceDialog dialog = new CommonChoiceDialog();
            dialog
                    .setTitleStr("温馨提示")
                    .setContentStr("确定解散群聊吗?")
                    .setNegativeStr("取消")
                    .setPositiveStr("确定")
                    .setConfirmListener(
                            new ChoiceListener() {
                                @Override
                                public void onPositive() {

                                    RegisterBean bean = new RegisterBean();
                                    bean.groupId = groupId;
                                    HttpUtil.apiW().group_dissolveGroup(bean)
                                            .enqueue(new CommonCallback<NetData>() {
                                                @Override
                                                public void Successful(Call<NetData> call, Response<NetData> response, NetData body) {
                                                    ToastUtils.toastMsg(body.msg);
                                                    finish();
                                                }

                                                @Override
                                                public void Failure(Call<NetData> call, Throwable t) {

                                                }
                                            });
                                }

                                @Override
                                public void onNegative() {
                                }
                            })
                    .show(getSupportFragmentManager());
        } else if (view == binding.funTeamSettingGroupManagerActivityQunzhuZhuanrang.viewTitleArrowLl) {


            Intent intent = new Intent(this, FunTeamSettingNew_TeamUsersActivity.class);
            intent.putExtra("groupId", groupId);
            intent.putExtra("opt_type", "1");
            activityResultLauncher.launch(intent);
        } else if (view == binding.funTeamSettingGroupManagerActivityGuanliyuanSet.viewTitleArrowLl) {


            Intent intent = new Intent(this, FunTeamSettingNew_TeamUsersActivity.class);
            intent.putExtra("groupId", groupId);
            intent.putExtra("opt_type", "2");
            activityResultLauncher.launch(intent);

        } else if (view == binding.funTeamSettingGroupManagerActivityJinzhiLingquMingdan.viewTitleArrowLl) {
            HashMap map = new HashMap();
            map.put("groupId", groupId);
            FunTeamSettingNew_ForbiddenListActivity.start(FunTeamSettingNew_ForbiddenListActivity.class, this, map);
        } else if (view == binding.funTeamSettingGroupManagerActivityDanrenJinyan.viewTitleArrowLl) {
            HashMap map = new HashMap();
            map.put("groupId", groupId);
            FunTeamSettingNew_MuteListActivity.start(FunTeamSettingNew_MuteListActivity.class, this, map);
        } else if (view == binding.funTeamSettingGroupManagerActivityQunshengji.viewTitleArrowLl) {
            XKitRouter.withKey("BuyGroupFeatureActivity")
                    .withParam("type", 0)
                    .withParam("groupId", groupId)
                    .withContext(this)
                    .navigate();
        } else if (view == binding.funTeamSettingGroupManagerActivityQunheimingdan.viewTitleArrowLl) {

            XKitRouter.withKey(RouterConstant.PATH_FUN_MY_BLACK_PAGE)
                    .withParam("groupId", groupId)
                    .withContext(this)
                    .navigate();
        }
    }

    @Override
    protected void callBackResult(Intent data) {
        super.callBackResult(data);

        ArrayList<String> userIds = data.getStringArrayListExtra("userIds");
        String opt_type = data.getStringExtra("opt_type");
        RegisterBean bean = new RegisterBean();
        bean.groupId = groupId;

        if ("1".equals(opt_type)) {
            bean.newGroupUserId = userIds.get(0);
            HttpUtil.apiW().group_transferGroup(bean)
                    .enqueue(new CommonCallback<NetData>() {
                        @Override
                        public void Successful(Call<NetData> call, Response<NetData> response, NetData body) {

                            ToastUtils.toastMsg(body.msg);
                            EventBus.getDefault().post(new BaseEvent("reloadTeamSettingData"));
                        }

                        @Override
                        public void Failure(Call<NetData> call, Throwable t) {

                        }
                    });
        } else if ("2".equals(opt_type)) {
            ArrayList<String> unuserIds = data.getStringArrayListExtra("un_userIds");
            if (userIds.size() > 0) {
                bean.members = userIds;
                HttpUtil.apiW().group_installAdmin(bean)
                        .enqueue(new CommonCallback<NetData>() {
                            @Override
                            public void Successful(Call<NetData> call, Response<NetData> response, NetData body) {

                                ToastUtils.toastMsg(body.msg);
                                EventBus.getDefault().post(new BaseEvent("reloadTeamSettingData"));
                            }

                            @Override
                            public void Failure(Call<NetData> call, Throwable t) {

                            }
                        });
            }
            if (unuserIds.size() > 0) {

                bean.members = unuserIds;
                bean.state = 1;
                HttpUtil.apiW().group_installAdmin(bean)
                        .enqueue(new CommonCallback<NetData>() {
                            @Override
                            public void Successful(Call<NetData> call, Response<NetData> response, NetData body) {

                                ToastUtils.toastMsg(body.msg);
                                EventBus.getDefault().post(new BaseEvent("reloadTeamSettingData"));
                            }

                            @Override
                            public void Failure(Call<NetData> call, Throwable t) {

                            }
                        });
            }

        }

    }


    void doOptWithType(int type) {

        RegisterBean bean = new RegisterBean();
        bean.groupId = groupId;
        if (type == 0) {
            bean.inviteState = binding.funTeamSettingGroupManagerActivityYaoqing.viewTitleDetailArrowTemplateSwitch.isSelected() ? "1" : "0";
        }
        if (type == 1) {
            bean.shutupState = binding.funTeamSettingGroupManagerActivityChengyuanJinyan.viewTitleDetailArrowTemplateSwitch.isSelected() ? "1" : "0";
        }
        if (type == 2) {
            bean.nonCollectionState = binding.funTeamSettingGroupManagerActivityJinzhiLingquGouwuquan.viewTitleDetailArrowTemplateSwitch.isSelected() ? "1" : "0";
        }
        if (type == 3) {
            bean.addFriendsState = binding.funTeamSettingGroupManagerActivityQunchengyuanBaohu.viewTitleDetailArrowTemplateSwitch.isSelected() ? "1" : "0";
        }

        HttpUtil.apiW().groupMember_invitationGroupConfirmed(bean)
                .enqueue(new CommonCallback<NetData>() {
                    @Override
                    public void Successful(Call<NetData> call, Response<NetData> response, NetData body) {

                        ToastUtils.toastMsg(body.msg);
                        if (type == 0) {
                            binding.funTeamSettingGroupManagerActivityYaoqing.viewTitleDetailArrowTemplateSwitch.setSelected(!binding.funTeamSettingGroupManagerActivityYaoqing.viewTitleDetailArrowTemplateSwitch.isSelected());
                        }
                        if (type == 1) {
                            binding.funTeamSettingGroupManagerActivityChengyuanJinyan.viewTitleDetailArrowTemplateSwitch.setSelected(!binding.funTeamSettingGroupManagerActivityChengyuanJinyan.viewTitleDetailArrowTemplateSwitch.isSelected());
                        }
                        if (type == 2) {
                            binding.funTeamSettingGroupManagerActivityJinzhiLingquGouwuquan.viewTitleDetailArrowTemplateSwitch.setSelected(!binding.funTeamSettingGroupManagerActivityJinzhiLingquGouwuquan.viewTitleDetailArrowTemplateSwitch.isSelected());
                        }
                        if (type == 3) {
                            binding.funTeamSettingGroupManagerActivityQunchengyuanBaohu.viewTitleDetailArrowTemplateSwitch.setSelected(!binding.funTeamSettingGroupManagerActivityQunchengyuanBaohu.viewTitleDetailArrowTemplateSwitch.isSelected());
                        }
                    }

                    @Override
                    public void Failure(Call<NetData> call, Throwable t) {

                    }
                });
    }

}
