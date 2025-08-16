// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.turunsi.yaoxin.main.mine.setting;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;

import com.netease.nimlib.sdk.NIMClient;
import com.netease.nimlib.sdk.misc.DirCacheFileType;
import com.netease.nimlib.sdk.msg.MsgService;
import com.netease.nimlib.sdk.msg.constant.SessionTypeEnum;
import com.netease.yunxin.kit.chatkit.model.ConversationInfo;
import com.netease.yunxin.kit.chatkit.repo.ConversationRepo;
import com.netease.yunxin.kit.chatkit.ui.custom.ChatConfigManager;
import com.netease.yunxin.kit.common.ui.dialog.ChoiceListener;
import com.netease.yunxin.kit.common.ui.dialog.CommonChoiceDialog;
import com.netease.yunxin.kit.common.utils.SizeUtils;
import com.netease.yunxin.kit.corekit.im.IMKitClient;
import com.netease.yunxin.kit.corekit.im.provider.FetchCallback;
import com.netease.yunxin.kit.corekit.im.repo.MiscRepo;
import com.turunsi.yaoxin.IMApplication;
import com.turunsi.yaoxin.R;
import com.turunsi.yaoxin.databinding.ActivityMineSetNewBinding;
import com.turunsi.yaoxin.databinding.ActivityMineSettingBinding;
import com.turunsi.yaoxin.login.LoginActivity;
import com.turunsi.yaoxin.main.mine.DownLoadActivity;
import com.turunsi.yaoxin.main.mine.account.AccountAnQuanManagerActivity;
import com.turunsi.yaoxin.main.mine.account.AccountDetailActivity;
import com.turunsi.yaoxin.main.mine.account.AccoutCodeDetailActivity;
import com.turunsi.yaoxin.register.ForgetPwdActivity;
import com.turunsi.yaoxin.welcome.WelcomeActivity;
import com.yaoxin.appbase.activity.BaseActivity;
import com.yaoxin.appbase.model.NetData;
import com.yaoxin.appbase.net.CommonCallback;
import com.yaoxin.appbase.net.HttpUtil;
import com.yaoxin.appbase.utils.DataUtil;
import com.yaoxin.appbase.utils.DialogAlertUtil;
import com.yaoxin.appbase.utils.StatusBarUtils;
import com.yaoxin.appbase.utils.ToastUtils;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Response;

public class SettingNewActivity extends BaseActivity implements View.OnClickListener {

    private ActivityMineSetNewBinding viewBinding;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//    changeStatusBarColor(R.color.color_e9eff5);
        viewBinding = ActivityMineSetNewBinding.inflate(getLayoutInflater());
        setContentView(viewBinding.getRoot());
        StatusBarUtils.transtStatusBar(this, viewBinding.activityMineSetNewNav);
        initView();
    }

    private void initView() {

        viewBinding.activityMineSetNewNav.addCloseImageButton().setOnClickListener(this);
        viewBinding.activityMineSetNewNoticeVoice.viewTitleArrowLl.setOnClickListener(this);
        viewBinding.activityMineSetNewDeleteCache.viewTitleArrowLl.setOnClickListener(this);
        viewBinding.activityMineSetNewDeleteRecord.viewTitleArrowLl.setOnClickListener(this);
        viewBinding.activityMineSetNewAboutUs.viewTitleArrowLl.setOnClickListener(this);
        viewBinding.activityMineSetNewDownload.viewTitleArrowLl.setOnClickListener(this);
        viewBinding.activityMineSetNewLoginOut.viewTitleArrowLl.setOnClickListener(this);
        viewBinding.activityMineSetNewExchangeAcount.viewTitleArrowLl.setOnClickListener(this);
        viewBinding.activityMineSetNewZhuxiao.viewTitleArrowLl.setOnClickListener(this);

        viewBinding.layoutZhanghaoAnquan.viewTitleArrowTv.setText("账号与安全");
        viewBinding.layoutZhanghaoAnquan.viewTitleArrowLl.setOnClickListener(this);
        viewBinding.layoutNewNotice.viewTitleArrowTv.setText("新消息通知");
        viewBinding.layoutNewNotice.viewTitleArrowLl.setOnClickListener(this);
        viewBinding.layoutAnQuan.viewTitleArrowTv.setText("安全与隐私");
        viewBinding.layoutAnQuan.viewTitleArrowLl.setOnClickListener(this);

        viewBinding.activityMineSetNewNoticeVoice.viewTitleArrowTv.setText("通知声音");
        viewBinding.activityMineSetNewDeleteCache.viewTitleArrowTv.setText("清空缓存");
        viewBinding.activityMineSetNewDeleteRecord.viewTitleArrowTv.setText("清空所有聊天记录");
        viewBinding.activityMineSetNewAboutUs.viewTitleArrowTv.setText("关于我们");

        viewBinding.activityMineSetNewDownload.viewTitleArrowTv.setText("下载地址");
        viewBinding.activityMineSetNewLoginOut.viewTitleArrowTv.setText("退出登录");
        viewBinding.activityMineSetNewZhuxiao.viewTitleArrowTv.setText("注销账号");
        viewBinding.activityMineSetNewExchangeAcount.viewTitleArrowTv.setText("切换账号");
//      viewBinding.activityMineSetNewExchangeAcount.viewTitleArrowTv.setTextColor(getResources().getColor(com.yaoxin.appbase.R.color.app_theme_red_color));
        viewBinding.activityMineSetNewLoginOut.viewTitleArrowTv.setTextColor(getResources().getColor(com.yaoxin.appbase.R.color.app_theme_red_color));
        viewBinding.activityMineSetNewZhuxiao.viewTitleArrowTv.setTextColor(getResources().getColor(com.yaoxin.appbase.R.color.app_theme_red_color));
    }

    @Override
    public void onClick(View v) {
        if (v == viewBinding.layoutZhanghaoAnquan.viewTitleArrowLl) {
            AccountDetailActivity.start(AccountDetailActivity.class, this, null);
        } else if (v == viewBinding.activityMineSetNewNoticeVoice.viewTitleArrowLl || v == viewBinding.layoutNewNotice.viewTitleArrowLl) {
            startActivity(new Intent(SettingNewActivity.this, SettingNotifyNewActivity.class));
        } else if (v == viewBinding.layoutAnQuan.viewTitleArrowLl) {
            AccountAnQuanManagerActivity.start(AccountAnQuanManagerActivity.class, this, null);
        } else if (v == viewBinding.activityMineSetNewDeleteCache.viewTitleArrowLl) {
            //startActivity(new Intent(SettingNewActivity.this, ClearCacheActivity.class));
            DialogAlertUtil.showAlert("确定清空缓存吗？", new DialogAlertUtil.DialogAlertUtilCallBack() {
                @Override
                public void clickType(int type) {
                    if (type == 1) {
                        MiscRepo.INSTANCE.clearCacheSize(getSDKFileType(), new FetchCallback<Void>() {
                            @Override
                            public void onSuccess(@Nullable Void param) {
                            }

                            @Override
                            public void onFailed(int code) {
                            }

                            @Override
                            public void onException(@Nullable Throwable exception) {
                            }
                        });
                        ToastUtils.toastMsg("操作成功");
                    }
                }
            }, getSupportFragmentManager());
        } else if (v == viewBinding.activityMineSetNewDeleteRecord.viewTitleArrowLl) {
            DialogAlertUtil.showAlert("确定清空聊天记录吗？", new DialogAlertUtil.DialogAlertUtilCallBack() {
                @Override
                public void clickType(int type) {
                    if (type == 1) {
                        // MiscRepo.INSTANCE.clearMessageCache();
                        clearServerMessageHistory();
                        ToastUtils.toastMsg("操作成功");
                    }
                }
            }, getSupportFragmentManager());
        } else if (v == viewBinding.activityMineSetNewAboutUs.viewTitleArrowLl) {

            AboutUsNewActivity.start(AboutUsNewActivity.class, this, null);
        } else if (v == viewBinding.activityMineSetNewLoginOut.viewTitleArrowLl) {
            showLogin();

        } else if (v == viewBinding.activityMineSetNewExchangeAcount.viewTitleArrowLl) {
//        showLogin();
            ExchangeAccountActivity.start(ExchangeAccountActivity.class, this, null);
        } else if (v == viewBinding.activityMineSetNewNav.addCloseImageButton()) {
            finish();

        } else if (v == viewBinding.activityMineSetNewDownload.viewTitleArrowLl) {
            DownLoadActivity.start(DownLoadActivity.class, this, null);
        } else if (v == viewBinding.activityMineSetNewZhuxiao.viewTitleArrowLl) {
            DialogAlertUtil.showAlert("确定注销账号吗？", new DialogAlertUtil.DialogAlertUtilCallBack() {
                @Override
                public void clickType(int type) {
                    if (type == 1) {
                        HttpUtil.apiW().home_logout().enqueue(new CommonCallback<NetData>() {
                            @Override
                            public void Successful(Call<NetData> call, Response<NetData> response, NetData body) {
                                ToastUtils.toastMsg("注销成功");
                                showLogin();
                            }

                            @Override
                            public void Failure(Call<NetData> call, Throwable t) {

                            }
                        });

                    }
                }
            }, getSupportFragmentManager());
        }
    }

    void showLogin() {
        IMKitClient.logoutIM(new com.netease.yunxin.kit.corekit.im.login.LoginCallback<Void>() {
            @Override
            public void onError(int errorCode, @NonNull String errorMsg) {
                Toast.makeText(SettingNewActivity.this, "error code is " + errorCode + ", message is " + errorMsg, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onSuccess(@Nullable Void data) {
                if (getApplicationContext() instanceof IMApplication) {
                    ((IMApplication) getApplicationContext()).clearActivity(SettingNewActivity.this);
                }
                DataUtil.deleteLoginUserInfoList(DataUtil.getUserInfo());
                DataUtil.deleteData();
                startActivity(new Intent(SettingNewActivity.this, LoginActivity.class));
                finish();
            }
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
    
    private List<DirCacheFileType> getSDKFileType() {
        List<DirCacheFileType> types = new ArrayList<>();
        types.add(DirCacheFileType.AUDIO);
        types.add(DirCacheFileType.THUMB);
        types.add(DirCacheFileType.IMAGE);
        types.add(DirCacheFileType.VIDEO);
        types.add(DirCacheFileType.OTHER);
        return types;
    }
}
