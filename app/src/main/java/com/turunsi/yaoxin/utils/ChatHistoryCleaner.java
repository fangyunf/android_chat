// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.turunsi.yaoxin.utils;

import com.netease.yunxin.kit.alog.ALog;
import com.netease.yunxin.kit.corekit.im.IMKitClient;
import com.yaoxin.appbase.utils.ChatRetentionHelper;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * App 启动 / 登录后清理超过 {@link ChatRetentionHelper#RETENTION_DAYS} 天的本地聊天记录。
 */
public final class ChatHistoryCleaner {

    private static final String TAG = "ChatHistoryCleaner";

    private static final AtomicBoolean sRunning = new AtomicBoolean(false);

    public static void clearOldHistoryIfNeeded() {
        if (!sRunning.compareAndSet(false, true)) {
            return;
        }
        new Thread(() -> {
            try {
                String account = IMKitClient.account();
                if (account == null || account.isEmpty()) {
                    ALog.d(Constant.PROJECT_TAG, TAG, "clearOldHistory: skip, not logged in");
                    return;
                }
                ChatRetentionHelper.cleanAllRecentSessions();
                ALog.d(Constant.PROJECT_TAG, TAG, "clearOldHistory: done");
            } catch (Exception e) {
                ALog.e(Constant.PROJECT_TAG, TAG, "clearOldHistory: " + e.getMessage());
            } finally {
                sRunning.set(false);
            }
        }, "ChatHistoryCleaner").start();
    }
}
