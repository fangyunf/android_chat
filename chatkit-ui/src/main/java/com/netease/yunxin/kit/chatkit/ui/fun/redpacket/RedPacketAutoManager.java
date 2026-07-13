package com.netease.yunxin.kit.chatkit.ui.fun.redpacket;

import android.app.Activity;
import android.app.Application;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.gson.Gson;
import com.netease.nimlib.sdk.NIMClient;
import com.netease.nimlib.sdk.msg.MsgService;
import com.netease.nimlib.sdk.msg.model.IMMessage;
import com.yaoxin.appbase.model.CustomMsgBean;
import com.yaoxin.appbase.model.NetData;
import com.yaoxin.appbase.model.RegisterBean;
import com.yaoxin.appbase.net.HttpUtil;
import com.yaoxin.appbase.utils.AESUtil;
import com.yaoxin.appbase.utils.AppProxy;
import com.yaoxin.appbase.utils.BaseEvent;
import com.yaoxin.appbase.utils.DataUtil;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RedPacketAutoManager {

    public static final String ACTION_STATE_CHANGED = "com.netease.yunxin.redpacket.STATE_CHANGED";
    public static final String ACTION_CLAIMED = "com.netease.yunxin.redpacket.CLAIMED";
    public static final String EXTRA_MESSAGE_UUID = "message_uuid";

    private static final int MAX_GRABBED_IDS = 512;
    private static final long WARM_UP_DEBOUNCE_MS = 30_000L;

    private static RedPacketAutoManager instance;

    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private final RedPacketAutoConfig config = new RedPacketAutoConfig();
    private final LinkedHashSet<String> grabbedIds = new LinkedHashSet<>();
    private final Set<String> grabbingIds = new HashSet<>();
    private final Gson gson = new Gson();

    private boolean initialized;
    private boolean appInForeground;
    private int startedActivityCount;
    private boolean autoSendActive;
    private boolean grabExclusiveActive;
    private boolean grabLuckyActive;
    private boolean chatInterfaceVisible;
    private boolean sendInFlight;
    private String activeSessionId = "";
    private String autoSendGroupId = "";
    private String cachedMyUserId = "";
    private double cachedGrabDelay;
    private int grabGeneration;
    private long lastWarmUpAt;
    private RedPacketAutoConfig.SendSnapshot sendSnapshot;
    private final Runnable autoSendRunnable = this::sendOnce;
    private final Runnable refreshClaimRunnable = this::dispatchClaimRefresh;

    private final List<String> pendingClaimUuids = new ArrayList<>();

    private RedPacketAutoManager() {}

    public static synchronized RedPacketAutoManager get() {
        if (instance == null) {
            instance = new RedPacketAutoManager();
        }
        return instance;
    }

    public void init(Application application) {
        if (initialized) {
            return;
        }
        initialized = true;
        application.registerActivityLifecycleCallbacks(new Application.ActivityLifecycleCallbacks() {
            @Override
            public void onActivityCreated(@NonNull Activity activity, @Nullable android.os.Bundle savedInstanceState) {}

            @Override
            public void onActivityStarted(@NonNull Activity activity) {
                startedActivityCount++;
                appInForeground = startedActivityCount > 0;
            }

            @Override
            public void onActivityResumed(@NonNull Activity activity) {}

            @Override
            public void onActivityPaused(@NonNull Activity activity) {}

            @Override
            public void onActivityStopped(@NonNull Activity activity) {
                startedActivityCount = Math.max(0, startedActivityCount - 1);
                appInForeground = startedActivityCount > 0;
            }

            @Override
            public void onActivitySaveInstanceState(@NonNull Activity activity, @NonNull android.os.Bundle outState) {}

            @Override
            public void onActivityDestroyed(@NonNull Activity activity) {}
        });
        VoiceAnnouncer.get();
    }

    public RedPacketAutoConfig getConfig() {
        return config;
    }

    public boolean isAutoSendActive() {
        return autoSendActive;
    }

    public boolean isGrabExclusiveActive() {
        return grabExclusiveActive;
    }

    public boolean isGrabLuckyActive() {
        return grabLuckyActive;
    }

    public void setChatInterfaceVisible(boolean visible, String sessionId) {
        chatInterfaceVisible = visible;
        if (!TextUtils.isEmpty(sessionId)) {
            activeSessionId = sessionId;
        }
        if (visible) {
            cachedGrabDelay = config.getGrabDelay();
            if (autoSendActive && !TextUtils.isEmpty(autoSendGroupId)) {
                scheduleNextSend();
            }
        } else {
            mainHandler.removeCallbacks(autoSendRunnable);
        }
        notifyStateChanged();
    }

    public void stopAll(@Nullable String sessionId) {
        mainHandler.removeCallbacks(autoSendRunnable);
        autoSendActive = false;
        grabLuckyActive = false;
        sendInFlight = false;
        grabGeneration++;
        if (sessionId != null) {
            if (TextUtils.equals(activeSessionId, sessionId)) {
                activeSessionId = "";
            }
            if (TextUtils.equals(autoSendGroupId, sessionId)) {
                autoSendGroupId = "";
            }
        }
        chatInterfaceVisible = false;
        notifyStateChanged();
    }

    public boolean toggleGrabExclusive() {
        if (!config.isGrabExclusiveEnabled()) {
            return false;
        }
        grabExclusiveActive = !grabExclusiveActive;
        if (grabExclusiveActive) {
            cachedMyUserId = DataUtil.getUserid();
            cachedGrabDelay = config.getGrabDelay();
            warmUpConnection();
        } else if (!grabLuckyActive) {
            clearGrabCaches();
        }
        notifyStateChanged();
        return true;
    }

    public boolean toggleGrabLucky(String groupId) {
        if (!config.isGrabLuckyEnabled()) {
            return false;
        }
        if (grabLuckyActive && TextUtils.equals(activeSessionId, groupId)) {
            grabLuckyActive = false;
            grabGeneration++;
            if (!autoSendActive) {
                activeSessionId = "";
            }
        } else {
            activeSessionId = groupId;
            grabLuckyActive = true;
            cachedMyUserId = DataUtil.getUserid();
            cachedGrabDelay = config.getGrabDelay();
            warmUpConnection();
        }
        notifyStateChanged();
        return true;
    }

    public boolean toggleAutoSend(String groupId) {
        if (!config.isSendConfigured()) {
            return false;
        }
        if (autoSendActive && TextUtils.equals(autoSendGroupId, groupId)) {
            stopAutoSend();
            return true;
        }
        autoSendGroupId = groupId;
        activeSessionId = groupId;
        autoSendActive = true;
        sendSnapshot = config.createSendSnapshot();
        scheduleNextSend();
        notifyStateChanged();
        return true;
    }

    public void stopAutoSend() {
        mainHandler.removeCallbacks(autoSendRunnable);
        autoSendActive = false;
        sendInFlight = false;
        autoSendGroupId = "";
        notifyStateChanged();
    }

    public void onGrabSettingsSaved() {
        cachedGrabDelay = config.getGrabDelay();
        if (!config.isGrabExclusiveEnabled() && grabExclusiveActive) {
            grabExclusiveActive = false;
            if (!grabLuckyActive) {
                clearGrabCaches();
            }
        }
        if (!config.isGrabLuckyEnabled() && grabLuckyActive) {
            grabLuckyActive = false;
            grabGeneration++;
        }
        notifyStateChanged();
    }

    public void handleGlobalExclusive(List<IMMessage> messages) {
        if (!appInForeground || !grabExclusiveActive || messages == null) {
            return;
        }
        for (IMMessage message : messages) {
            RedPacketAttachUtil.ParsedRedPacket parsed = RedPacketAttachUtil.parse(message);
            if (parsed == null || parsed.type != 21) {
                continue;
            }
            if (shouldSkip(parsed)) {
                continue;
            }
            scheduleGrab(parsed, true, 0);
        }
    }

    public void handleReceivedMessages(List<IMMessage> messages, String sessionId) {
        if (!appInForeground || !chatInterfaceVisible || !grabLuckyActive || messages == null) {
            return;
        }
        if (TextUtils.isEmpty(activeSessionId) || !TextUtils.equals(activeSessionId, sessionId)) {
            return;
        }
        for (IMMessage message : messages) {
            RedPacketAttachUtil.ParsedRedPacket parsed = RedPacketAttachUtil.parse(message);
            if (parsed == null || parsed.type != 23) {
                continue;
            }
            if (shouldSkip(parsed)) {
                continue;
            }
            scheduleGrab(parsed, false, grabGeneration);
        }
    }

    private boolean shouldSkip(RedPacketAttachUtil.ParsedRedPacket parsed) {
        if (TextUtils.isEmpty(parsed.redpacketId)) {
            return true;
        }
        String myId = getMyUserId();
        if (!TextUtils.isEmpty(parsed.fromUserId) && TextUtils.equals(parsed.fromUserId, myId)) {
            return true;
        }
        if (parsed.type == 21 && !TextUtils.isEmpty(parsed.toUserId) && !TextUtils.equals(parsed.toUserId, myId)) {
            return true;
        }
        if (parsed.claimed) {
            return true;
        }
        synchronized (this) {
            return grabbedIds.contains(parsed.redpacketId) || grabbingIds.contains(parsed.redpacketId);
        }
    }

    private void scheduleGrab(RedPacketAttachUtil.ParsedRedPacket parsed, boolean exclusive, int generation) {
        synchronized (this) {
            if (grabbingIds.contains(parsed.redpacketId)) {
                return;
            }
            grabbingIds.add(parsed.redpacketId);
        }
        long delayMs = (long) (cachedGrabDelay * 1000);
        Runnable task = () -> performGrab(parsed, exclusive, generation, 0);
        if (delayMs <= 0) {
            mainHandler.post(task);
        } else {
            mainHandler.postDelayed(task, delayMs);
        }
    }

    private void performGrab(RedPacketAttachUtil.ParsedRedPacket parsed, boolean exclusive, int generation, int retry) {
        if (!appInForeground) {
            finishGrabbing(parsed.redpacketId);
            return;
        }
        if (exclusive) {
            if (!grabExclusiveActive) {
                finishGrabbing(parsed.redpacketId);
                return;
            }
        } else {
            if (!grabLuckyActive || generation != grabGeneration || !chatInterfaceVisible) {
                finishGrabbing(parsed.redpacketId);
                return;
            }
        }
        RegisterBean bean = new RegisterBean();
        bean.redpacketId = parsed.redpacketId;
        Call<NetData> call = exclusive
                ? HttpUtil.apiW().red_reciveExclusiveRedpacket(bean)
                : HttpUtil.apiW().red_grab(bean);
        call.enqueue(new SilentCallback() {
            @Override
            protected void onSuccess(NetData body) {
                CustomMsgBean result = null;
                try {
                    if (body.data != null) {
                        result = gson.fromJson(body.data.toString(), CustomMsgBean.class);
                    }
                } catch (Exception ignored) {
                }
                markGrabbed(parsed.redpacketId);
                markMessageClaimed(parsed.message);
                if (exclusive) {
                    long amount = 0;
                    if (result != null && !TextUtils.isEmpty(result.reciveAmount)) {
                        try {
                            amount = Long.parseLong(result.reciveAmount);
                        } catch (Exception ignored) {
                        }
                    }
                    VoiceAnnouncer.get().announceExclusive(amount);
                }
                queueClaimRefresh(parsed.message.getUuid());
            }

            @Override
            protected void onFailed() {
                if (retry < 2) {
                    performGrab(parsed, exclusive, generation, retry + 1);
                } else {
                    finishGrabbing(parsed.redpacketId);
                }
            }
        });
    }

    private void scheduleNextSend() {
        mainHandler.removeCallbacks(autoSendRunnable);
        if (!autoSendActive || !chatInterfaceVisible || sendSnapshot == null) {
            return;
        }
        long delay = (long) (sendSnapshot.intervalSeconds * 1000);
        mainHandler.postDelayed(autoSendRunnable, Math.max(delay, 500));
    }

    private void sendOnce() {
        if (!autoSendActive || !chatInterfaceVisible || sendInFlight || sendSnapshot == null) {
            return;
        }
        if (TextUtils.isEmpty(autoSendGroupId)) {
            stopAutoSend();
            return;
        }
        int amount = config.randomAmountCents();
        int count = config.randomCount();
        if (amount <= 0 || count <= 0) {
            stopAutoSend();
            return;
        }
        sendInFlight = true;
        RegisterBean bean = new RegisterBean();
        bean.groupId = autoSendGroupId;
        bean.amount = amount;
        bean.num = count;
        bean.title = config.randomGreeting();
        bean.tradePassword = sendSnapshot.payPassword;
        HttpUtil.apiW().red_sendGroupRedpacket(bean).enqueue(new SilentCallback() {
            @Override
            protected void onSuccess(NetData body) {
                sendInFlight = false;
                scheduleNextSend();
            }

            @Override
            protected void onFailed() {
                sendInFlight = false;
                stopAutoSend();
            }
        });
    }

    private void markGrabbed(String redpacketId) {
        synchronized (this) {
            grabbingIds.remove(redpacketId);
            grabbedIds.add(redpacketId);
            while (grabbedIds.size() > MAX_GRABBED_IDS) {
                Iterator<String> it = grabbedIds.iterator();
                if (it.hasNext()) {
                    it.next();
                    it.remove();
                } else {
                    break;
                }
            }
        }
    }

    private void finishGrabbing(String redpacketId) {
        synchronized (this) {
            grabbingIds.remove(redpacketId);
        }
    }

    private void clearGrabCaches() {
        synchronized (this) {
            grabbedIds.clear();
            grabbingIds.clear();
        }
        cachedMyUserId = "";
    }

    private String getMyUserId() {
        if (TextUtils.isEmpty(cachedMyUserId)) {
            cachedMyUserId = DataUtil.getUserid();
        }
        return cachedMyUserId;
    }

    private void markMessageClaimed(IMMessage message) {
        if (message == null) {
            return;
        }
        HashMap<String, Object> map = new HashMap<>();
        map.put("userId", DataUtil.getUserid());
        map.put("hasDragDown", 1);
        message.setLocalExtension(map);
        NIMClient.getService(MsgService.class).updateIMMessage(message);
    }

    private void queueClaimRefresh(String uuid) {
        if (TextUtils.isEmpty(uuid)) {
            return;
        }
        synchronized (pendingClaimUuids) {
            pendingClaimUuids.add(uuid);
        }
        mainHandler.removeCallbacks(refreshClaimRunnable);
        mainHandler.postDelayed(refreshClaimRunnable, 200);
    }

    private void dispatchClaimRefresh() {
        List<String> copy;
        synchronized (pendingClaimUuids) {
            copy = new ArrayList<>(pendingClaimUuids);
            pendingClaimUuids.clear();
        }
        for (String uuid : copy) {
            BaseEvent event = new BaseEvent(ACTION_CLAIMED);
            event.put(EXTRA_MESSAGE_UUID, uuid);
            EventBus.getDefault().post(event);
        }
    }

    public void warmUpConnection() {
        long now = System.currentTimeMillis();
        if (now - lastWarmUpAt < WARM_UP_DEBOUNCE_MS) {
            return;
        }
        lastWarmUpAt = now;
        RegisterBean bean = new RegisterBean();
        HttpUtil.apiW().home_balance().enqueue(new SilentCallback() {
            @Override
            protected void onSuccess(NetData body) {}

            @Override
            protected void onFailed() {}
        });
    }

    private void notifyStateChanged() {
        EventBus.getDefault().post(new BaseEvent(ACTION_STATE_CHANGED));
    }

    private abstract static class SilentCallback implements Callback<NetData> {
        @Override
        public void onResponse(@NonNull Call<NetData> call, @NonNull Response<NetData> response) {
            if (!response.isSuccessful() || response.body() == null) {
                onFailed();
                return;
            }
            NetData netData = response.body();
            if (netData.code != 200) {
                onFailed();
                return;
            }
            try {
                if (netData.data != null) {
                    netData.data = AESUtil.aseDecrypt(netData.data.toString());
                }
            } catch (Exception e) {
                onFailed();
                return;
            }
            onSuccess(netData);
        }

        @Override
        public void onFailure(@NonNull Call<NetData> call, @NonNull Throwable t) {
            onFailed();
        }

        protected abstract void onSuccess(NetData body);

        protected abstract void onFailed();
    }
}
