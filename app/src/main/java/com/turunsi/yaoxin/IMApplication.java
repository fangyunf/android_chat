// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.turunsi.yaoxin;

import static com.yaoxin.appbase.net.Constant.AccountDetailActivityKey;
import static com.yaoxin.appbase.net.Constant.RealName_Router;

import android.app.Activity;
import android.content.Context;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.multidex.MultiDexApplication;

import com.google.gson.Gson;
import com.heytap.msp.push.HeytapPushManager;
import com.huawei.hms.support.common.ActivityMgr;
import com.netease.nimlib.sdk.NIMClient;
import com.netease.nimlib.sdk.Observer;
import com.netease.nimlib.sdk.RequestCallbackWrapper;
import com.netease.nimlib.sdk.ResponseCode;
import com.netease.nimlib.sdk.SDKOptions;
import com.netease.nimlib.sdk.msg.MsgService;
import com.netease.nimlib.sdk.msg.MsgServiceObserve;
import com.netease.nimlib.sdk.msg.constant.SessionTypeEnum;
import com.netease.nimlib.sdk.msg.model.CustomNotification;
import com.netease.nimlib.sdk.msg.model.IMMessage;
import com.netease.nimlib.sdk.msg.model.RevokeMsgNotification;
import com.netease.nimlib.sdk.msg.model.ShowNotificationWhenRevokeFilter;
import com.netease.nimlib.sdk.team.TeamService;
import com.netease.nimlib.sdk.team.constant.TeamMessageNotifyTypeEnum;
import com.netease.nimlib.sdk.team.model.IMMessageFilter;
import com.netease.yunxin.kit.chatkit.model.TeamWithCurrentMember;
import com.netease.yunxin.kit.chatkit.repo.ConversationRepo;
import com.netease.yunxin.kit.chatkit.repo.TeamRepo;
import com.netease.yunxin.kit.corekit.im.provider.FetchCallback;
import com.netease.yunxin.kit.teamkit.ui.fun.activity.FunTeamMemberListActivity;
import com.netease.yunxin.kit.teamkit.ui.normal.activity.TeamMemberListActivity;
import com.orhanobut.hawk.Hawk;
import com.scwang.smart.refresh.footer.BallPulseFooter;
import com.scwang.smart.refresh.header.MaterialHeader;
import com.scwang.smart.refresh.layout.SmartRefreshLayout;
import com.scwang.smart.refresh.layout.api.RefreshFooter;
import com.scwang.smart.refresh.layout.api.RefreshHeader;
import com.scwang.smart.refresh.layout.api.RefreshLayout;
import com.scwang.smart.refresh.layout.listener.DefaultRefreshFooterCreator;
import com.scwang.smart.refresh.layout.listener.DefaultRefreshHeaderCreator;
import com.tencent.bugly.crashreport.CrashReport;
import com.turunsi.yaoxin.crash.AppCrashHandler;
import com.turunsi.yaoxin.login.LoginActivity;
import com.turunsi.yaoxin.login.OtherPlaceLoginActivity;
import com.turunsi.yaoxin.login.RealNameSetActivity;
import com.turunsi.yaoxin.main.MainActivity;
import com.turunsi.yaoxin.main.conversation.XiaoZhuShouActivity;
import com.turunsi.yaoxin.main.mine.MineInfoActivity;
import com.turunsi.yaoxin.main.mine.account.AccountDetailActivity;
import com.turunsi.yaoxin.main.mine.collection.CollectionListActivity;
import com.turunsi.yaoxin.main.mine.fuhao.BuyGroupFeatureActivity;
import com.turunsi.yaoxin.push.PushMessageHandler;
import com.turunsi.yaoxin.register.ForgetPwdActivity;
import com.turunsi.yaoxin.register.RegisterActivity;
import com.turunsi.yaoxin.splash.SplashActivity;
import com.turunsi.yaoxin.utils.Constant;
import com.turunsi.yaoxin.utils.DataUtils;
import com.turunsi.yaoxin.welcome.WelcomeActivity;
import com.netease.yunxin.kit.alog.ALog;
import com.netease.yunxin.kit.corekit.im.IMKitClient;
import com.netease.yunxin.kit.corekit.im.repo.SettingRepo;
import com.netease.yunxin.kit.corekit.im.utils.IMKitUtils;
import com.netease.yunxin.kit.corekit.im.utils.RouterConstant;
import com.netease.yunxin.kit.corekit.route.XKitRouter;
import com.netease.yunxin.kit.locationkit.LocationKitClient;
import com.vivo.push.PushClient;
import com.vivo.push.util.VivoPushException;
import com.yaoxin.appbase.activity.BaseWebViewActivity;
import com.yaoxin.appbase.model.CustomMsgBean;
import com.yaoxin.appbase.utils.AppProxy;
import com.yaoxin.appbase.utils.BaseEvent;
import com.yaoxin.appbase.utils.DataUtil;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class IMApplication extends MultiDexApplication {

    private static final String TAG = "IMApplication";
    private static boolean coldStart = false;
    private static int foregroundActCount = 0;
    private Activity currentActivity;
    private MediaPlayer mediaPlayer;
    Vibrator vibrator;

    @Override
    public void onCreate() {
        super.onCreate();
        ALog.d(Constant.PROJECT_TAG, TAG, "onCreate");
        //app init
        registerActivityLifeCycle();
        AppCrashHandler.getInstance().initCrashHandler(this);
        Thread.setDefaultUncaughtExceptionHandler(AppCrashHandler.getInstance());

        initUIKit();
        // temp register for mine
        XKitRouter.registerRouter(RouterConstant.PATH_MINE_INFO_PAGE, MineInfoActivity.class);
        XKitRouter.registerRouter(AccountDetailActivityKey, AccountDetailActivity.class);
        XKitRouter.registerRouter(RealName_Router, RealNameSetActivity.class);
        XKitRouter.registerRouter(com.yaoxin.appbase.net.Constant.TeamMemberListActivity_Router, FunTeamMemberListActivity.class);
        XKitRouter.registerRouter(com.yaoxin.appbase.net.Constant.CollectionListActivityKey, CollectionListActivity.class);
        XKitRouter.registerRouter(com.yaoxin.appbase.net.Constant.BaseWebViewActivityKey, BaseWebViewActivity.class);
        XKitRouter.registerRouter(com.yaoxin.appbase.net.Constant.XiaoZhuShouActivityKey, XiaoZhuShouActivity.class);
        XKitRouter.registerRouter("BuyGroupFeatureActivity", BuyGroupFeatureActivity.class);
        AppProxy.getInstance().init(this)
                .setIsDebug(BuildConfig.DEBUG)
                .setVersionName(BuildConfig.VERSION_NAME);
        initThirdPart();
        mediaPlayer = MediaPlayer.create(this, R.raw.msg);
        vibrator = (Vibrator) this.getSystemService(Context.VIBRATOR_SERVICE);

        if (vibrator != null && vibrator.hasVibrator()) { // 检查设备是否支持震动
            // 创建一个震动效果，参数为震动时长（毫秒）
            VibrationEffect vibrationEffect = null;
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                vibrationEffect = VibrationEffect.createOneShot(500, VibrationEffect.DEFAULT_AMPLITUDE);
                vibrator.vibrate(vibrationEffect);
            }

            // 开始震动
        }
    }

    private void initThirdPart() {
        Hawk.init(this).build();
        SmartRefreshLayout.setDefaultRefreshHeaderCreator(new DefaultRefreshHeaderCreator() {
            @NonNull
            @Override
            public RefreshHeader createRefreshHeader(@NonNull Context context, @NonNull RefreshLayout layout) {
                layout.setEnableHeaderTranslationContent(false);////是否下拉Header的时候向下平移列表或者内容
                return new MaterialHeader(context).setColorSchemeColors(getResources().getColor(R.color.color_884EEF));//Material design 风格Header
            }
        });


        //设置全局的Footer构建器
        SmartRefreshLayout.setDefaultRefreshFooterCreator(new DefaultRefreshFooterCreator() {
            @NonNull
            @Override
            public RefreshFooter createRefreshFooter(@NonNull Context context, @NonNull RefreshLayout layout) {
                //指定为经典Footer，默认是 BallPulseFooter
                return new BallPulseFooter(context);
            }
        });
    }

    private void initUIKit() {
        SDKOptions options = NimSDKOptionConfig.getSDKOptions(this, DataUtils.readAppKey(this));
        IMKitClient.init(this, null, options);
        ALog.d(Constant.PROJECT_TAG, TAG, "initUIKit");

        if (IMKitUtils.isMainProcess(this)) {
            ALog.d(Constant.PROJECT_TAG, TAG, "initUIKit:isMainProcess");
            LocationKitClient.init(this);
            //huawei push
            ActivityMgr.INST.init(this);
            //oppo push
            HeytapPushManager.init(this, true);
            try {
                //vivo push
                PushClient.getInstance(this).initialize();
            } catch (VivoPushException e) {
                e.printStackTrace();
            }
            IMKitClient.toggleNotification(SettingRepo.isPushNotify());
            IMKitClient.registerMixPushMessageHandler(new PushMessageHandler());
            // 在 Application启动时注册，保证漫游、离线消息也能够回调此过滤器进行过滤。注意，过滤器的实现不要有耗时操作。
            NIMClient.getService(MsgServiceObserve.class)
                    .observeCustomNotification(new Observer<CustomNotification>() {
                        @Override
                        public void onEvent(CustomNotification notification) {
                            // 处理接收到的自定义系统通知
                            String content = notification.getContent();
                            if (content != null) {
                                CustomMsgBean msgBean = new Gson().fromJson(content, CustomMsgBean.class);
                                if (msgBean.type == 525) {
                                    BaseEvent baseEvent = new BaseEvent("egg_open_notice");
                                    baseEvent.customMsgBean = msgBean;
                                    EventBus.getDefault().post(baseEvent);
                                }
                            }
                            // 根据需要处理通知内容
                        }
                    }, true);
            NIMClient.getService(MsgService.class).registerIMMessageFilter(new IMMessageFilter() {
                @Override
                public boolean shouldIgnore(IMMessage message) {
                    if (message.getAttachStr() != null) {
                        try {
                            CustomMsgBean msgBean = new Gson().fromJson(message.getAttachStr(), CustomMsgBean.class);
                            msgBean.result = new Gson().fromJson(msgBean.data, CustomMsgBean.class);
                            if (msgBean.type == 21 || msgBean.type == 22 || msgBean.type == 23) {
                                if (DataUtil.getUserid().equals(msgBean.result.toUserId) || DataUtil.getUserid().equals(msgBean.result.fromUserId) || msgBean.result.adminIds.contains(DataUtil.getUserid())) {
                                    return false;
                                }
                                return true;
                            }
                            return false;
                        } catch (Exception e) {

                        }
                    }

                    if (message.getContent() != null && message.getContent().startsWith("{")) {
                        try {
                            CustomMsgBean msgBean = new Gson().fromJson(message.getContent(), CustomMsgBean.class);

                            if (msgBean.sendUserId != null && msgBean.sendUserName != null && msgBean.receiveUserName != null && msgBean.receiveUserId != null) {
                                {
                                    if (msgBean.sendUserId.equals(DataUtil.getUserid()) || msgBean.receiveUserId.equals(DataUtil.getUserid())) {
                                        return false;
                                    }
                                    return true;

                                }
                            }

                        } catch (Exception e) {

                        }
//                        if (message.getContent().contains("receiveUserId") && message.getContent().contains("receiveUserName")) {
//                            return true;
//                        }
                    }
                    String ringSoud = DataUtil.getStringValue("ring_soud");
                    String shakeSoud = DataUtil.getStringValue("shake_soud");
                    if ("1".equals(ringSoud)) {
                        // 获取会话 ID 和类型
                        String sessionId = message.getSessionId();
                        SessionTypeEnum sessionType = message.getSessionType();

                        if (sessionType == SessionTypeEnum.P2P) { // 单聊会话

                            boolean isNotice = ConversationRepo.isNotify(sessionId, SessionTypeEnum.P2P);

                            if (isNotice) {
                                mediaPlayer.start();
                            }
                        } else if (sessionType == SessionTypeEnum.Team) { // 群聊会话
                            TeamRepo.queryTeamWithMember(
                                    sessionId,
                                    Objects.requireNonNull(IMKitClient.account()),
                                    new FetchCallback<TeamWithCurrentMember>() {
                                        @Override
                                        public void onSuccess(@Nullable TeamWithCurrentMember param) {
                                            boolean isMute = param.getTeam().getMessageNotifyType() == TeamMessageNotifyTypeEnum.Mute;
                                            if (!isMute) {
                                                mediaPlayer.start();
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
                    }
                    if ("1".equals(shakeSoud)) {
                        // 获取会话 ID 和类型
                        String sessionId = message.getSessionId();
                        SessionTypeEnum sessionType = message.getSessionType();

                        if (sessionType == SessionTypeEnum.P2P) { // 单聊会话

                            boolean isNotice = ConversationRepo.isNotify(sessionId, SessionTypeEnum.P2P);

                            if (isNotice) {
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                    vibrator.vibrate(VibrationEffect.createOneShot(500, VibrationEffect.DEFAULT_AMPLITUDE));
                                } else {
                                    vibrator.vibrate(500); // 简单震动
                                }
                            }
                        } else if (sessionType == SessionTypeEnum.Team) { // 群聊会话
                            TeamRepo.queryTeamWithMember(
                                    sessionId,
                                    Objects.requireNonNull(IMKitClient.account()),
                                    new FetchCallback<TeamWithCurrentMember>() {
                                        @Override
                                        public void onSuccess(@Nullable TeamWithCurrentMember param) {
                                            boolean isMute = param.getTeam().getMessageNotifyType() == TeamMessageNotifyTypeEnum.Mute;
                                            if (!isMute) {

                                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                                    vibrator.vibrate(VibrationEffect.createOneShot(500, VibrationEffect.DEFAULT_AMPLITUDE));
                                                } else {
                                                    vibrator.vibrate(500); // 简单震动
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


                    }
                    return false; // 不过滤
                }
            });

            CrashReport.initCrashReport(getApplicationContext(), "54b5046e41", false);
        }
    }

    private final List<Activity> activities = new ArrayList<>();

    //用于系统杀死应用之后，系统恢复应用，可能存在没有登录的异常
    //此处如果在没有登录的情况下，其他页面打开的时候进行finish();除了MainActivity
    //MainActivity启动进行登录检测，如果没有登录进行登录操作
    private void registerActivityLifeCycle() {
        registerActivityLifecycleCallbacks(
                new ActivityLifecycleCallbacks() {
                    @Override
                    public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
                        if (TextUtils.isEmpty(IMKitClient.account())
                                && !(activity instanceof MainActivity || activity instanceof SplashActivity || activity instanceof LoginActivity || activity instanceof RegisterActivity || activity instanceof ForgetPwdActivity || activity instanceof WelcomeActivity || activity instanceof RealNameSetActivity || activity instanceof BaseWebViewActivity || activity instanceof OtherPlaceLoginActivity)
                                && !coldStart) {
                            activity.finish();
                        } else {
                            activities.add(activity);
                        }
                    }

                    @Override
                    public void onActivityStarted(Activity activity) {
                        foregroundActCount++;
                        currentActivity = activity;
                    }

                    @Override
                    public void onActivityResumed(Activity activity) {
                        currentActivity = activity;
                    }

                    @Override
                    public void onActivityPaused(Activity activity) {
                    }

                    @Override
                    public void onActivityStopped(Activity activity) {
                        if (currentActivity == activity) {
                            currentActivity = null;
                        }
                        foregroundActCount--;
                    }

                    @Override
                    public void onActivitySaveInstanceState(Activity activity, Bundle outState) {
                    }

                    @Override
                    public void onActivityDestroyed(Activity activity) {
                        if (activities.isEmpty()) {
                            return;
                        }
                        activities.remove(activity);
                    }
                });
    }

    public void clearActivity(Activity exclude) {
        for (int i = 0; i < activities.size(); i++) {
            if (activities.get(i) != null && activities.get(i) != exclude) {
                activities.get(i).finish();
            }
        }
    }

    public static void setColdStart(boolean value) {
        coldStart = value;
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
        foregroundActCount = 0;
    }

    public static int getForegroundActCount() {
        return foregroundActCount;
    }

    public Activity getCurrentActivity() {
        return currentActivity;
    }
}
