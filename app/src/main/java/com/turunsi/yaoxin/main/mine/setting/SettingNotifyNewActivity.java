// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.turunsi.yaoxin.main.mine.setting;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;

import com.google.gson.Gson;
import com.netease.nimlib.sdk.NIMClient;
import com.netease.nimlib.sdk.msg.MsgService;
import com.netease.nimlib.sdk.msg.constant.SessionTypeEnum;
import com.netease.nimlib.sdk.msg.model.RecentContact;
import com.netease.yunxin.kit.chatkit.model.ConversationInfo;
import com.netease.yunxin.kit.chatkit.repo.ConversationRepo;
import com.netease.yunxin.kit.corekit.im.provider.FetchCallback;
import com.netease.yunxin.kit.corekit.im.repo.MiscRepo;
import com.turunsi.yaoxin.databinding.ActivityMineSettingNotifyNewBinding;
import com.yaoxin.appbase.activity.BaseActivity;
import com.yaoxin.appbase.model.NetData;
import com.yaoxin.appbase.model.RegisterBean;
import com.yaoxin.appbase.model.UserBean;
import com.yaoxin.appbase.net.CommonCallback;
import com.yaoxin.appbase.net.HttpUtil;
import com.yaoxin.appbase.utils.DataUtil;
import com.yaoxin.appbase.utils.DialogAlertUtil;
import com.yaoxin.appbase.utils.ToastUtils;

import java.util.List;

import androidx.annotation.Nullable;

import retrofit2.Call;
import retrofit2.Response;

public class SettingNotifyNewActivity extends BaseActivity {

    private ActivityMineSettingNotifyNewBinding viewBinding;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewBinding = ActivityMineSettingNotifyNewBinding.inflate(getLayoutInflater());
        setContentView(viewBinding.getRoot());

        // 设置返回按钮
        viewBinding.activityMineSettingNotifyBackIv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    @Override
    protected void _requestData() {

        HttpUtil.apiW().home_selectSoundSwith().enqueue(new CommonCallback<NetData>() {
            @Override
            public void Successful(Call<NetData> call, Response<NetData> response, NetData body) {
                UserBean userBean = new Gson().fromJson(body.data.toString(), UserBean.class);
                if (userBean != null) {
                    initView(userBean);
                }
            }

            @Override
            public void Failure(Call<NetData> call, Throwable t) {

            }
        });
    }

    private void initView(UserBean userBean) {
        // 设置第一个开关：消息通知
        // allDisturb: "1"表示免打扰(关闭通知)，"0"表示开启通知
        viewBinding.activityMineSettingNotifyCell1.viewTitleDetailArrowTemplateTitleTv.setText("消息通知");
        viewBinding.activityMineSettingNotifyCell1.viewTitleDetailArrowTemplateDetailTv.setText("开启后 有新消息将会收到推送");
        viewBinding.activityMineSettingNotifyCell1.viewTitleDetailArrowTemplateSwitch.setVisibility(View.VISIBLE);
        boolean isNotificationOn = !"1".equals(userBean.allDisturb); // "0"或空表示开启通知
        viewBinding.activityMineSettingNotifyCell1.viewTitleDetailArrowTemplateSwitch.setSelected(isNotificationOn);
        viewBinding.activityMineSettingNotifyCell1.viewTitleDetailArrowTemplateSwitch.setOnClickListener(v -> {
            // 先切换状态，然后更新
            boolean newState = !viewBinding.activityMineSettingNotifyCell1.viewTitleDetailArrowTemplateSwitch.isSelected();
            viewBinding.activityMineSettingNotifyCell1.viewTitleDetailArrowTemplateSwitch.setSelected(newState);
            updateStatus(1);
        });

        // 设置第二个开关：消息提示音
        viewBinding.activityMineSettingNotifyCell2.viewTitleDetailArrowTemplateTitleTv.setText("消息提示音");
        viewBinding.activityMineSettingNotifyCell2.viewTitleDetailArrowTemplateDetailTv.setText("开启后 有新消息将会声音提醒");
        viewBinding.activityMineSettingNotifyCell2.viewTitleDetailArrowTemplateSwitch.setVisibility(View.VISIBLE);
        boolean isSoundOn = "1".equals(userBean.sound);
        viewBinding.activityMineSettingNotifyCell2.viewTitleDetailArrowTemplateSwitch.setSelected(isSoundOn);
        viewBinding.activityMineSettingNotifyCell2.viewTitleDetailArrowTemplateSwitch.setOnClickListener(v -> {
            boolean newState = !viewBinding.activityMineSettingNotifyCell2.viewTitleDetailArrowTemplateSwitch.isSelected();
            viewBinding.activityMineSettingNotifyCell2.viewTitleDetailArrowTemplateSwitch.setSelected(newState);
            updateStatus(2);
        });

        // 设置第三个开关：振动
        viewBinding.activityMineSettingNotifyCell3.viewTitleDetailArrowTemplateTitleTv.setText("振动");
        viewBinding.activityMineSettingNotifyCell3.viewTitleDetailArrowTemplateDetailTv.setText("开启后 有新消息将会振动提醒");
        viewBinding.activityMineSettingNotifyCell3.viewTitleDetailArrowTemplateSwitch.setVisibility(View.VISIBLE);
        boolean isShakeOn = "1".equals(userBean.shake);
        viewBinding.activityMineSettingNotifyCell3.viewTitleDetailArrowTemplateSwitch.setSelected(isShakeOn);
        viewBinding.activityMineSettingNotifyCell3.viewTitleDetailArrowTemplateSwitch.setOnClickListener(v -> {
            boolean newState = !viewBinding.activityMineSettingNotifyCell3.viewTitleDetailArrowTemplateSwitch.isSelected();
            viewBinding.activityMineSettingNotifyCell3.viewTitleDetailArrowTemplateSwitch.setSelected(newState);
            updateStatus(3);
        });

        // 保存初始状态
        DataUtil.setStringValue(isSoundOn ? "1" : "0", "ring_soud");
        DataUtil.setStringValue(isShakeOn ? "1" : "0", "shake_soud");

        // 设置清除聊天记录
        viewBinding.activityMineSettingNotifyClearHistory.viewTitleArrowTv.setText("清除聊天记录");
        viewBinding.activityMineSettingNotifyClearHistory.viewTitleArrowArrowIv.setVisibility(View.VISIBLE);
        viewBinding.activityMineSettingNotifyClearHistory.viewTitleArrowRightTvSwitch.setVisibility(View.GONE);
        viewBinding.activityMineSettingNotifyClearHistory.viewTitleArrowLl.setOnClickListener(v -> {
            DialogAlertUtil.showAlert("确认删除聊天记录吗？", new DialogAlertUtil.DialogAlertUtilCallBack() {
                @Override
                public void clickType(int type) {
                    if (type == 1) {
                        // 清除所有会话的聊天记录
                        clearServerMessageHistory();
                        ToastUtils.toastMsg("清除成功");
                    }
                }
            }, getSupportFragmentManager());
        });
    }

    // 添加清除服务器聊天记录的方法
    private void clearServerMessageHistory() {
        // 获取所有会话列表 - 需要传入 Comparator 和 Callback
        ConversationRepo.getAllSessionList(null, new FetchCallback<List<ConversationInfo>>() {
            @Override
            public void onSuccess(@Nullable List<ConversationInfo> conversations) {
                if (conversations != null && !conversations.isEmpty()) {
                    for (ConversationInfo conversation : conversations) {
                        if (conversation.getSessionType() == SessionTypeEnum.P2P) {
                            // 清除单聊记录
                            clearP2PMessageHistory(conversation.getContactId());
                        } else if (conversation.getSessionType() == SessionTypeEnum.Team) {
                            // 清除群聊记录
                            clearTeamMessageHistory(conversation.getContactId());
                        }
                    }
                }
            }

            @Override
            public void onFailed(int code) {
            }

            @Override
            public void onException(@Nullable Throwable exception) {
            }
        });
    }

    // 清除单聊记录
    private void clearP2PMessageHistory(String account) {
        NIMClient.getService(MsgService.class).clearChattingHistory(account, SessionTypeEnum.P2P);
        NIMClient.getService(MsgService.class).clearServerHistory(account, SessionTypeEnum.P2P);
        //NIMClient.getService(MsgService.class).clearServerHistory(account, SessionTypeEnum.P2P, true);
    }

    // 清除群聊记录
    private void clearTeamMessageHistory(String teamId) {
        NIMClient.getService(MsgService.class).clearChattingHistory(teamId, SessionTypeEnum.Team);
        NIMClient.getService(MsgService.class).clearServerHistory(teamId, SessionTypeEnum.Team);
        //NIMClient.getService(MsgService.class).clearServerHistory(teamId, SessionTypeEnum.Team, true);
    }

    void updateStatus(int type) {
        RegisterBean registerBean = new RegisterBean();
        if (type == 1) {
            // allDisturb: "1"表示免打扰(关闭通知)，"0"表示开启通知
            // isSelected为true表示开启通知，需要发送"0"（不免打扰）
            registerBean.allDisturb = viewBinding.activityMineSettingNotifyCell1.viewTitleDetailArrowTemplateSwitch.isSelected() ? "0" : "1";
        }
        if (type == 2) {
            registerBean.sound = viewBinding.activityMineSettingNotifyCell2.viewTitleDetailArrowTemplateSwitch.isSelected() ? "1" : "0";
        }
        if (type == 3) {
            registerBean.shake = viewBinding.activityMineSettingNotifyCell3.viewTitleDetailArrowTemplateSwitch.isSelected() ? "1" : "0";
        }
        HttpUtil.apiW().home_soundSwitch(registerBean).enqueue(new CommonCallback<NetData>() {
            @Override
            public void Successful(Call<NetData> call, Response<NetData> response, NetData body) {
                ToastUtils.toastMsg(body.msg);
                if (type == 2) {
                    DataUtil.setStringValue(viewBinding.activityMineSettingNotifyCell2.viewTitleDetailArrowTemplateSwitch.isSelected() ? "1" : "0", "ring_soud");
                } else if (type == 3) {
                    DataUtil.setStringValue(viewBinding.activityMineSettingNotifyCell3.viewTitleDetailArrowTemplateSwitch.isSelected() ? "1" : "0", "shake_soud");
                }
                // 重新请求数据以更新UI
                _requestData();
            }

            @Override
            public void Failure(Call<NetData> call, Throwable t) {
                // 如果失败，恢复之前的状态
                _requestData();
            }
        });
    }

}
