package com.yaoxin.appbase.utils;

import androidx.annotation.Nullable;

import com.netease.nimlib.sdk.NIMClient;
import com.netease.nimlib.sdk.msg.MsgService;
import com.netease.nimlib.sdk.msg.constant.SessionTypeEnum;
import com.netease.nimlib.sdk.msg.model.GetMessageDirectionEnum;
import com.netease.nimlib.sdk.msg.model.GetMessagesDynamicallyParam;
import com.netease.nimlib.sdk.msg.model.IMMessage;
import com.netease.nimlib.sdk.msg.model.RecentContact;

import java.util.List;

/**
 * 聊天记录仅保留最近 {@link #RETENTION_DAYS} 天（本地删除 + 拉取窗口限制）。
 */
public final class ChatRetentionHelper {

    public static final int RETENTION_DAYS = 3;
    private static final long DAY_MS = 24 * 60 * 60 * 1000L;

    private ChatRetentionHelper() {
    }

    public static long cutoffTimeMs() {
        return System.currentTimeMillis() - RETENTION_DAYS * DAY_MS;
    }

    public static boolean isRetained(long messageTimeMs) {
        return messageTimeMs >= cutoffTimeMs();
    }

    /** 删除单个会话 cutoff 之前的本地消息 */
    public static void cleanSession(String sessionId, SessionTypeEnum sessionType) {
        if (sessionId == null || sessionId.isEmpty() || sessionType == null) {
            return;
        }
        try {
            NIMClient.getService(MsgService.class)
                    .deleteRangeHistory(sessionId, sessionType, -1L, cutoffTimeMs());
        } catch (Exception ignored) {
        }
    }

    /** 清理最近会话列表里所有会话的过期本地消息 */
    public static void cleanAllRecentSessions() {
        try {
            MsgService msgService = NIMClient.getService(MsgService.class);
            List<RecentContact> recentContacts = msgService.queryRecentContactsBlock();
            if (recentContacts == null || recentContacts.isEmpty()) {
                return;
            }
            long cutoff = cutoffTimeMs();
            for (RecentContact recent : recentContacts) {
                if (recent == null) {
                    continue;
                }
                String contactId = recent.getContactId();
                SessionTypeEnum sessionType = recent.getSessionType();
                if (contactId == null || contactId.isEmpty() || sessionType == null) {
                    continue;
                }
                try {
                    msgService.deleteRangeHistory(contactId, sessionType, -1L, cutoff);
                } catch (Exception ignored) {
                }
            }
        } catch (Exception ignored) {
        }
    }

    /**
     * 限制动态拉取只落在保留窗口内。
     * FORWARD=上拉看更早；BACKWARD=下拉看更新。
     */
    public static void applyFetchRetention(
            GetMessagesDynamicallyParam param,
            GetMessageDirectionEnum direction,
            @Nullable IMMessage anchor) {
        long cutoff = cutoffTimeMs();
        if (anchor == null) {
            param.setFromTime(cutoff);
            return;
        }
        if (direction == GetMessageDirectionEnum.FORWARD) {
            param.setToTime(anchor.getTime());
            param.setFromTime(cutoff);
        } else {
            param.setFromTime(Math.max(anchor.getTime(), cutoff));
        }
    }

    /** 上拉加载更早消息时，锚点已在保留窗口外则不再请求 */
    public static boolean shouldStopForwardFetch(@Nullable IMMessage anchor) {
        return anchor != null && anchor.getTime() <= cutoffTimeMs();
    }
}
