package com.netease.yunxin.kit.chatkit.ui.fun.redpacket;

import android.text.TextUtils;

import androidx.annotation.Nullable;

import com.google.gson.Gson;
import com.netease.nimlib.sdk.msg.model.IMMessage;
import com.yaoxin.appbase.model.CustomMsgBean;
import com.yaoxin.appbase.utils.DataUtil;

import java.util.Map;

public final class RedPacketAttachUtil {

    private static final Gson GSON = new Gson();

    private RedPacketAttachUtil() {}

    @Nullable
    public static ParsedRedPacket parse(IMMessage message) {
        if (message == null || TextUtils.isEmpty(message.getAttachStr())) {
            return null;
        }
        try {
            CustomMsgBean outer = GSON.fromJson(message.getAttachStr(), CustomMsgBean.class);
            if (outer == null || outer.type < 21 || outer.type > 23) {
                return null;
            }
            CustomMsgBean inner = null;
            if (!TextUtils.isEmpty(outer.data)) {
                inner = GSON.fromJson(outer.data, CustomMsgBean.class);
            }
            if (inner == null) {
                inner = outer.result;
            }
            if (inner == null) {
                return null;
            }
            ParsedRedPacket parsed = new ParsedRedPacket();
            parsed.type = outer.type;
            parsed.message = message;
            parsed.redpacketId = firstNonEmpty(inner.id, inner.redPacketId, inner.redpacketId, outer.redpacketId);
            parsed.toUserId = firstNonEmpty(inner.toUserId, outer.toUserId);
            parsed.fromUserId = firstNonEmpty(inner.fromUserId, outer.fromUserId, message.getFromAccount());
            parsed.createTime = firstNonEmpty(inner.createTime, outer.createTime);
            parsed.claimed = isClaimedByMe(message);
            return parsed;
        } catch (Exception ignored) {
            return null;
        }
    }

    public static boolean isClaimedByMe(IMMessage message) {
        Map<String, Object> ext = message.getLocalExtension();
        if (ext == null) {
            return false;
        }
        Object userId = ext.get("userId");
        Object hasDragDown = ext.get("hasDragDown");
        return DataUtil.getUserid().equals(userId) && hasDragDown != null;
    }

    private static String firstNonEmpty(String... values) {
        if (values == null) {
            return "";
        }
        for (String value : values) {
            if (!TextUtils.isEmpty(value)) {
                return value;
            }
        }
        return "";
    }

    public static class ParsedRedPacket {
        public int type;
        public IMMessage message;
        public String redpacketId;
        public String toUserId;
        public String fromUserId;
        public String createTime;
        public boolean claimed;
    }
}
