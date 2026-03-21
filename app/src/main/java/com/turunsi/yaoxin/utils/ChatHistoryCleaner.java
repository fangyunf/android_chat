// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.turunsi.yaoxin.utils;

import com.netease.nimlib.sdk.NIMClient;
import com.netease.nimlib.sdk.msg.MsgService;
import com.netease.nimlib.sdk.msg.constant.SessionTypeEnum;
import com.netease.nimlib.sdk.msg.model.RecentContact;
import com.netease.yunxin.kit.alog.ALog;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 每次打开 app 时检查并清除 7 天前的本地聊天记录。
 * 使用 NIM MsgService.deleteRangeHistory 按会话删除指定时间段的本地消息。
 */
public final class ChatHistoryCleaner {

    private static final String TAG = "ChatHistoryCleaner";
    private static final long DAY_MS = 24 * 60 * 60 * 1000L;
    private static final int RETENTION_DAYS = 3;

    private static final AtomicBoolean sCleanedThisProcess = new AtomicBoolean(false);

    /**
     * 在后台线程执行一次清理：删除所有会话中 7 天前的本地消息。
     * 同一进程内多次调用只会实际执行一次。
     */
    public static void clearOldHistoryIfNeeded() {
        if (!sCleanedThisProcess.compareAndSet(false, true)) {
            return;
        }
        new Thread(() -> {
            try {
                String account = com.netease.yunxin.kit.corekit.im.IMKitClient.account();
                if (account == null || account.isEmpty()) {
                    ALog.d(Constant.PROJECT_TAG, TAG, "clearOldHistory: skip, not logged in");
                    return;
                }
                long endTime = System.currentTimeMillis() - RETENTION_DAYS * DAY_MS;
                List<RecentContact> recentContacts = NIMClient.getService(MsgService.class).queryRecentContactsBlock();
                if (recentContacts == null || recentContacts.isEmpty()) {
                    ALog.d(Constant.PROJECT_TAG, TAG, "clearOldHistory: no sessions");
                    return;
                }
                MsgService msgService = NIMClient.getService(MsgService.class);
                for (RecentContact recent : recentContacts) {
                    try {
                        String contactId = recent.getContactId();
                        SessionTypeEnum sessionType = recent.getSessionType();
                        // 删除该会话中 [0, endTime) 时间段的本地消息，即 7 天前的消息
                        msgService.deleteRangeHistory(contactId, sessionType, 0, endTime);
                        ALog.d(Constant.PROJECT_TAG, TAG, "clearOldHistory: " + sessionType + " " + contactId);
                    } catch (Exception e) {
                        ALog.e(Constant.PROJECT_TAG, TAG, "clearOldHistory exception: " + e.getMessage());
                    }
                }
                ALog.d(Constant.PROJECT_TAG, TAG, "clearOldHistory: done");
            } catch (Exception e) {
                ALog.e(Constant.PROJECT_TAG, TAG, "clearOldHistory: " + e.getMessage());
                sCleanedThisProcess.set(false);
            }
        }, "ChatHistoryCleaner").start();
    }
}
