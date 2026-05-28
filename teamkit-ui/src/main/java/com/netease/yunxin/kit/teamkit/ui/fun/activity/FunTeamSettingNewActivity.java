// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.teamkit.ui.fun.activity;

import static com.netease.yunxin.kit.corekit.im.utils.RouterConstant.KEY_TEAM_ID;
import static com.netease.yunxin.kit.corekit.im.utils.RouterConstant.REQUEST_CONTACT_SELECTOR_KEY;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.chad.library.adapter4.BaseQuickAdapter;
import com.google.android.material.tabs.TabLayout;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.netease.nimlib.sdk.NIMClient;
import com.netease.nimlib.sdk.RequestCallback;
import com.netease.nimlib.sdk.msg.MsgService;
import com.netease.nimlib.sdk.msg.constant.SessionTypeEnum;
import com.netease.nimlib.sdk.msg.model.StickTopSessionInfo;
import com.netease.nimlib.sdk.team.TeamService;
import com.netease.nimlib.sdk.team.constant.TeamMessageNotifyTypeEnum;
import com.netease.yunxin.kit.chatkit.model.TeamWithCurrentMember;
import com.netease.yunxin.kit.chatkit.model.UserInfoWithTeam;
import com.netease.yunxin.kit.chatkit.repo.ConversationRepo;
import com.netease.yunxin.kit.chatkit.repo.TeamRepo;
import com.netease.yunxin.kit.common.ui.dialog.ChoiceListener;
import com.netease.yunxin.kit.common.ui.dialog.CommonChoiceDialog;
import com.netease.yunxin.kit.common.utils.SPUtils;
import com.netease.yunxin.kit.common.utils.SizeUtils;
import com.netease.nimlib.sdk.team.model.Team;
import com.netease.yunxin.kit.corekit.im.IMKitClient;
import com.netease.yunxin.kit.corekit.im.provider.FetchCallback;
import com.netease.yunxin.kit.corekit.im.utils.RouterConstant;
import com.netease.yunxin.kit.corekit.route.XKitRouter;
import com.netease.yunxin.kit.teamkit.ui.databinding.FunTeamSettingNewActivityBinding;
import com.netease.yunxin.kit.teamkit.ui.fun.activity.adapter.TeamSettingUserInfoAdapter;
import com.netease.yunxin.kit.teamkit.ui.fun.activity.adapter.TeamSettingUserInfoNewAdapter;
import com.netease.yunxin.kit.teamkit.ui.fun.dialog.TeamModifyDialog;
import com.yaoxin.appbase.activity.BaseActivity;
import com.yaoxin.appbase.dialog.GroupEditActionDialog;
import com.yaoxin.appbase.model.GroupInfoBean;
import com.yaoxin.appbase.model.NetData;
import com.yaoxin.appbase.model.RegisterBean;
import com.yaoxin.appbase.model.UserBean;
import com.yaoxin.appbase.net.CommonCallback;
import com.yaoxin.appbase.net.Constant;
import com.yaoxin.appbase.net.HttpUtil;
import com.yaoxin.appbase.utils.BarUtils;
import com.yaoxin.appbase.utils.BaseEvent;
import com.yaoxin.appbase.utils.CommonCallBack;
import com.yaoxin.appbase.utils.DataUtil;
import com.yaoxin.appbase.utils.DialogAlertUtil;
import com.yaoxin.appbase.utils.GlideUtil;
import com.yaoxin.appbase.utils.StatusBarUtils;
import com.yaoxin.appbase.utils.ToastUtils;
import com.yaoxin.appbase.utils.UploadUtil;
import com.yaoxin.appbase.view.CommonGridSpacingItemDecoration;
import com.yaoxin.appbase.view.LoadingDialog;
import com.zhihu.matisse.Matisse;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import retrofit2.Call;
import retrofit2.Response;

/**
 * team setting activity
 */
public class FunTeamSettingNewActivity extends BaseActivity implements View.OnClickListener {

    private static final int REQUEST_CODE_CHOOSE = 23;
    FunTeamSettingNewActivityBinding binding;
    String groupId;
    GroupInfoBean groupInfoBean = new GroupInfoBean();
    TeamSettingUserInfoAdapter adapter;// = new TeamSettingUserInfoAdapter(true, new ArrayList<>());
    private TeamSettingUserInfoNewAdapter newAdapter;


    protected ActivityResultLauncher<Intent> launcher;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        Intent intent = getIntent();
        groupId = intent.getStringExtra(KEY_TEAM_ID);

        super.onCreate(savedInstanceState);
        binding = FunTeamSettingNewActivityBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.funTeamSettingNewActivityNav.enableUnderDivider(false);
        StatusBarUtils.setStatusBarLightMode(this, true, true);
        LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) binding.funTeamSettingNewActivityNav.getLayoutParams();
        params.height = params.height + BarUtils.getStatusBarHeight();
        binding.funTeamSettingNewActivityNav.setLayoutParams(params);
        binding.funTeamSettingNewActivityNav.setPadding(0, BarUtils.getStatusBarHeight(), 0, 0);
        _initView();

        EventBus.getDefault().register(this);
        launcher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            if (result.getResultCode() != RESULT_OK || result.getData() == null) {
                return;
            }
            Intent intent1 = result.getData();
            ArrayList<String> memberList = intent1.getStringArrayListExtra(REQUEST_CONTACT_SELECTOR_KEY);
            String opt_type = intent1.getStringExtra("opt_type");
            if ("1".equals(opt_type)) {
                RegisterBean bean = new RegisterBean();
                bean.groupId = groupId;
                bean.members = memberList;
                HttpUtil.apiW().group_pullPeopleGroup(bean).enqueue(new CommonCallback<NetData>() {
                    @Override
                    public void Successful(Call<NetData> call, Response<NetData> response, NetData body) {
                        ToastUtils.toastMsg(body.msg);
                        _requestData();
                    }

                    @Override
                    public void Failure(Call<NetData> call, Throwable t) {

                    }
                });
            }
        });

        binding.tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                // 当选项卡被选中时
                int position = tab.getPosition();
                switch (position) {
                    case 0:
                        binding.rvUserInfo.setVisibility(View.VISIBLE);
                        binding.layouSettingBox.setVisibility(View.GONE);
                        break;
                    case 1:
                        binding.rvUserInfo.setVisibility(View.GONE);
                        binding.layouSettingBox.setVisibility(View.VISIBLE);
                        break;
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });

    }

    @Override
    protected void _initView() {
        binding.rvUserInfo.setLayoutManager(new LinearLayoutManager(this));
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        binding.funTeamSettingNewActivityMemberRv.setLayoutManager(layoutManager);
        binding.editIcon.setOnClickListener(this);
        binding.funTeamSettingNewActivityTeamIcon.setOnClickListener(this);
        binding.funTeamSettingNewActivitySeeAllMemberLl.setOnClickListener(this);
        //退出群聊 我修改的
        binding.funTeamSettingNewActivityQuite.setVisibility(View.GONE);
        binding.funTeamSettingNewActivityQuite.setOnClickListener(this);
        binding.funTeamSettingNewActivityNav.addCloseImageButton().setOnClickListener(this);

        binding.funTeamSettingNewActivitySearchHistory.viewTitleArrowTv.setText("查找聊天记录");
        binding.funTeamSettingNewActivitySearchHistory.viewTitleArrowLl.setOnClickListener(this);

        binding.funTeamSettingNewActivityNicheng.viewTitleArrowTv.setText("我在本群昵称");
        binding.funTeamSettingNewActivityNicheng.viewTitleArrowRightTv.setVisibility(View.VISIBLE);
        binding.funTeamSettingNewActivityNicheng.viewTitleArrowLl.setOnClickListener(this);

        binding.funTeamSettingNewActivitySetGonggao.viewTitleArrowTv.setText("设置群公告");
        binding.funTeamSettingNewActivitySetGonggao.viewTitleArrowLl.setOnClickListener(this);
        binding.funTeamSettingNewActivityManagerTeam.viewTitleArrowTv.setText("群管理");
        binding.funTeamSettingNewActivityManagerTeam.viewTitleArrowLl.setOnClickListener(this);
        binding.funTeamSettingNewActivityManagerTeam.viewTitleArrowLl.setVisibility(View.GONE);

        binding.funTeamSettingNewActivityTeamSetting.viewTitleArrowTv.setText("群设置");
        binding.funTeamSettingNewActivityTeamSetting.viewTitleArrowLl.setOnClickListener(this);

        binding.funTeamSettingNewActivityTeamUpgrade.viewTitleArrowTv.setText("群升级");
        binding.funTeamSettingNewActivityTeamUpgrade.viewTitleArrowLl.setOnClickListener(this);
        binding.funTeamSettingNewActivityTeamUpgrade.viewTitleArrowLl.setVisibility(View.GONE);

//        binding.funTeamSettingNewActivityUpgradeTeam.viewTitleArrowTv.setText("群升级");
//        binding.funTeamSettingNewActivityUpgradeTeam.viewTitleArrowLl.setOnClickListener(this);

        binding.funTeamSettingNewActivityZhiding.viewTitleArrowTv.setText("置顶聊天");
        binding.funTeamSettingNewActivityZhiding.viewTitleArrowArrowIv.setVisibility(View.GONE);
        binding.funTeamSettingNewActivityZhiding.viewTitleArrowRightTvSwitch.setVisibility(View.VISIBLE);
        binding.funTeamSettingNewActivityZhiding.viewTitleArrowRightTvSwitch.setOnClickListener(this);

        binding.funTeamSettingNewActivityMiandarao.viewTitleArrowTv.setText("消息免打扰");
        binding.funTeamSettingNewActivityMiandarao.viewTitleArrowArrowIv.setVisibility(View.GONE);
        binding.funTeamSettingNewActivityMiandarao.viewTitleArrowRightTvSwitch.setVisibility(View.VISIBLE);
        binding.funTeamSettingNewActivityMiandarao.viewTitleArrowRightTvSwitch.setOnClickListener(this);

        binding.funTeamSettingNewActivityVlqHb.viewTitleArrowTv.setText("未领取红包专区");
        binding.funTeamSettingNewActivityVlqHb.viewTitleArrowLl.setOnClickListener(this);

        binding.funTeamSettingNewActivityTuichu.viewTitleArrowTv.setText("删除并退出");
        binding.funTeamSettingNewActivityTuichu.viewTitleArrowTv.setTextColor(Color.parseColor("#E23D47"));
        binding.funTeamSettingNewActivityTuichu.viewTitleArrowLl.setOnClickListener(this);

        //清空聊天信息 我修改的
        binding.tvClearMessage.setVisibility(View.GONE);
        binding.tvClearMessage.setOnClickListener(this);
        binding.funTeamSettingNewActivityDelteRecord.viewTitleArrowTv.setText("清除聊天记录");
        binding.funTeamSettingNewActivityDelteRecord.viewTitleArrowLl.setOnClickListener(this);
//        binding.funTeamSettingNewActivityTousu.viewTitleArrowTv.setText("投诉");
//        binding.funTeamSettingNewActivityTousu.viewTitleArrowLl.setOnClickListener(this);

        binding.editIcon.setVisibility(View.GONE);

//        binding.funTeamSettingNewActivityZhiding.viewTitleArrowTemplateLeftIv.setVisibility(View.VISIBLE);
//        binding.funTeamSettingNewActivityZhiding.viewTitleArrowTemplateLeftIv.setImageResource(R.drawable.team_setting_cell_zd);
//
//        binding.funTeamSettingNewActivityMiandarao.viewTitleArrowTemplateLeftIv.setVisibility(View.VISIBLE);
//        binding.funTeamSettingNewActivityMiandarao.viewTitleArrowTemplateLeftIv.setImageResource(R.drawable.team_setting_miandarao);
//
//        binding.funTeamSettingNewActivityNicheng.viewTitleArrowTemplateLeftIv.setVisibility(View.VISIBLE);
//        binding.funTeamSettingNewActivityNicheng.viewTitleArrowTemplateLeftIv.setImageResource(R.drawable.team_setting_cell_edit_name);
//
//        binding.funTeamSettingNewActivitySetGonggao.viewTitleArrowTemplateLeftIv.setVisibility(View.VISIBLE);
//        binding.funTeamSettingNewActivitySetGonggao.viewTitleArrowTemplateLeftIv.setImageResource(R.drawable.team_setting_cell_edit_name);
//
//        binding.funTeamSettingNewActivityManagerTeam.viewTitleArrowTemplateLeftIv.setVisibility(View.VISIBLE);
//        binding.funTeamSettingNewActivityManagerTeam.viewTitleArrowTemplateLeftIv.setImageResource(R.drawable.team_setting_cell_qgl);
//
//        binding.funTeamSettingNewActivityDelteRecord.viewTitleArrowTemplateLeftIv.setVisibility(View.VISIBLE);
//        binding.funTeamSettingNewActivityDelteRecord.viewTitleArrowTemplateLeftIv.setImageResource(R.drawable.team_setting_cell_ql);

        binding.layoutQunGuanli.setOnClickListener(view -> {
            HashMap map = new HashMap();
            map.put("groupId", groupId);
            map.put("rankState", groupInfoBean.rankState + "");
            FunTeamSetting_GroupManagerActivity.start(FunTeamSetting_GroupManagerActivity.class, this, map);
        });

        binding.layoutYaoqing.setOnClickListener(view -> {
            DataUtil.setStringValue(new Gson().toJson(groupInfoBean), "groupInfo");
            XKitRouter.withKey(Constant.FunSelected_User_ActivityKey).withParam("type", "2").withParam("groupId", groupId).withContext(FunTeamSettingNewActivity.this).navigate();
        });

        binding.layoutTtuichu.setOnClickListener(view -> {
            DataUtil.setStringValue(new Gson().toJson(groupInfoBean), "groupInfo");
            XKitRouter.withKey(Constant.FunSelected_User_ActivityKey).withParam("type", "3").withParam("groupId", groupId).withContext(FunTeamSettingNewActivity.this).navigate();
        });
        binding.layoutQunMore.setOnClickListener(this);
    }

    void _requestPeople(int page) {
        RegisterBean bean = new RegisterBean();
        bean.groupId = groupId;
        bean.page = page + "";
        bean.pageNo = "100";

//        LoadingDialog.showDialog(getSupportFragmentManager(),"加载中");
        HttpUtil.apiW().group_groupUserListPost(bean).enqueue(new CommonCallback<NetData>() {
            @Override
            public void Successful(Call<NetData> call, Response<NetData> response, NetData body) {

                Type type = new TypeToken<List<GroupInfoBean>>() {
                }.getType();
                List<GroupInfoBean> tempList = new Gson().fromJson(body.data.toString(), type);
                if (!tempList.isEmpty()) {
                    groupInfoBean.userInfos.addAll(tempList);
                    if (tempList.size() == 100) {
                        _requestPeople((page + 1));
                    } else {
                        LoadingDialog.dismissDialog();
                        requestYunXin();
                        updateUI();
                    }
                } else {
                    LoadingDialog.dismissDialog();
                    requestYunXin();
                    updateUI();

                }
            }

            @Override
            public void Failure(Call<NetData> call, Throwable t) {
                LoadingDialog.dismissDialog();
            }

            @Override
            public void end() {
                super.end();

            }
        });
    }

    @Override
    protected void _requestData() {
        SPUtils.getInstance().put("reloadTeamSettingData", false);
        LoadingDialog.showDialog(getSupportFragmentManager(), "加载中");
        HttpUtil.apiW().group_groupHomeInfo(groupId).enqueue(new CommonCallback<NetData>() {
            @Override
            public void Successful(Call<NetData> call, Response<NetData> response, NetData body) {

                groupInfoBean = new Gson().fromJson(body.data.toString(), GroupInfoBean.class);
                groupInfoBean.userInfos.clear();
                _requestPeople(1);

            }

            @Override
            public void Failure(Call<NetData> call, Throwable t) {
                LoadingDialog.dismissDialog();
            }

//                    @Override
//                    public void end() {
//                        super.end();
//
//                    }
        });
    }

    private void requestYunXin() {
        TeamRepo.queryTeamWithMember(groupId, Objects.requireNonNull(IMKitClient.account()), new FetchCallback<TeamWithCurrentMember>() {
            @Override
            public void onSuccess(@Nullable TeamWithCurrentMember param) {

                binding.funTeamSettingNewActivityZhiding.viewTitleArrowRightTvSwitch.setSelected(param.isStickTop());
                binding.funTeamSettingNewActivityMiandarao.viewTitleArrowRightTvSwitch.setSelected(param.getTeam().getMessageNotifyType() == TeamMessageNotifyTypeEnum.Mute);
            }

            @Override
            public void onFailed(int code) {
            }

            @Override
            public void onException(@Nullable Throwable exception) {
            }
        });
    }

    void updateUI() {
        GlideUtil.yh_loadImageRoundedCorner(this, binding.funTeamSettingNewActivityTeamIcon, groupInfoBean.head, 30);
        binding.tvName.setText(groupInfoBean.name);
        binding.tvTeamNumber.setText("共" + groupInfoBean.userInfos.size() + "人");
        ArrayList<GroupInfoBean> maxList = new ArrayList<>();
        if (groupInfoBean.userInfos.size() > 3) {
            for (int i = 0; i < 3; i++) {
                maxList.add(groupInfoBean.userInfos.get(i));
            }
        } else {
            maxList.addAll(groupInfoBean.userInfos);
        }
//        binding.funTeamSettingNewActivityIdTv.setText("ID: " + groupInfoBean.groupId);
        binding.funTeamSettingNewActivityIdTv.setText(groupInfoBean.userInfos.size() + "人成员");
        //跳转详情


        syncYunXinGradeToLocal(groupId);


        adapter =
                new TeamSettingUserInfoAdapter(
                        groupInfoBean.rankState == 1 || groupInfoBean.rankState == 2, maxList);
        CommonGridSpacingItemDecoration gridSpacingItemDecoration = new CommonGridSpacingItemDecoration(5, SizeUtils.dp2px(16), false);
        binding.funTeamSettingNewActivityMemberRv.addItemDecoration(gridSpacingItemDecoration);
        binding.funTeamSettingNewActivityMemberRv.setAdapter(adapter);
        Context that = this;
        adapter.setOnItemClickListener(new BaseQuickAdapter.OnItemClickListener<GroupInfoBean>() {
            @Override
            public void onClick(@NonNull BaseQuickAdapter<GroupInfoBean, ?> baseQuickAdapter, @NonNull View view, int i) {
//                if (maxList.size() > 3 || i > 4) {
//                    return;
//                }
                if (i == maxList.size()) {
                    DataUtil.setStringValue(new Gson().toJson(groupInfoBean), "groupInfo");
                    XKitRouter.withKey(Constant.FunSelected_User_ActivityKey).withParam("type", "2").withParam("groupId", groupId).withContext(that).navigate();
                } else if (i == maxList.size() + 1) {
                    DataUtil.setStringValue(new Gson().toJson(groupInfoBean), "groupInfo");
                    XKitRouter.withKey(Constant.FunSelected_User_ActivityKey).withParam("type", "3").withParam("groupId", groupId).withContext(that).navigate();
                } else {
                    XKitRouter.withKey(Constant.FunTeamUserInfoDetailActivityKey).withParam("groupId", groupId).withParam("userId", maxList.get(i).userId).withContext(view.getContext()).navigate();
                }
            }
        });
        binding.funTeamSettingNewActivityGonggaoTv.setText(groupInfoBean.announcement == null ? "暂无公告" : groupInfoBean.announcement);
//        binding.funTeamSettingNewActivityMiandarao.viewTitleArrowRightTvSwitch.setSelected(groupInfoBean.noDisturbingState == 1);
//        binding.funTeamSettingNewActivityZhiding.viewTitleArrowRightTvSwitch.setSelected(groupInfoBean.topState == 1);
        binding.funTeamSettingNewActivityNicheng.viewTitleArrowRightTv.setText(groupInfoBean.getSelfRemarkName());

        binding.layoutYaoqing.setVisibility(View.VISIBLE);
        binding.layoutTtuichu.setVisibility(View.GONE);
        binding.layoutQunGuanli.setVisibility(View.GONE);
        binding.layoutQunMore.setVisibility(View.GONE);
        if (groupInfoBean.rankState == 1 || groupInfoBean.rankState == 2) {
            binding.layoutYaoqing.setVisibility(View.VISIBLE);
            binding.layoutTtuichu.setVisibility(View.VISIBLE);
        }
//        binding.funTeamSettingNewActivityUpgradeTeam.viewTitleArrowLl.setVisibility(View.GONE);
        if (groupInfoBean.rankState == 1 || groupInfoBean.rankState == 2) {
            binding.layoutQunGuanli.setVisibility(View.VISIBLE);
            //我修改的
            binding.funTeamSettingNewActivityManagerTeam.viewTitleArrowLl.setVisibility(View.GONE);
            binding.funTeamSettingNewActivityManagerLl.setVisibility(View.VISIBLE);
//            binding.funTeamSettingNewActivityUpgradeTeam.viewTitleArrowLl.setVisibility(View.VISIBLE);
//            binding.funTeamSettingNewActivitySetGonggao.viewTitleArrowLl.setVisibility(View.VISIBLE);
            //我修改的
            binding.editIcon.setVisibility(View.GONE);
            binding.layoutQunMore.setVisibility(View.VISIBLE);
        }
    }

    ArrayList<String> getTeamUserIds() {
        ArrayList<String> list = new ArrayList();
        for (GroupInfoBean temp : groupInfoBean.userInfos) {
            list.add(temp.userId);
        }
        return list;
    }

    @Override
    public void onClick(View view) {
        if (view == binding.funTeamSettingNewActivityNav.addCloseImageButton()) {
//            if (BuildConfig.DEBUG) {
//                TeamMaxMemberDialogFragment.showV(getSupportFragmentManager(), new TeamMaxMemberDialogFragment.TeamMaxMemberDialogFragmentBlock() {
//                    @Override
//                    public void upGrade() {
//
//                    }
//                });
//            } else {
            finish();
//            }
        } else if (view == binding.funTeamSettingNewActivityMiandarao.viewTitleArrowRightTvSwitch) {
            boolean isOpen = binding.funTeamSettingNewActivityMiandarao.viewTitleArrowRightTvSwitch.isSelected();
            // 以设置 “仅管理员消息提醒” 为例
            TeamMessageNotifyTypeEnum type = TeamMessageNotifyTypeEnum.All;
            if (!isOpen) {
                type = TeamMessageNotifyTypeEnum.Mute;
            }
            NIMClient.getService(TeamService.class).muteTeam(groupId, type).setCallback(new RequestCallback<Void>() {
                @Override
                public void onSuccess(Void param) {
                    // 设置成功
                    binding.funTeamSettingNewActivityMiandarao.viewTitleArrowRightTvSwitch.setSelected(!binding.funTeamSettingNewActivityMiandarao.viewTitleArrowRightTvSwitch.isSelected());
                }

                @Override
                public void onFailed(int code) {
                    // 设置失败
                }

                @Override
                public void onException(Throwable exception) {
                    // 错误
                }
            });

//            ConversationRepo.setNotify(
//                    groupId,
//                    SessionTypeEnum.Team,
//                    false,
//                    new FetchCallback<Void>() {
//                        @Override
//                        public void onSuccess(@Nullable Void param) {
//                            binding.funTeamSettingNewActivityMiandarao.viewTitleArrowRightTvSwitch.setSelected(!binding.funTeamSettingNewActivityMiandarao.viewTitleArrowRightTvSwitch.isSelected());
//                        }
//
//                        @Override
//                        public void onFailed(int code) {
//                        }
//
//                        @Override
//                        public void onException(@Nullable Throwable exception) {
//                        }
//                    });
        } else if (view == binding.funTeamSettingNewActivityZhiding.viewTitleArrowRightTvSwitch) {
            configStick(groupId, !binding.funTeamSettingNewActivityZhiding.viewTitleArrowRightTvSwitch.isSelected());
        } else if (view == binding.funTeamSettingNewActivityDelteRecord.viewTitleArrowLl || view == binding.tvClearMessage) {
//            configStick(groupId,!binding.funTeamSettingNewActivityZhiding.viewTitleArrowRightTvSwitch.isSelected());
            DialogAlertUtil.showAlert("确认删除聊天记录吗？", new DialogAlertUtil.DialogAlertUtilCallBack() {
                @Override
                public void clickType(int type) {
                    if (type == 1) {
                        NIMClient.getService(MsgService.class).clearChattingHistory(groupId, SessionTypeEnum.Team);
                        NIMClient.getService(MsgService.class).clearServerHistory(groupId, SessionTypeEnum.Team);
                        EventBus.getDefault().post(new BaseEvent("clearTeamMessageList"));
                    }
                }
            }, getSupportFragmentManager());
        } else if (view == binding.funTeamSettingNewActivityQuite || view == binding.funTeamSettingNewActivityTuichu.viewTitleArrowLl) {
            CommonChoiceDialog dialog = new CommonChoiceDialog();
            dialog.setTitleStr("温馨提示").setContentStr("确定退出群聊吗?").setNegativeStr("取消").setPositiveStr("确定").setConfirmListener(new ChoiceListener() {
                @Override
                public void onPositive() {
                    RegisterBean bean = new RegisterBean();
                    bean.groupId = groupId;
                    bean.userId = DataUtil.getUserid();
                    HttpUtil.apiW().group_quitGroup(bean).enqueue(new CommonCallback<NetData>() {
                        @Override
                        public void Successful(Call<NetData> call, Response<NetData> response, NetData body) {
                            NIMClient.getService(MsgService.class).clearChattingHistory(groupId, SessionTypeEnum.Team);
                            NIMClient.getService(MsgService.class).clearServerHistory(groupId, SessionTypeEnum.Team);
                            EventBus.getDefault().post(new BaseEvent("clearTeamMessageList"));
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
            }).show(getSupportFragmentManager());

        } else if (view == binding.layoutQunMore || view == binding.editIcon || view == binding.funTeamSettingNewActivityTeamSetting.viewTitleArrowLl) {
            GroupEditActionDialog dialog = new GroupEditActionDialog(this, new GroupEditActionDialog.OnActionListener() {
                @Override
                public void onEditName() {
                    //修改群名
                    TeamModifyDialog.showV(getSupportFragmentManager(), 1, new TeamModifyDialog.TeamModifyDialogBlock() {
                        @Override
                        public void returnResult(String result) {
                            RegisterBean bean = new RegisterBean();
                            bean.groupId = groupId;
                            bean.groupName = result;
                            HttpUtil.apiW().group_updateGroupInfo(bean).enqueue(new CommonCallback<NetData>() {
                                @Override
                                public void Successful(Call<NetData> call, Response<NetData> response, NetData body) {

                                    ToastUtils.toastMsg(body.msg);
                                    _requestData();
                                }

                                @Override
                                public void Failure(Call<NetData> call, Throwable t) {

                                }
                            });
                        }
                    }, groupInfoBean.name);
                }

                @Override
                public void onEditNotice() {
                    // 处理“修改群公告”点击
                    TeamModifyDialog.showV(getSupportFragmentManager(), 2, new TeamModifyDialog.TeamModifyDialogBlock() {
                        @Override
                        public void returnResult(String result) {
                            RegisterBean bean = new RegisterBean();
                            bean.groupId = groupId;
                            bean.announcement = result;
                            HttpUtil.apiW().group_updateGroupInfo(bean).enqueue(new CommonCallback<NetData>() {
                                @Override
                                public void Successful(Call<NetData> call, Response<NetData> response, NetData body) {

                                    ToastUtils.toastMsg(body.msg);
                                    _requestData();
                                    EventBus.getDefault().post(new BaseEvent("reload_gonggao"));
                                }

                                @Override
                                public void Failure(Call<NetData> call, Throwable t) {

                                }
                            });
                        }
                    }, groupInfoBean.announcement);
                }

                @Override
                public void onEditAvatar() {
                    // 处理“修改群头像”点击
                    UploadUtil.openPhotoLibrary(FunTeamSettingNewActivity.this, Constant.REQUEST_CODE_CHOOSE);
                }
            });
            dialog.show();

//            Activity that = this;
//            DialogAlertUtil.showSheetView(this, getSupportFragmentManager(), new String[]{"修改群名称", "修改公告栏", "修改群头像"}, new DialogAlertUtil.DialogAlertUtilCallBack() {
//                @Override
//                public void clickType(int type) {
//                    if (type == 1) {
//                        //修改群名
//                        TeamModifyDialog.showV(getSupportFragmentManager(), type, new TeamModifyDialog.TeamModifyDialogBlock() {
//                            @Override
//                            public void returnResult(String result) {
//                                RegisterBean bean = new RegisterBean();
//                                bean.groupId = groupId;
//                                bean.groupName = result;
//                                HttpUtil.apiW().group_updateGroupInfo(bean).enqueue(new CommonCallback<NetData>() {
//                                    @Override
//                                    public void Successful(Call<NetData> call, Response<NetData> response, NetData body) {
//
//                                        ToastUtils.toastMsg(body.msg);
//                                        _requestData();
//                                    }
//
//                                    @Override
//                                    public void Failure(Call<NetData> call, Throwable t) {
//
//                                    }
//                                });
//                            }
//                        }, groupInfoBean.name);
//                    }
//                    if (type == 2) {
//                        //修改公告栏
//                        TeamModifyDialog.showV(getSupportFragmentManager(), type, new TeamModifyDialog.TeamModifyDialogBlock() {
//                            @Override
//                            public void returnResult(String result) {
//                                RegisterBean bean = new RegisterBean();
//                                bean.groupId = groupId;
//                                bean.announcement = result;
//                                HttpUtil.apiW().group_updateGroupInfo(bean).enqueue(new CommonCallback<NetData>() {
//                                    @Override
//                                    public void Successful(Call<NetData> call, Response<NetData> response, NetData body) {
//
//                                        ToastUtils.toastMsg(body.msg);
//                                        _requestData();
//                                        EventBus.getDefault().post(new BaseEvent("reload_gonggao"));
//                                    }
//
//                                    @Override
//                                    public void Failure(Call<NetData> call, Throwable t) {
//
//                                    }
//                                });
//                            }
//                        }, groupInfoBean.announcement);
//                    }
//                    if (type == 3) {
//                        UploadUtil.openPhotoLibrary(that, Constant.REQUEST_CODE_CHOOSE);
//                    }
//                }
//            });
//            Intent intent = new Intent(this, ModifyInfoActivity.class);
//            intent.putExtra("title","修改群名");
//            intent.putExtra("type","1");
//            activityResultLauncher.launch(intent);
        } else if (view == binding.funTeamSettingNewActivityNicheng.viewTitleArrowLl) {

            TeamModifyDialog.showV(getSupportFragmentManager(), 3, new TeamModifyDialog.TeamModifyDialogBlock() {
                @Override
                public void returnResult(String result) {
                    RegisterBean bean = new RegisterBean();
                    bean.groupId = groupId;
                    bean.nickName = result;
                    HttpUtil.apiW().groupMember_installGroupNickName(bean).enqueue(new CommonCallback<NetData>() {
                        @Override
                        public void Successful(Call<NetData> call, Response<NetData> response, NetData body) {

                            ToastUtils.toastMsg(body.msg);
                            _requestData();
                        }

                        @Override
                        public void Failure(Call<NetData> call, Throwable t) {

                        }
                    });
                }
            }, groupInfoBean.getSelfRemarkName());
        } else if (view == binding.funTeamSettingNewActivitySetGonggao.viewTitleArrowLl) {

            Intent intent = new Intent(this, ModifyInfoActivity.class);
            intent.putExtra("title", "修改群公告");
            intent.putExtra("type", "3");
            activityResultLauncher.launch(intent);
        } else if (view == binding.funTeamSettingNewActivityManagerTeam.viewTitleArrowLl) {
            HashMap map = new HashMap();
            map.put("groupId", groupId);
            map.put("rankState", groupInfoBean.rankState + "");
            FunTeamSetting_GroupManagerActivity.start(FunTeamSetting_GroupManagerActivity.class, this, map);
        } else if (view == binding.funTeamSettingNewActivitySeeAllMemberLl) {
            HashMap map = new HashMap();
            map.put("groupId", groupId);
            FunTeamSettingNew_TeamUsersActivity.start(FunTeamSettingNew_TeamUsersActivity.class, this, map);

        } else if (view == binding.funTeamSettingNewActivityTeamIcon) {
//            UploadUtil.openPhotoLibrary(this, Constant.REQUEST_CODE_CHOOSE);
//            Matisse.from(FunTeamSettingNewActivity.this)
//                    .choose(MimeType.ofImage(), false)
//                    .countable(true)
//                    .capture(true)
//                    .captureStrategy(
//                            new CaptureStrategy(true, "com.zhihu.matisse.sample.fileprovider", "test"))
//                    .maxSelectable(1)
//                    .addFilter(new GifSizeFilter(320, 320, 5 * Filter.K * Filter.K))
//                    .gridExpectedSize(
//                            getResources().getDimensionPixelSize(com.zhihu.matisse.R.dimen.grid_expected_size))
//                    .restrictOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT)
//                    .thumbnailScale(0.85f)
//                    .imageEngine(new GlideEngine())
//                    .showSingleMediaType(true)
//                    .originalEnable(true)
//                    .maxOriginalSize(10)
//                    .autoHideToolbarOnSingleTap(true)
//                    .forResult(REQUEST_CODE_CHOOSE);
        } else if (view == binding.funTeamSettingNewActivityTeamUpgrade.viewTitleArrowLl) {
            XKitRouter.withKey("BuyGroupFeatureActivity").withParam("type", 0).withParam("groupId", groupId).withContext(this).navigate();

        } else if (view == binding.funTeamSettingNewActivityVlqHb.viewTitleArrowLl) {
            HashMap map = new HashMap();
            map.put("groupId", groupId);
            FunTeamSettingNew_Vlq_HB_ListActivity.start(FunTeamSettingNew_Vlq_HB_ListActivity.class, this, map);
        } else if (view == binding.funTeamSettingNewActivitySearchHistory.viewTitleArrowLl) {
            openSearchChatHistory();
        }
    }

    private void openSearchChatHistory() {
        if (TextUtils.isEmpty(groupId)) {
            return;
        }
        Team team = NIMClient.getService(TeamService.class).queryTeamBlock(groupId);
        if (team == null) {
            ToastUtils.toastMsg("群信息加载中，请稍后再试");
            return;
        }
        XKitRouter.withKey(RouterConstant.PATH_FUN_CHAT_SEARCH_PAGE)
                .withParam(RouterConstant.CHAT_KRY, team)
                .withContext(this)
                .navigate();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == Constant.REQUEST_CODE_CHOOSE && resultCode == RESULT_OK) {
            List<Uri> uris = Matisse.obtainResult(data);
            List<String> strings = Matisse.obtainPathResult(data);
            if (!strings.isEmpty()) {
                UploadUtil.uploadImage(strings.get(0), "", new CommonCallBack() {
                    @Override
                    public void onCallBackUserBean(UserBean userBean) {
                        RegisterBean bean = new RegisterBean();
                        bean.groupId = groupId;
                        bean.head = userBean.url;
                        HttpUtil.apiW().group_updateGroupInfo(bean).enqueue(new CommonCallback<NetData>() {
                            @Override
                            public void Successful(Call<NetData> call, Response<NetData> response, NetData body) {

                                ToastUtils.toastMsg(body.msg);
                                _requestData();
                            }

                            @Override
                            public void Failure(Call<NetData> call, Throwable t) {

                            }
                        });
                    }
                });
            }
        }
    }

    @Override
    protected void callBackResult(Intent data) {
        super.callBackResult(data);
        String result = data.getStringExtra("result");
        String type = data.getStringExtra("type");
        if ("1".equals(type)) {
            RegisterBean bean = new RegisterBean();
            bean.groupId = groupId;
            bean.groupName = result;
            HttpUtil.apiW().group_updateGroupInfo(bean).enqueue(new CommonCallback<NetData>() {
                @Override
                public void Successful(Call<NetData> call, Response<NetData> response, NetData body) {

                    ToastUtils.toastMsg(body.msg);
                    _requestData();
                }

                @Override
                public void Failure(Call<NetData> call, Throwable t) {

                }
            });
        } else if ("2".equals(type)) {

            RegisterBean bean = new RegisterBean();
            bean.groupId = groupId;
            bean.nickName = result;
            HttpUtil.apiW().groupMember_installGroupNickName(bean).enqueue(new CommonCallback<NetData>() {
                @Override
                public void Successful(Call<NetData> call, Response<NetData> response, NetData body) {

                    ToastUtils.toastMsg(body.msg);
                    _requestData();
                }

                @Override
                public void Failure(Call<NetData> call, Throwable t) {

                }
            });
        } else if ("3".equals(type)) {

            RegisterBean bean = new RegisterBean();
            bean.groupId = groupId;
            bean.announcement = result;
            HttpUtil.apiW().group_updateGroupInfo(bean).enqueue(new CommonCallback<NetData>() {
                @Override
                public void Successful(Call<NetData> call, Response<NetData> response, NetData body) {

                    ToastUtils.toastMsg(body.msg);
                    _requestData();
                }

                @Override
                public void Failure(Call<NetData> call, Throwable t) {

                }
            });
        }

    }

    public void configStick(String sessionId, boolean stick) {
        if (TextUtils.isEmpty(sessionId)) {
            return;
        }
        if (stick) {
            ConversationRepo.addStickTop(sessionId, SessionTypeEnum.Team, "", new FetchCallback<StickTopSessionInfo>() {
                @Override
                public void onSuccess(@Nullable StickTopSessionInfo param) {
                    ConversationRepo.notifyStickTop(sessionId, SessionTypeEnum.Team);
                    binding.funTeamSettingNewActivityZhiding.viewTitleArrowRightTvSwitch.setSelected(!binding.funTeamSettingNewActivityZhiding.viewTitleArrowRightTvSwitch.isSelected());

                }

                @Override
                public void onFailed(int code) {
                    Log.i("fanbo", code + "");
                }

                @Override
                public void onException(@Nullable Throwable exception) {
                    Log.i("fanbo", exception.getMessage() + "");
                }
            });
        } else {
            ConversationRepo.removeStickTop(sessionId, SessionTypeEnum.Team, "", new FetchCallback<Void>() {
                @Override
                public void onSuccess(@Nullable Void param) {
                    ConversationRepo.notifyStickTop(sessionId, SessionTypeEnum.Team);
                    binding.funTeamSettingNewActivityZhiding.viewTitleArrowRightTvSwitch.setSelected(!binding.funTeamSettingNewActivityZhiding.viewTitleArrowRightTvSwitch.isSelected());

                }

                @Override
                public void onFailed(int code) {
                }

                @Override
                public void onException(@Nullable Throwable exception) {
                }
            });
        }
    }


    private void syncYunXinGradeToLocal(String teamId) {
        TeamRepo.getMemberList(teamId, new FetchCallback<List<UserInfoWithTeam>>() {
            @Override
            public void onSuccess(@Nullable List<UserInfoWithTeam> members) {
                if (members != null && groupInfoBean != null && groupInfoBean.userInfos != null) {
                    // 遍历云信成员列表
                    for (UserInfoWithTeam member : members) {
                        String accId = member.getTeamInfo().getAccount();
                        String nick = member.getUserInfo() != null ? member.getUserInfo().getName() : "";
                        // 从UserInfo的扩展字段获取grade
                        int grade = 0;
                        if (member.getUserInfo() != null) {
                            Map<String, Object> ext = member.getUserInfo().getExtensionMap();
                            if (ext != null && ext.get("grade") != null) {
                                grade = (int) ext.get("grade");
                            }
                        }
                        // 在本地userInfos中查找对应的成员
                        for (GroupInfoBean local : groupInfoBean.userInfos) {
                            if (local.userId != null && local.userId.equals(accId)) {
                                // 找到匹配的成员，复制grade
                                if (grade != 0) {
                                    local.grade = grade;
                                }
                                break;
                            }
                        }
                        // 打印调试信息
                        System.out.println("成员accId: " + accId + "，昵称: " + nick + "，grade: " + grade);
                    }
                    newAdapter = new TeamSettingUserInfoNewAdapter(false, groupInfoBean.userInfos);
                    binding.rvUserInfo.setAdapter(newAdapter);
                    newAdapter.setOnItemClickListener((baseQuickAdapter, view, i) -> {
                        HttpUtil.apiW().group_groupManage(groupId).enqueue(new CommonCallback<NetData>() {
                            @Override
                            public void Successful(Call<NetData> call, Response<NetData> response, NetData body) {
                                GroupInfoBean tempBean = new Gson().fromJson(body.data.toString(), GroupInfoBean.class);
                                if (groupInfoBean.rankState == 1 || groupInfoBean.rankState == 2) {
                                    if (!groupInfoBean.userInfos.get(i).userId.equals(DataUtil.getUserid())) {
                                        XKitRouter.withKey(Constant.FunTeamUserInfoDetailActivityKey).withParam("groupId", groupId).withParam("userId", groupInfoBean.userInfos.get(i).userId).withContext(view.getContext()).navigate();
                                    }
                                    return;
                                }
                                if (tempBean.addFriendsState == 0) {
                                    ToastUtils.toastMsg("禁止群成员相互添加好友");
                                } else {
                                    if (!groupInfoBean.userInfos.get(i).userId.equals(DataUtil.getUserid())) {
                                        XKitRouter.withKey(Constant.FunTeamUserInfoDetailActivityKey).withParam("groupId", groupId).withParam("userId", groupInfoBean.userInfos.get(i).userId).withContext(view.getContext()).navigate();
                                    }
                                }
                            }

                            @Override
                            public void Failure(Call<NetData> call, Throwable t) {

                            }
                        });
                    });
                }
            }

            @Override
            public void onFailed(int code) {
                ToastUtils.toastMsg("获取云信群成员失败，code=" + code);
            }

            @Override
            public void onException(@Nullable Throwable exception) {
                ToastUtils.toastMsg("获取云信群成员异常: " + (exception != null ? exception.getMessage() : ""));
            }
        });
    }

    protected void onStart() {
        super.onStart();
        if (SPUtils.getInstance().getBoolean("reloadTeamSettingData")) {
            _requestData();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(BaseEvent event) {
        if ("reloadTeamSettingData".equals(event.getTag())) {
            SPUtils.getInstance().put("reloadTeamSettingData", true);
        }
    }
}
