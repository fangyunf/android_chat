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
import com.netease.nimlib.sdk.SDKOptions;
import com.netease.nimlib.sdk.StatusBarNotificationConfig;
import com.netease.nimlib.sdk.msg.MsgService;
import com.netease.nimlib.sdk.msg.constant.SessionTypeEnum;
import com.netease.yunxin.kit.chatkit.model.ConversationInfo;
import com.netease.yunxin.kit.chatkit.repo.ConversationRepo;
import com.netease.yunxin.kit.common.ui.viewmodel.LoadStatus;
import com.netease.yunxin.kit.common.utils.SPUtils;
import com.netease.yunxin.kit.corekit.im.provider.FetchCallback;
import com.netease.yunxin.kit.corekit.im.repo.MiscRepo;
import com.turunsi.yaoxin.NimSDKOptionConfig;
import com.turunsi.yaoxin.databinding.ActivityMineSettingNotifyBinding;
import com.turunsi.yaoxin.databinding.ActivityMineSettingNotifyNewBinding;
import com.yaoxin.appbase.activity.BaseActivity;
import com.yaoxin.appbase.model.NetData;
import com.yaoxin.appbase.model.RegisterBean;
import com.yaoxin.appbase.model.UserBean;
import com.yaoxin.appbase.net.CommonCallback;
import com.yaoxin.appbase.net.HttpUtil;
import com.yaoxin.appbase.utils.BaseEvent;
import com.yaoxin.appbase.utils.DataUtil;
import com.yaoxin.appbase.utils.DialogAlertUtil;
import com.yaoxin.appbase.utils.ToastUtils;

import org.greenrobot.eventbus.EventBus;

import java.util.List;

import retrofit2.Call;
import retrofit2.Response;

public class SettingNotifyNewActivity extends BaseActivity {

    private ActivityMineSettingNotifyNewBinding viewBinding;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewBinding = ActivityMineSettingNotifyNewBinding.inflate(getLayoutInflater());
        viewBinding.activityMineSettingNotifyNav.addCloseImageButton().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        setContentView(viewBinding.getRoot());
    }

    @Override
    protected void _requestData() {

        HttpUtil.apiW().home_selectSoundSwith()
                .enqueue(new CommonCallback<NetData>() {
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
        viewBinding.activityMineSettingNotifyCell1.viewTitleArrowTv.setText("新消息通知");
        viewBinding.activityMineSettingNotifyCell1.viewTitleArrowRightTvSwitch.setVisibility(View.VISIBLE);
        viewBinding.activityMineSettingNotifyCell1.viewTitleArrowArrowIv.setVisibility(View.GONE);
        viewBinding.activityMineSettingNotifyCell1.viewTitleArrowLl.setVisibility(View.GONE);

        viewBinding.activityMineSettingNotifyCell2.viewTitleArrowTv.setText("声音");
        viewBinding.activityMineSettingNotifyCell2.viewTitleArrowRightTvSwitch.setVisibility(View.VISIBLE);
        viewBinding.activityMineSettingNotifyCell2.viewTitleArrowArrowIv.setVisibility(View.GONE);

        viewBinding.activityMineSettingNotifyCell3.viewTitleArrowTv.setText("震动");
        viewBinding.activityMineSettingNotifyCell3.viewTitleArrowRightTvSwitch.setVisibility(View.VISIBLE);
        viewBinding.activityMineSettingNotifyCell3.viewTitleArrowArrowIv.setVisibility(View.GONE);

        viewBinding.activityMineSettingNotifyCell4.viewTitleArrowTv.setText("清除聊天记录");

        viewBinding.activityMineSettingNotifyCell1.viewTitleArrowRightTvSwitch.setSelected("1".equals(userBean.allDisturb));
        viewBinding.activityMineSettingNotifyCell1.viewTitleArrowRightTvSwitch.setOnClickListener(
                v -> {

                    updateStatus(1);
//                    viewBinding.activityMineSettingNotifyCell1.viewTitleArrowRightTvSwitch.setSelected(isCloseNotice);
//                    NIMClient.toggleNotification(isCloseNotice);
//                    isCloseNotice = !isCloseNotice;
//                    SPUtils.getInstance().put("isCloseNotice", isCloseNotice);
                });
        viewBinding.activityMineSettingNotifyCell2.viewTitleArrowRightTvSwitch.setSelected("1".equals(userBean.sound));
        viewBinding.activityMineSettingNotifyCell2.viewTitleArrowRightTvSwitch.setOnClickListener(
                v -> {
                    updateStatus(2);
//                    viewBinding.activityMineSettingNotifyCell2.viewTitleArrowRightTvSwitch.setSelected(isCloseVoice);
//                    isCloseVoice = !isCloseVoice;
//                    SPUtils.getInstance().put("isCloseVoice", isCloseVoice);
//                    updatConfig();

                });
        viewBinding.activityMineSettingNotifyCell3.viewTitleArrowRightTvSwitch.setSelected("1".equals(userBean.shake));
        viewBinding.activityMineSettingNotifyCell3.viewTitleArrowRightTvSwitch.setOnClickListener(
                v -> {
                    updateStatus(3);
//                    viewBinding.activityMineSettingNotifyCell3.viewTitleArrowRightTvSwitch.setSelected(isCloseShake);
//                    isCloseShake = !isCloseShake;
//                    SPUtils.getInstance().put("isCloseShake", isCloseShake);
//                    updatConfig();
                });
        viewBinding.activityMineSettingNotifyCell4.viewTitleArrowLl.setOnClickListener(
                v -> {
                    DialogAlertUtil.showAlert("确定清空聊天记录吗？", new DialogAlertUtil.DialogAlertUtilCallBack() {
                        @Override
                        public void clickType(int type) {
                            if (type == 1) {
                                // MiscRepo.INSTANCE.clearMessageCache();
                                clearServerMessageHistory();
                            }
                        }
                    }, getSupportFragmentManager());

                });
        DataUtil.setStringValue(viewBinding.activityMineSettingNotifyCell2.viewTitleArrowRightTvSwitch.isSelected() ? "1" : "0", "ring_soud");
        DataUtil.setStringValue(viewBinding.activityMineSettingNotifyCell3.viewTitleArrowRightTvSwitch.isSelected() ? "1" : "0", "shake_soud");
//
    }

    void updateStatus(int type) {
        RegisterBean registerBean = new RegisterBean();
        if (type == 1) {
            registerBean.allDisturb = viewBinding.activityMineSettingNotifyCell1.viewTitleArrowRightTvSwitch.isSelected() ? "0" : "1";
        }
        if (type == 2) {
            registerBean.sound = viewBinding.activityMineSettingNotifyCell2.viewTitleArrowRightTvSwitch.isSelected() ? "0" : "1";
        }
        if (type == 3) {
            registerBean.shake = viewBinding.activityMineSettingNotifyCell3.viewTitleArrowRightTvSwitch.isSelected() ? "0" : "1";
        }
        HttpUtil.apiW().home_soundSwitch(registerBean)
                .enqueue(new CommonCallback<NetData>() {
                    @Override
                    public void Successful(Call<NetData> call, Response<NetData> response, NetData body) {
                        ToastUtils.toastMsg(body.msg);
                        if (type == 1) {
//                            NIMClient.toggleNotification("1".equals(registerBean.allDisturb));
                        } else if (type == 2) {
                            DataUtil.setStringValue(viewBinding.activityMineSettingNotifyCell2.viewTitleArrowRightTvSwitch.isSelected() ? "1" : "0", "ring_soud");
//                            StatusBarNotificationConfig config = NimSDKOptionConfig.loadStatusBarNotificationConfig();
//                            config.ring = !viewBinding.activityMineSettingNotifyCell2.viewTitleArrowRightTvSwitch.isSelected() ;
//                            config.vibrate = viewBinding.activityMineSettingNotifyCell3.viewTitleArrowRightTvSwitch.isSelected() ;
//                            NIMClient.updateStatusBarNotificationConfig(config);
                        } else if (type == 3) {
                            DataUtil.setStringValue(viewBinding.activityMineSettingNotifyCell3.viewTitleArrowRightTvSwitch.isSelected() ? "1" : "0", "shake_soud");
//                            StatusBarNotificationConfig config = NimSDKOptionConfig.loadStatusBarNotificationConfig();
//                            config.ring = viewBinding.activityMineSettingNotifyCell2.viewTitleArrowRightTvSwitch.isSelected() ;
//                            config.vibrate = !viewBinding.activityMineSettingNotifyCell3.viewTitleArrowRightTvSwitch.isSelected() ;
//                            NIMClient.updateStatusBarNotificationConfig(config);
                        }
                        _requestData();

                    }

                    @Override
                    public void Failure(Call<NetData> call, Throwable t) {

                    }
                });
    }

    void updatConfig() {

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

}
