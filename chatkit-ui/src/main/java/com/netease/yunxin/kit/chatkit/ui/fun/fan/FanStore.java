package com.netease.yunxin.kit.chatkit.ui.fun.fan;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/** 爆粉名单本地持久化。去重只认 userId。 */
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

    public synchronized void save(FanMode mode, List<FanUser> users) {
        JSONArray arr = new JSONArray();
        if (users != null) {
            for (FanUser u : users) {
                if (u != null && !TextUtils.isEmpty(u.userId)) {
                    arr.put(u.toJson());
                }
            }
        }
        prefs.edit().putString(mode.storageKey(), arr.toString()).commit();
    }

    public synchronized boolean append(FanMode mode, FanUser user) {
        if (user == null || TextUtils.isEmpty(user.userId)) {
            return false;
        }
        List<FanUser> list = new ArrayList<>(list(mode));
        String uid = user.userId.trim();
        for (FanUser old : list) {
            if (old != null && uid.equals(old.userId == null ? "" : old.userId.trim())) {
                // 已存在：仅在信息更完整时更新
                boolean changed = false;
                if (!TextUtils.isEmpty(user.name)
                        && (TextUtils.isEmpty(old.name) || old.name.equals(old.userId))) {
                    old.name = user.name;
                    changed = true;
                }
                if (!TextUtils.isEmpty(user.avatar)
                        && !user.avatar.equals(old.avatar == null ? "" : old.avatar)) {
                    old.avatar = user.avatar;
                    changed = true;
                }
                if (!TextUtils.isEmpty(user.cloudId) && TextUtils.isEmpty(old.cloudId)) {
                    old.cloudId = user.cloudId;
                    changed = true;
                }
                if (!TextUtils.isEmpty(user.groupId)) {
                    old.groupId = user.groupId;
                    changed = true;
                }
                if (changed) {
                    save(mode, list);
                    return true;
                }
                return false;
            }
        }
        list.add(user);
        save(mode, list);
        return true;
    }

    public void clear(FanMode mode) {
        prefs.edit().remove(mode.storageKey()).commit();
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
                if (u == null || TextUtils.isEmpty(u.userId)) {
                    continue;
                }
                boolean dup = false;
                for (FanUser old : out) {
                    if (u.userId.equals(old.userId)) {
                        dup = true;
                        break;
                    }
                }
                if (!dup) {
                    out.add(u);
                }
            }
        } catch (Exception ignored) {
        }
        return out;
    }
}
