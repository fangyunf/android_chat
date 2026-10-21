package com.netease.yunxin.kit.chatkit.ui.fun.fan;

import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;

import androidx.annotation.NonNull;

import com.google.gson.Gson;
import com.yaoxin.appbase.model.GroupInfoBean;
import com.yaoxin.appbase.model.NetData;
import com.yaoxin.appbase.model.RegisterBean;
import com.yaoxin.appbase.model.UserBean;
import com.yaoxin.appbase.net.HttpUtil;
import com.yaoxin.appbase.utils.AESUtil;
import com.yaoxin.appbase.utils.DataUtil;
import com.yaoxin.appbase.utils.ToastUtils;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * 顺序加好友队列。
 * 流程：friends/searchByUserIdAOS(userId) → 取 memberCode → friends/addFriends(memberCode)。
 */
public class FanAddQueue {

    public interface Listener {
        void onProgress(int current, int total, int countdownSec, String status);

        void onFinished();
    }

    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private final Gson gson = new Gson();
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
            advance();
            return;
        }

        // searchByUserIdAOS(userId) → 临时拿到 memberCode → addFriends(memberCode)
        String lookupUserId = user.lookupUserId();
        if (TextUtils.isEmpty(lookupUserId)) {
            advance();
            return;
        }

        RegisterBean searchBean = new RegisterBean();
        searchBean.userId = lookupUserId;
        HttpUtil.apiW()
                .friends_searchByUserIdAOS(searchBean)
                .enqueue(
                        new Callback<NetData>() {
                            @Override
                            public void onResponse(
                                    @NonNull Call<NetData> call, @NonNull Response<NetData> response) {
                                if (!running) {
                                    return;
                                }
                                String memberCode = parseMemberCode(response);
                                if (TextUtils.isEmpty(memberCode)) {
                                    advance();
                                    return;
                                }
                                addFriendByMemberCode(memberCode);
                            }

                            @Override
                            public void onFailure(@NonNull Call<NetData> call, @NonNull Throwable t) {
                                if (!running) {
                                    return;
                                }
                                advance();
                            }
                        });
    }

    private String parseMemberCode(Response<NetData> response) {
        if (response == null || !response.isSuccessful() || response.body() == null) {
            return "";
        }
        NetData netData = response.body();
        if (netData.code != 200 || netData.data == null) {
            return "";
        }
        String raw = netData.data.toString();
        if (TextUtils.isEmpty(raw)) {
            return "";
        }
        try {
            raw = AESUtil.aseDecrypt(raw);
        } catch (Exception ignored) {
            // 已是明文则继续解析
        }
        try {
            UserBean userBean = gson.fromJson(raw, UserBean.class);
            if (userBean == null) {
                return "";
            }
            if (!TextUtils.isEmpty(userBean.memberCode)) {
                return userBean.memberCode.trim();
            }
            if (!TextUtils.isEmpty(userBean.userMemberCode)) {
                return userBean.userMemberCode.trim();
            }
        } catch (Exception ignored) {
        }
        return "";
    }

    private void addFriendByMemberCode(String memberCode) {
        RegisterBean bean = new RegisterBean();
        bean.memberCode = memberCode;
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
                                // 成功/失败都继续下一位，避免卡队列
                                advance();
                            }

                            @Override
                            public void onFailure(@NonNull Call<NetData> call, @NonNull Throwable t) {
                                if (!running) {
                                    return;
                                }
                                advance();
                            }
                        });
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
        String uid = user.lookupUserId();
        if (TextUtils.isEmpty(uid)) {
            return false;
        }
        for (GroupInfoBean f : friends) {
            if (f == null) {
                continue;
            }
            if (uid.equals(f.userId)) {
                return true;
            }
        }
        return false;
    }
}
