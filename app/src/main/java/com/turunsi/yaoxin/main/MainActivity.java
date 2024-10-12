// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.turunsi.yaoxin.main;

import static com.yzq.zxinglibrary.common.Constant.CODED_CONTENT;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.app.ActivityOptionsCompat;
import androidx.fragment.app.Fragment;

import com.google.gson.Gson;
//import com.king.camera.scan.CameraScan;
import com.netease.lava.nertc.sdk.NERtcOption;
import com.netease.nimlib.sdk.NIMClient;
import com.netease.nimlib.sdk.Observer;
import com.netease.nimlib.sdk.StatusCode;
import com.netease.nimlib.sdk.avsignalling.constant.ChannelType;
import com.netease.yunxin.kit.common.utils.SPUtils;
import com.netease.yunxin.kit.contactkit.ui.fun.addfriend.FunAddFriendVerifyActivity;
import com.netease.yunxin.kit.contactkit.ui.normal.contact.ContactNewFragment;
import com.netease.yunxin.kit.corekit.im.utils.RouterConstant;
import com.netease.yunxin.kit.corekit.route.XKitRouter;
import com.turunsi.yaoxin.AppSkinConfig;
import com.turunsi.yaoxin.BuildConfig;
import com.turunsi.yaoxin.CustomConfig;
import com.turunsi.yaoxin.IMApplication;
import com.turunsi.yaoxin.R;
import com.turunsi.yaoxin.databinding.ActivityMainBinding;
import com.turunsi.yaoxin.login.LoginActivity;
import com.turunsi.yaoxin.main.mine.MineFragment;
import com.turunsi.yaoxin.main.mine.setting.SettingNewActivity;
import com.turunsi.yaoxin.utils.Constant;
import com.turunsi.yaoxin.utils.DataUtils;
import com.turunsi.yaoxin.utils.IMUtil;
import com.turunsi.yaoxin.welcome.WelcomeActivity;
import com.netease.yunxin.kit.alog.ALog;
import com.netease.yunxin.kit.call.p2p.NECallEngine;
import com.netease.yunxin.kit.call.p2p.model.NECallInitRtcMode;
import com.netease.yunxin.kit.chatkit.repo.ContactRepo;
import com.netease.yunxin.kit.chatkit.ui.custom.ChatConfigManager;
import com.netease.yunxin.kit.common.ui.activities.BaseActivity;
import com.netease.yunxin.kit.contactkit.ui.contact.BaseContactFragment;
import com.netease.yunxin.kit.contactkit.ui.fun.contact.FunContactFragment;
import com.netease.yunxin.kit.contactkit.ui.normal.contact.ContactFragment;
import com.netease.yunxin.kit.conversationkit.ui.fun.page.FunConversationFragment;
import com.netease.yunxin.kit.conversationkit.ui.normal.page.ConversationFragment;
import com.netease.yunxin.kit.conversationkit.ui.page.ConversationBaseFragment;
import com.netease.yunxin.kit.corekit.event.BaseEvent;
import com.netease.yunxin.kit.corekit.event.EventCenter;
import com.netease.yunxin.kit.corekit.event.EventNotify;
import com.netease.yunxin.kit.corekit.im.IMKitClient;
import com.netease.yunxin.kit.corekit.im.model.UserInfo;
import com.netease.yunxin.kit.corekit.im.provider.FetchCallback;
import com.netease.yunxin.kit.corekit.im.repo.SettingRepo;
import com.netease.yunxin.nertc.ui.CallKitNotificationConfig;
import com.netease.yunxin.nertc.ui.CallKitUI;
import com.netease.yunxin.nertc.ui.CallKitUIOptions;
import com.yaoxin.appbase.model.NetData;
import com.yaoxin.appbase.model.ParamsBean;
import com.yaoxin.appbase.model.RegisterBean;
import com.yaoxin.appbase.model.UserBean;
import com.yaoxin.appbase.net.CommonCallback;
import com.yaoxin.appbase.net.HttpUtil;
import com.yaoxin.appbase.update.ycupdatelib.UpdateFragment;
import com.yaoxin.appbase.utils.AppProxy;
import com.yaoxin.appbase.utils.DataUtil;
import com.yaoxin.appbase.utils.StatusBarUtils;
import com.yaoxin.appbase.utils.ToastUtils;
import com.yzq.zxinglibrary.android.CaptureActivity;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import pub.devrel.easypermissions.EasyPermissions;
import retrofit2.Call;
import retrofit2.Response;

/**
 * IM Main Page include four tab , message/contact/live/profile
 */
public class MainActivity extends BaseActivity {

    private ActivityMainBinding activityMainBinding;
    private static final int START_INDEX = 0;
    private View mCurrentTab;
    //  private BaseContactFragment mContactFragment;
    private ContactNewFragment mContactFragment;
    private ConversationBaseFragment mConversationFragment;
    private ConversationBaseFragment mConversationFragment1;
    public static final int REQUEST_CODE_SCAN = 0x01;

    //皮肤变更事件
    EventNotify<SkinEvent> skinNotify =
            new EventNotify<SkinEvent>() {
                @Override
                public void onNotify(@NonNull SkinEvent message) {
                    Intent intent = getIntent();
                    finish();
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                }

                @NonNull
                @Override
                public String getEventType() {
                    return "skinEvent";
                }
            };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ALog.d(Constant.PROJECT_TAG, "MainActivity:onCreate");
        if (TextUtils.isEmpty(IMKitClient.account())) {
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
            finish();
            return;
        }
        activityMainBinding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(activityMainBinding.getRoot());
        initView();
        initData();
        EventCenter.registerEventNotify(skinNotify);

        EventBus.getDefault().register(this);
        _update();
        NIMClient.toggleNotification(false);

//        if (SPUtils.getInstance().getBoolean("isRegister")) {
//            SPUtils.getInstance().put("isRegister",false);
//            if (!com.yaoxin.appbase.net.Constant.isRunningRealName) {
//                com.yaoxin.appbase.net.Constant.isRunningRealName = true;
//                XKitRouter.withKey(com.yaoxin.appbase.net.Constant.RealName_Router)
//                        .withContext(AppProxy.getInstance().getContext())
//                        .navigate();
//            }
//        }
        Timer timer = new Timer();
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                // 这里是每30秒执行的任务
                HttpUtil.fetchTokenAsync();
            }
        };
        // 每30秒执行一次任务
        timer.scheduleAtFixedRate(task, 0, 5000);

    }

    void _update() {
        HttpUtil.apiW().customer_versionCkeck("AOS", BuildConfig.VERSION_NAME)
                .enqueue(new CommonCallback<NetData>() {
                    @Override
                    public void Successful(Call<NetData> call, Response<NetData> response, NetData body) {

                        if (body.data != null) {
                            ParamsBean updateBean = new Gson().fromJson(body.data.toString(), ParamsBean.class);

                            UpdateFragment.showFragment(MainActivity.this,
                                    true, updateBean.downloadUrl, "私信", updateBean.upMsg, BuildConfig.APPLICATION_ID, null);
                        }
                    }

                    @Override
                    public void Failure(Call<NetData> call, Throwable t) {

                    }
                });
    }

    private void initData() {
        SettingRepo.getShowReadStatus(
                new FetchCallback<Boolean>() {
                    @Override
                    public void onSuccess(@Nullable Boolean param) {
                        ChatConfigManager.showReadStatus = param;
                    }

                    @Override
                    public void onFailed(int code) {
                    }

                    @Override
                    public void onException(@Nullable Throwable exception) {
                    }
                });
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();
        // 部分Android机型在页面进入onResume前启动其他页面会取消当前页面流程，避免组件初始化后立即展示来电页面将初始化的逻辑滞后
        if (!CallKitUI.INSTANCE.getInit()) {
            configCallKit();
        }
    }

    private void initView() {
//    boolean isCommonSkin =
//        AppSkinConfig.getInstance().getAppSkinStyle() == AppSkinConfig.AppSkin.commonSkin;
        ALog.d(Constant.PROJECT_TAG, "MainActivity:initView");
        //    loadConfig();
        List<Fragment> fragments = new ArrayList<>();

        changeStatusBarColor(R.color.fun_page_bg_color);
        mConversationFragment = new FunConversationFragment(0);
//        AppProxy.getInstance().showType = 1;
        mConversationFragment1 = new FunConversationFragment(1);
//        mConversationFragment1._type = 1;
        mContactFragment = new ContactNewFragment();

        fragments.add(mConversationFragment);
        fragments.add(mConversationFragment1);
        fragments.add(mContactFragment);

        fragments.add(new MineFragment());

        FragmentAdapter fragmentAdapter = new FragmentAdapter(this);
        fragmentAdapter.setFragmentList(fragments);
        activityMainBinding.viewPager.setUserInputEnabled(false);
        activityMainBinding.viewPager.setAdapter(fragmentAdapter);
        activityMainBinding.viewPager.setCurrentItem(START_INDEX, false);
        activityMainBinding.viewPager.setOffscreenPageLimit(fragments.size());
        mCurrentTab = activityMainBinding.conversationBtnGroup;
        changeStatusBarColor(R.color.color_white);
        resetTabSkin();
    }

    @Override
    protected void onResume() {
        super.onResume();
        initContactFragment(mContactFragment);
        initConversationFragment(mConversationFragment);
        initConversationFragment(mConversationFragment1);
    }

    @Override
    protected void onDestroy() {
        EventCenter.unregisterEventNotify(skinNotify);

        EventBus.getDefault().unregister(this);
        super.onDestroy();
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    public void tabClick(View view) {

        if (mCurrentTab != null && mCurrentTab == view) {
            return;
        }
        if (view == activityMainBinding.conversationBtnGroup) {
            AppProxy.getInstance().showType = 1;
            mConversationFragment.getConversationView().adapter.notifyDataSetChanged();

        }
        if (view == activityMainBinding.conversationBtnGroup1) {
            AppProxy.getInstance().showType = 2;
            mConversationFragment1.getConversationView().adapter.notifyDataSetChanged();
        }
        resetTabStyle();
        mCurrentTab = view;
        resetTabSkin();
        StatusBarUtils.setStatusBarLightMode(this, true, true);
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    private void resetTabSkin() {
        if (mCurrentTab == activityMainBinding.contactBtnGroup) {
            activityMainBinding.viewPager.setCurrentItem(2, false);
            activityMainBinding.contact.setTextColor(
                    getResources().getColor(R.color.fun_tab_checked_color));
            activityMainBinding.contact.setCompoundDrawablesWithIntrinsicBounds(
                    null, getResources().getDrawable(R.mipmap.mine_tabbar_txl_sel), null, null);
            changeStatusBarColor(R.color.fun_page_bg_color);
        } else if (mCurrentTab == activityMainBinding.myselfBtnGroup) {
            activityMainBinding.viewPager.setCurrentItem(3, false);
            activityMainBinding.mine.setTextColor(
                    getResources().getColor(R.color.fun_tab_checked_color));
            activityMainBinding.mine.setCompoundDrawablesWithIntrinsicBounds(
                    null, getResources().getDrawable(R.mipmap.mine_tabbar_mine_sel), null, null);

            changeStatusBarColor(R.color.color_white);
        } else if (mCurrentTab == activityMainBinding.conversationBtnGroup) {
            activityMainBinding.viewPager.setCurrentItem(0, false);
            activityMainBinding.conversation.setTextColor(
                    getResources().getColor(R.color.fun_tab_checked_color));
            activityMainBinding.conversation.setCompoundDrawablesWithIntrinsicBounds(
                    null, getResources().getDrawable(R.mipmap.mine_tabbar_msg_sel), null, null);
            changeStatusBarColor(R.color.fun_page_bg_color);
        } else if (mCurrentTab == activityMainBinding.conversationBtnGroup1) {
            activityMainBinding.viewPager.setCurrentItem(1, false);
            activityMainBinding.conversation1.setTextColor(
                    getResources().getColor(R.color.fun_tab_checked_color));
            activityMainBinding.conversation1.setCompoundDrawablesWithIntrinsicBounds(
                    null, getResources().getDrawable(R.mipmap.mine_tabbar_msg_group_sel), null, null);
            changeStatusBarColor(R.color.fun_page_bg_color);
        }
    }

    private void initConversationFragment(ConversationBaseFragment conversationFragment) {
        if (conversationFragment != null) {
            conversationFragment.setConversationCallback(
                    count -> {
                        if (count == null) {
                            activityMainBinding.conversationDot.setVisibility(View.GONE);
                            activityMainBinding.conversationDot1.setVisibility(View.GONE);

                        } else {
                            int singleChatUnreadCount = 0;
                            int groupChatUnreadCount = 0;

                            if (count.size() == 2) {
                                singleChatUnreadCount = count.get(0);
                                groupChatUnreadCount = count.get(1);

                            }

                            if (conversationFragment == mConversationFragment) {
                                if (singleChatUnreadCount > 0) {
                                    activityMainBinding.conversationDot.setVisibility(View.VISIBLE);
                                } else {
                                    activityMainBinding.conversationDot.setVisibility(View.GONE);

                                }
                            }
                          if (conversationFragment == mConversationFragment1) {
                            if (groupChatUnreadCount > 0) {
                              activityMainBinding.conversationDot1.setVisibility(View.VISIBLE);
                            } else {
                              activityMainBinding.conversationDot1.setVisibility(View.GONE);

                            }
                          }
                        }
                    });
        }
    }

    private void initContactFragment(ContactNewFragment contactFragment) {
        if (contactFragment != null) {
            contactFragment.setContactCallback(
                    count -> {
                        if (count > 0) {
                            activityMainBinding.contactDot.setVisibility(View.VISIBLE);
                        } else {
                            activityMainBinding.contactDot.setVisibility(View.GONE);
                        }
                    });
        }
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    private void resetTabStyle() {

        activityMainBinding.conversation.setTextColor(
                getResources().getColor(R.color.tab_unchecked_color));
        activityMainBinding.conversation.setCompoundDrawablesWithIntrinsicBounds(
                null, getResources().getDrawable(R.mipmap.mine_tabbar_msg_normal), null, null);
        activityMainBinding.conversation1.setTextColor(
                getResources().getColor(R.color.tab_unchecked_color));
        activityMainBinding.conversation1.setCompoundDrawablesWithIntrinsicBounds(
                null, getResources().getDrawable(R.mipmap.mine_tabbar_msg_group_normal), null, null);

        activityMainBinding.contact.setTextColor(getResources().getColor(R.color.tab_unchecked_color));
        activityMainBinding.contact.setCompoundDrawablesWithIntrinsicBounds(
                null, getResources().getDrawable(R.mipmap.mine_tabbar_txl_normal), null, null);

        activityMainBinding.mine.setTextColor(getResources().getColor(R.color.tab_unchecked_color));
        activityMainBinding.mine.setCompoundDrawablesWithIntrinsicBounds(
                null, getResources().getDrawable(R.mipmap.mine_tabbar_mine_normal), null, null);
    }

    private void configCallKit() {

        CallKitUIOptions options =
                new CallKitUIOptions.Builder()
                        // 必要：音视频通话 sdk appKey，用于通话中使用
                        .rtcAppKey(DataUtils.readAppKey(this))
                        // 必要：当前用户 AccId
                        .currentUserAccId(IMKitClient.account())
                        // 通话接听成功的超时时间单位 毫秒，默认30s
                        .timeOutMillisecond(30 * 1000L)
                        // 此处为 收到来电时展示的 notification 相关配置，如图标，提示语等。
                        .notificationConfigFetcher(
                                invitedInfo -> {
                                    UserInfo info = ContactRepo.getUserInfoFromLocal(invitedInfo.callerAccId);
                                    String content =
                                            (info != null ? info.getUserInfoName() : invitedInfo.callerAccId)
                                                    + (invitedInfo.callType == ChannelType.AUDIO.getValue()
                                                    ? getString(R.string.incoming_call_notify_audio)
                                                    : getString(R.string.incoming_call_notify_video));
                                    ALog.d("=======" + content);
                                    return new CallKitNotificationConfig(R.mipmap.yaoxin_icon, null, null, content);
                                })
                        // 收到被叫时若 app 在后台，在恢复到前台时是否自动唤起被叫页面，默认为 true
                        .resumeBGInvitation(true)
                        // 请求 rtc token 服务，若非安全模式则不需设置(V1.8.0版本之前需要配置，V1.8.0及之后版本无需配置)
                        //.rtcTokenService((uid, callback) -> requestRtcToken(appKey, uid, callback)) // 自己实现的 token 请求方法
                        // 设置初始化 rtc sdk 相关配置，按照所需进行配置
                        .rtcSdkOption(new NERtcOption())
                        // 呼叫组件初始化 rtc 范围，NECallInitRtcMode.GLOBAL-全局初始化，
                        // NECallInitRtcMode.IN_NEED-每次通话进行初始化以及销毁，全局初始化有助于更快进入首帧页面，
                        // 当结合其他组件使用时存在rtc初始化冲突可设置NECallInitRtcMode.IN_NEED
                        // 或当结合其他组件使用时存在rtc初始化冲突可设置NECallInitRtcMode.IN_NEED_DELAY_TO_ACCEPT
                        .initRtcMode(NECallInitRtcMode.GLOBAL)
                        .build();
        // 设置自定义话单消息发送
        NECallEngine.sharedInstance().setCallRecordProvider(new CustomCallOrderProvider());
        // 若重复初始化会销毁之前的初始化实例，重新初始化
//    CallKitUI.init(getApplicationContext(), options);
        IMKitClient.getAuthServiceObserver().observeOnlineStatus(
                (Observer<StatusCode>) statusCode -> {
//              if (statusCode == StatusCode.LOGOUT) {
//                CallKitUI.destroy();
//              }
                    if (statusCode.wontAutoLogin()) {
                        // 处理被顶号的情况
                        if (statusCode == StatusCode.KICKOUT) {
                            // 被顶号
//                  handleKickout();
                            ToastUtils.toastMsg("您的账号在其他设备登录");
                            if (getApplicationContext() instanceof IMApplication) {
                                ((IMApplication) getApplicationContext())
                                        .clearActivity(MainActivity.this);
                            }
                            DataUtil.deleteData();
                            startActivity(new Intent(MainActivity.this, LoginActivity.class));
                            finish();
                        }
                    }
                },
                true);
    }

    private void loadConfig() {
        CustomConfig.configContactKit(this);
        CustomConfig.configConversation(this);
        CustomConfig.configChatKit(this);
    }

    //皮肤变更事件
    public static class SkinEvent extends BaseEvent {
        @NonNull
        @Override
        public String getType() {
            return "skinEvent";
        }
    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(com.yaoxin.appbase.utils.BaseEvent event) {
        if ("gotoScan".equals(event.getTag())) {
            String[] permission = new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE};
            // 根据系统版本判断，如果是Android13则采用Manifest.permission.READ_MEDIA_IMAGES
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                permission =
                        new String[]{
                                Manifest.permission.READ_MEDIA_IMAGES, Manifest.permission.READ_MEDIA_VIDEO
                        };
            }
            if (!EasyPermissions.hasPermissions(this, permission)) {
                // 请求相机权限
                EasyPermissions.requestPermissions(this, "需要访问相册权限", com.yaoxin.appbase.net.Constant.RC_PHOTO_PICKER_PERM, permission);
                return;
            }


            String[] perms1 = {Manifest.permission.CAMERA};
            if (!EasyPermissions.hasPermissions(this, perms1)) {
                EasyPermissions.requestPermissions(this, "需要访问相机权限", com.yaoxin.appbase.net.Constant.RC_PHOTO_CAMERA_PERM, perms1);
                return;
            }
//      ActivityOptionsCompat optionsCompat = ActivityOptionsCompat.makeCustomAnimation(this, R.anim.in, R.anim.out);
//      Intent intent = new Intent(this, QRCodeScanActivity.class);
//      ActivityCompat.startActivityForResult(this, intent, REQUEST_CODE_SCAN, optionsCompat.toBundle());

            Intent intent = new Intent(this, CaptureActivity.class);
            startActivityForResult(intent, REQUEST_CODE_SCAN);
        } else if ("gotoCreate".equals(event.getTag())) {
            XKitRouter.withKey(com.yaoxin.appbase.net.Constant.FunSelected_User_ActivityKey)
                    .withContext(this)
                    .withParam("type", "1")
                    .navigate();

        } else if ("login_out".equals(event.getTag())) {
            IMUtil.loginOut(this);
        } else if ("add_friend".equals(event.getTag())) {
            XKitRouter.withKey(RouterConstant.PATH_FUN_ADD_FRIEND_PAGE).withContext(this).navigate();
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && data != null) {
            Context that = this;
            switch (requestCode) {
                case REQUEST_CODE_SCAN:
//          String result = CameraScan.parseScanResult(data);
                    String content = data.getStringExtra(CODED_CONTENT);
                    String result = content;
                    if (result != null) {
                        HttpUtil.api8444().friends_search(result)
                                .enqueue(new CommonCallback<NetData>() {
                                    @Override
                                    public void Successful(Call<NetData> call, Response<NetData> response, NetData body) {
                                        UserBean userInfo = new Gson().fromJson(body.data.toString(), UserBean.class);
                                        HashMap map = new HashMap();
                                        map.put("user", new Gson().toJson(userInfo));
                                        FunAddFriendVerifyActivity.start(FunAddFriendVerifyActivity.class, that, map);
                                    }

                                    @Override
                                    public void Failure(Call<NetData> call, Throwable t) {

                                    }
                                });
                    }
                    ToastUtils.toastMsg(result);
                    break;
            }

        }
    }

}
