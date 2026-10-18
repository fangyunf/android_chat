package com.netease.yunxin.kit.chatkit.ui.fun.fan;

import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.netease.yunxin.kit.chatkit.repo.ContactRepo;
import com.netease.yunxin.kit.corekit.im.model.FriendVerifyType;
import com.netease.yunxin.kit.corekit.im.provider.FetchCallback;
import com.yaoxin.appbase.model.GroupInfoBean;
import com.yaoxin.appbase.model.NetData;
import com.yaoxin.appbase.model.RegisterBean;
import com.yaoxin.appbase.net.HttpUtil;
import com.yaoxin.appbase.utils.AESUtil;
import com.yaoxin.appbase.utils.DataUtil;
import com.yaoxin.appbase.utils.ToastUtils;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/** 顺序加好友队列（对齐 qiangbao FanAddQueue）。 */
public class FanAddQueue {

    public interface Listener {
        void onProgress(int current, int total, int countdownSec, String status);

        void onFinished();
    }

    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private List<FanUser> queue = new ArrayList<>();
    private int index;
    private Listener listener;
    private String greeting = "加我通过下";
    private int intervalSec = 5;
    private volatile boolean running;
    private final Runnable countdownTick =
            new Runnable() {
                @Override
                public void run() {
                    if (!running) {
                        return;
                    }
                    if (countdownLeft > 0) {
                        countdownLeft--;
                        notifyProgress(
                                index + 1,
                                queue.size(),
                                countdownLeft,
                                "下次添加 " + countdownLeft + "s（平台限速）");
                        if (countdownLeft > 0) {
                            mainHandler.postDelayed(this, 1000);
                        } else {
                            processNext();
                        }
                    }
                }
            };
    private int countdownLeft;

    public boolean isRunning() {
        return running;
    }

    public void start(List<FanUser> selected, String greeting, int intervalSec, Listener listener) {
        if (running) {
            return;
        }
        if (selected == null || selected.isEmpty()) {
            return;
        }
        this.queue = new ArrayList<>(selected);
        this.index = 0;
        this.greeting = TextUtils.isEmpty(greeting) ? "加我通过下" : greeting;
        this.intervalSec = Math.max(5, intervalSec);
        this.listener = listener;
        running = true;
        processNext();
    }

    public void stop() {
        running = false;
        mainHandler.removeCallbacks(countdownTick);
        listener = null;
    }

    private void processNext() {
        if (!running) {
            return;
        }
        if (index >= queue.size()) {
            finish();
            return;
        }
        FanUser user = queue.get(index);
        int total = queue.size();
        int current = index + 1;
        notifyProgress(current, total, 0, "添加中：" + current + "/" + total);

        if (isAlreadyFriend(user)) {
            index++;
            scheduleInterval();
            return;
        }

        RegisterBean bean = new RegisterBean();
        bean.memberCode = user.userId;
        bean.msg = greeting;
        HttpUtil.apiW()
                .friends_addFriends(bean)
                .enqueue(
                        new Callback<NetData>() {
                            @Override
                            public void onResponse(
                                    @NonNull Call<NetData> call, @NonNull Response<NetData> response) {
                                if (!running) {
                                    return;
                                }
                                if (isBizOk(response)) {
                                    advance();
                                    return;
                                }
                                // 业务号失败时改走云信账号（对齐 qiangbao addFriendIm）
                                fallbackImAdd(user);
                            }

                            @Override
                            public void onFailure(@NonNull Call<NetData> call, @NonNull Throwable t) {
                                if (!running) {
                                    return;
                                }
                                fallbackImAdd(user);
                            }
                        });
    }

    /** code=200 即成功；解密失败不算失败，避免一点开始就崩。 */
    private static boolean isBizOk(Response<NetData> response) {
        if (response == null || !response.isSuccessful() || response.body() == null) {
            return false;
        }
        NetData netData = response.body();
        if (netData.code != 200) {
            return false;
        }
        try {
            if (netData.data != null) {
                String raw = netData.data.toString();
                if (!TextUtils.isEmpty(raw)) {
                    netData.data = AESUtil.aseDecrypt(raw);
                }
            }
        } catch (Exception ignored) {
        }
        return true;
    }

    private void fallbackImAdd(FanUser user) {
        String imId =
                user != null && !TextUtils.isEmpty(user.cloudId) ? user.cloudId.trim() : "";
        if (TextUtils.isEmpty(imId) && user != null && !TextUtils.isEmpty(user.userId)) {
            imId = user.userId.trim();
        }
        if (TextUtils.isEmpty(imId)) {
            advance();
            return;
        }
        try {
            ContactRepo.addFriend(
                    imId,
                    FriendVerifyType.VerifyRequest,
                    new FetchCallback<Void>() {
                        @Override
                        public void onSuccess(@Nullable Void param) {
                            mainHandler.post(FanAddQueue.this::advance);
                        }

                        @Override
                        public void onFailed(int code) {
                            mainHandler.post(FanAddQueue.this::advance);
                        }

                        @Override
                        public void onException(@Nullable Throwable exception) {
                            mainHandler.post(FanAddQueue.this::advance);
                        }
                    });
        } catch (Exception ignored) {
            advance();
        }
    }

    private void advance() {
        if (!running) {
            return;
        }
        index++;
        scheduleInterval();
    }

    private void scheduleInterval() {
        if (!running) {
            return;
        }
        if (index >= queue.size()) {
            finish();
            return;
        }
        countdownLeft = intervalSec;
        notifyProgress(index, queue.size(), countdownLeft, "下次添加 " + countdownLeft + "s（平台限速）");
        mainHandler.removeCallbacks(countdownTick);
        mainHandler.postDelayed(countdownTick, 1000);
    }

    private void finish() {
        running = false;
        mainHandler.removeCallbacks(countdownTick);
        Listener l = listener;
        listener = null;
        if (l != null) {
            mainHandler.post(l::onFinished);
        }
        ToastUtils.toastMsg("添加好友已结束，到通讯录去查看吧，有可能有延迟...");
    }

    private void notifyProgress(int current, int total, int countdown, String status) {
        Listener l = listener;
        if (l == null) {
            return;
        }
        mainHandler.post(() -> l.onProgress(current, total, countdown, status));
    }

    private static boolean isAlreadyFriend(FanUser user) {
        List<GroupInfoBean> friends = DataUtil.getFriendInfoList();
        if (friends == null || friends.isEmpty() || user == null) {
            return false;
        }
        String uid = user.userId == null ? "" : user.userId.trim();
        String cloud = user.cloudId == null ? "" : user.cloudId.trim();
        for (GroupInfoBean f : friends) {
            if (f == null) {
                continue;
            }
            if (!TextUtils.isEmpty(uid)
                    && (uid.equals(f.memberCode) || uid.equals(f.userId))) {
                return true;
            }
            if (!TextUtils.isEmpty(cloud)
                    && (cloud.equals(f.userId) || cloud.equals(f.memberCode))) {
                return true;
            }
        }
        return false;
    }
}
