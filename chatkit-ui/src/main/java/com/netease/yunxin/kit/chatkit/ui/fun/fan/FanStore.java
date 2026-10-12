package com.netease.yunxin.kit.chatkit.ui.fun.fan;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/** 爆粉名单本地持久化（对齐 qiangbao FanStore）。 */
public final class FanStore {

    private static final String PREFS = "yx_fan";
    private static FanStore instance;

    private final SharedPreferences prefs;

    private FanStore(Context context) {
        prefs = context.getApplicationContext().getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }

    public static FanStore getInstance(Context context) {
        if (instance == null) {
            synchronized (FanStore.class) {
                if (instance == null) {
                    instance = new FanStore(context);
                }
            }
        }
        return instance;
    }

    public List<FanUser> list(FanMode mode) {
        String raw = prefs.getString(mode.storageKey(), null);
        if (TextUtils.isEmpty(raw)) {
            return new ArrayList<>();
        }
        return parseJsonArray(raw);
    }

    public void save(FanMode mode, List<FanUser> users) {
        JSONArray arr = new JSONArray();
        if (users != null) {
            for (FanUser u : users) {
                if (u != null) {
                    arr.put(u.toJson());
                }
            }
        }
        prefs.edit().putString(mode.storageKey(), arr.toString()).apply();
    }

    /**
     * 按群合并普通爆粉：只替换 groupId 对应成员，其它群已存数据保留。
     *
     * @return int[0]=合并后总人数, int[1]=本次写入人数
     */
    public int[] mergeGroupMembers(FanMode mode, String groupId, List<FanUser> incoming) {
        String gid = groupId == null ? "" : groupId.trim();
        if (TextUtils.isEmpty(gid)) {
            return new int[] {list(mode).size(), 0};
        }
        List<FanUser> list = new ArrayList<>(list(mode));
        for (int i = list.size() - 1; i >= 0; i--) {
            FanUser u = list.get(i);
            String ug = u.groupId == null ? "" : u.groupId.trim();
            if (!TextUtils.isEmpty(ug) && ug.equals(gid)) {
                list.remove(i);
            }
        }
        Set<String> seen = new HashSet<>();
        for (FanUser u : list) {
            if (!TextUtils.isEmpty(u.userId)) {
                seen.add(u.userId.trim());
            }
        }
        int added = 0;
        if (incoming != null) {
            for (FanUser u : incoming) {
                if (u == null || TextUtils.isEmpty(u.userId)) {
                    continue;
                }
                String uid = u.userId.trim();
                if (seen.add(uid)) {
                    list.add(u);
                    added++;
                }
            }
        }
        save(mode, list);
        return new int[] {list.size(), added};
    }

    public boolean append(FanMode mode, FanUser user) {
        if (user == null || TextUtils.isEmpty(user.userId)) {
            return false;
        }
        List<FanUser> list = new ArrayList<>(list(mode));
        for (int i = 0; i < list.size(); i++) {
            FanUser old = list.get(i);
            if (user.userId.equals(old.userId)
                    || (!TextUtils.isEmpty(user.cloudId) && user.cloudId.equals(old.cloudId))
                    || (!TextUtils.isEmpty(user.cloudId) && user.cloudId.equals(old.userId))) {
                boolean betterName =
                        !TextUtils.isEmpty(user.name)
                                && (TextUtils.isEmpty(old.name) || old.name.equals(old.userId));
                boolean betterAvatar =
                        !TextUtils.isEmpty(user.avatar)
                                && !user.avatar.equals(old.avatar == null ? "" : old.avatar);
                if (!betterName && !betterAvatar) {
                    return false;
                }
                if (betterName) {
                    old.name = user.name;
                }
                if (betterAvatar) {
                    old.avatar = user.avatar;
                }
                if (!TextUtils.isEmpty(user.cloudId)) {
                    old.cloudId = user.cloudId;
                }
                if (!TextUtils.isEmpty(user.groupId)) {
                    old.groupId = user.groupId;
                }
                save(mode, list);
                return true;
            }
        }
        list.add(user);
        save(mode, list);
        return true;
    }

    public void clear(FanMode mode) {
        prefs.edit().remove(mode.storageKey()).apply();
    }

    public void remove(FanMode mode, String userId) {
        if (TextUtils.isEmpty(userId)) {
            return;
        }
        List<FanUser> next = new ArrayList<>();
        for (FanUser u : list(mode)) {
            if (!userId.equals(u.userId)) {
                next.add(u);
            }
        }
        save(mode, next);
    }

    private static List<FanUser> parseJsonArray(String raw) {
        List<FanUser> out = new ArrayList<>();
        try {
            JSONArray arr = new JSONArray(raw);
            for (int i = 0; i < arr.length(); i++) {
                JSONObject o = arr.optJSONObject(i);
                FanUser u = FanUser.fromJson(o);
                if (u != null) {
                    out.add(u);
                }
            }
        } catch (Exception ignored) {
        }
        return out;
    }
}
