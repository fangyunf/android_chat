package com.netease.yunxin.kit.chatkit.ui.fun.fan;

import android.text.TextUtils;

import org.json.JSONObject;

/**
 * 爆粉名单用户。
 * <p>
 * 只关心 {@link #userId}：名单主键，也是 friends/searchByUserIdAOS 入参。
 * 加好友时再查 memberCode，名单里不存、不用 memberCode。
 */
public class FanUser {
    public String userId;
    public String name;
    public String cloudId;
    public String groupId;
    public String avatar;

    public FanUser() {}

    public FanUser(String userId, String name, String cloudId, String groupId, String avatar) {
        this.userId = userId == null ? "" : userId.trim();
        this.name = name == null ? "" : name;
        this.cloudId = emptyToNull(cloudId);
        this.groupId = emptyToNull(groupId);
        this.avatar = emptyToNull(avatar);
    }

    /** 兼容旧调用：忽略 memberCode */
    public FanUser(
            String userId,
            String name,
            String cloudId,
            String groupId,
            String avatar,
            String ignoredMemberCode) {
        this(userId, name, cloudId, groupId, avatar);
    }

    public FanUser copy() {
        return new FanUser(userId, name, cloudId, groupId, avatar);
    }

    /** 查用户 / 展示：一律用 userId */
    public String lookupUserId() {
        if (!TextUtils.isEmpty(userId)) {
            return userId.trim();
        }
        if (!TextUtils.isEmpty(cloudId)) {
            return cloudId.trim();
        }
        return "";
    }

    JSONObject toJson() {
        JSONObject o = new JSONObject();
        try {
            o.put("userId", userId == null ? "" : userId);
            o.put("name", name == null ? "" : name);
            if (!TextUtils.isEmpty(cloudId)) {
                o.put("cloudId", cloudId);
            }
            if (!TextUtils.isEmpty(groupId)) {
                o.put("groupId", groupId);
            }
            if (!TextUtils.isEmpty(avatar)) {
                o.put("avatar", avatar);
            }
        } catch (Exception ignored) {
        }
        return o;
    }

    static FanUser fromJson(JSONObject o) {
        if (o == null) {
            return null;
        }
        FanUser u = new FanUser();
        u.userId = o.optString("userId", "").trim();
        u.name = o.optString("name", "");
        u.cloudId = optNullable(o, "cloudId");
        u.groupId = optNullable(o, "groupId");
        u.avatar = optNullable(o, "avatar");
        if (TextUtils.isEmpty(u.userId)) {
            return null;
        }
        return u;
    }

    private static String optNullable(JSONObject o, String key) {
        String v = o.optString(key, "");
        return TextUtils.isEmpty(v) ? null : v;
    }

    private static String emptyToNull(String v) {
        if (v == null) {
            return null;
        }
        String t = v.trim();
        return t.isEmpty() ? null : t;
    }
}
