package com.netease.yunxin.kit.chatkit.ui.fun.fan;

import android.text.TextUtils;

import org.json.JSONObject;

public class FanUser {
    /** 业务展示 ID / 加好友用 memberCode */
    public String userId;
    public String name;
    /** 云信 / 宿主 userId */
    public String cloudId;
    public String groupId;
    public String avatar;

    public FanUser() {}

    public FanUser(String userId, String name, String cloudId, String groupId, String avatar) {
        this.userId = userId == null ? "" : userId.trim();
        this.name = name == null ? "" : name;
        this.cloudId = cloudId;
        this.groupId = groupId;
        this.avatar = avatar;
    }

    public FanUser copy() {
        return new FanUser(userId, name, cloudId, groupId, avatar);
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
}
