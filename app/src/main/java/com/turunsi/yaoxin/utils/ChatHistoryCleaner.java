// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.turunsi.yaoxin.utils;

import android.os.Handler;
import android.os.Looper;

import com.netease.nimlib.sdk.NIMClient;
import com.netease.nimlib.sdk.msg.MsgService;
import com.netease.nimlib.sdk.msg.constant.SessionTypeEnum;
import com.netease.nimlib.sdk.msg.model.RecentContact;
import com.netease.yunxin.kit.alog.ALog;
import com.netease.yunxin.kit.corekit.im.IMKitClient;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 群聊本地记录只保留近 3 天；私聊不清空。
 * 使用 NIM MsgService.deleteRangeHistory 按会话删除指定时间段的本地消息。
 */
public final class ChatHistoryCleaner {

    private static final String TAG = "ChatHistoryCleaner";
    private static final long DAY_MS = 24 * 60 * 60 * 1000L;
    private static final int RETENTION_DAYS = 3;
    private static final long RETRY_INTERVAL_MS = 3000L;
    private static final int MAX_RETRY = 8;

    /** 成功清理后置 true；未登录跳过时保持 false，允许重试 */
    private static final AtomicBoolean sCleanedThisProcess = new AtomicBoolean(false);
    private static final AtomicBoolean sRunning = new AtomicBoolean(false);
    private static final AtomicInteger sRetryCount = new AtomicInteger(0);
    private static final Handler sMainHandler = new Handler(Looper.getMainLooper());

    private ChatHistoryCleaner() {}

    /**
     * 尝试清理。同一进程成功一次后不再重复；未登录时不占「已清理」标记，可重试。
     */
    public static void clearOldHistoryIfNeeded() {
        if (sCleanedThisProcess.get()) {
            return;
        }
        if (!sRunning.compareAndSet(false, true)) {
            return;
        }
        new Thread(
                        () -> {
                            try {
                                String account = IMKitClient.account();
                                if (account == null || account.isEmpty()) {
                                    ALog.d(Constant.PROJECT_TAG, TAG, "clearOldHistory: skip, not logged in");
                                    scheduleRetry();
                                    return;
                                }
                                long endTime = System.currentTimeMillis() - RETENTION_DAYS * DAY_MS;
                                List<RecentContact> recentContacts =
                                        NIMClient.getService(MsgService.class).queryRecentContactsBlock();
                                if (recentContacts == null || recentContacts.isEmpty()) {
                                    ALog.d(Constant.PROJECT_TAG, TAG, "clearOldHistory: no sessions");
                                    // 已登录但还没会话列表时也算完成，避免空转；有新群再进群时 ViewModel 会再删
                                    sCleanedThisProcess.set(true);
                                    return;
                                }
                                MsgService msgService = NIMClient.getService(MsgService.class);
                                int cleared = 0;
                                for (RecentContact recent : recentContacts) {
                                    try {
                                        // 只清群聊，私聊保留全部本地记录
                                        if (recent.getSessionType() != SessionTypeEnum.Team) {
                                            continue;
                                        }
                                        String contactId = recent.getContactId();
                                        // 开区间 (startTime, endTime)：startTime 用 -1，避免边界漏删
                                        msgService.deleteRangeHistory(
                                                contactId, SessionTypeEnum.Team, -1L, endTime);
                                        cleared++;
                                        ALog.d(
                                                Constant.PROJECT_TAG,
                                                TAG,
                                                "clearOldHistory: Team " + contactId);
                                    } catch (Exception e) {
                                        ALog.e(
                                                Constant.PROJECT_TAG,
                                                TAG,
                                                "clearOldHistory exception: " + e.getMessage());
                                    }
                                }
                                sCleanedThisProcess.set(true);
                                ALog.d(
                                        Constant.PROJECT_TAG,
                                        TAG,
                                        "clearOldHistory: done, teams=" + cleared);
                            } catch (Exception e) {
                                ALog.e(Constant.PROJECT_TAG, TAG, "clearOldHistory: " + e.getMessage());
                                scheduleRetry();
                            } finally {
                                sRunning.set(false);
                            }
                        },
                        "ChatHistoryCleaner")
                .start();
    }

    /** Application / 登录成功后调用：未成功则延迟重试。 */
    public static void scheduleClear() {
        clearOldHistoryIfNeeded();
    }

    private static void scheduleRetry() {
        int n = sRetryCount.incrementAndGet();
        if (n > MAX_RETRY || sCleanedThisProcess.get()) {
            return;
        }
        sMainHandler.postDelayed(ChatHistoryCleaner::clearOldHistoryIfNeeded, RETRY_INTERVAL_MS);
    }
}
