package com.yaoxin.appbase.utils;

import android.text.TextUtils;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;
import com.yaoxin.appbase.model.GroupInfoBean;
import com.yaoxin.appbase.model.UserBean;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

/**
 * 接口 data 字段兼容解析（愉悦等新后端可能返回数组或 {list/records/data/...} 包装）。
 */
public final class ApiDataParseUtil {

    private static final Gson GSON = new Gson();
    private static final Type FRIEND_LIST_TYPE = new TypeToken<List<GroupInfoBean>>() {}.getType();

    private ApiDataParseUtil() {}

    /** 好友分页：兼容纯数组 / 包装对象。 */
    public static List<GroupInfoBean> parseFriendPage(Object data) {
        if (data == null) {
            return new ArrayList<>();
        }
        String raw = data.toString();
        if (TextUtils.isEmpty(raw)) {
            return new ArrayList<>();
        }
        try {
            JsonElement root = JsonParser.parseString(raw.trim());
            JsonArray arr = extractArray(root);
            if (arr != null) {
                List<GroupInfoBean> list = GSON.fromJson(arr, FRIEND_LIST_TYPE);
                return list != null ? list : new ArrayList<>();
            }
            List<GroupInfoBean> list = GSON.fromJson(raw, FRIEND_LIST_TYPE);
            return list != null ? list : new ArrayList<>();
        } catch (Exception e) {
            try {
                List<GroupInfoBean> list = GSON.fromJson(raw, FRIEND_LIST_TYPE);
                return list != null ? list : new ArrayList<>();
            } catch (Exception ignored) {
                return new ArrayList<>();
            }
        }
    }

    /**
     * 客服 systemAppUser：兼容
     * - 纯 userId 字符串
     * - UserBean 对象
     * - 数组取首个
     * - {data/user/userInfo:...} 嵌套
     */
    public static UserBean parseKefuUser(Object data) {
        if (data == null) {
            return null;
        }
        String raw = data.toString().trim();
        if (TextUtils.isEmpty(raw)) {
            return null;
        }
        // 纯 id 字符串
        if (!raw.startsWith("{") && !raw.startsWith("[")) {
            String id = raw.replace("\"", "").trim();
            if (TextUtils.isEmpty(id)) {
                return null;
            }
            UserBean u = new UserBean();
            u.userId = id;
            return u;
        }
        try {
            JsonElement root = JsonParser.parseString(raw);
            JsonObject obj = null;
            if (root.isJsonArray()) {
                JsonArray arr = root.getAsJsonArray();
                if (arr.size() > 0 && arr.get(0).isJsonObject()) {
                    obj = arr.get(0).getAsJsonObject();
                }
            } else if (root.isJsonObject()) {
                obj = root.getAsJsonObject();
                // 常见嵌套
                String[] nestKeys = {"data", "user", "userInfo", "result", "info"};
                for (String key : nestKeys) {
                    if (obj.has(key) && obj.get(key).isJsonObject()) {
                        obj = obj.getAsJsonObject(key);
                        break;
                    }
                    if (obj.has(key) && obj.get(key).isJsonArray()) {
                        JsonArray arr = obj.getAsJsonArray(key);
                        if (arr.size() > 0 && arr.get(0).isJsonObject()) {
                            obj = arr.get(0).getAsJsonObject();
                        }
                        break;
                    }
                    if (obj.has(key) && obj.get(key).isJsonPrimitive()) {
                        String id = obj.get(key).getAsString();
                        if (!TextUtils.isEmpty(id)) {
                            UserBean u = new UserBean();
                            u.userId = id.trim();
                            return u;
                        }
                    }
                }
            }
            if (obj == null) {
                return null;
            }
            UserBean bean = GSON.fromJson(obj, UserBean.class);
            if (bean == null) {
                return null;
            }
            if (TextUtils.isEmpty(bean.userId) && bean.id > 0) {
                bean.userId = String.valueOf(bean.id);
            }
            if (TextUtils.isEmpty(bean.memberCode) && !TextUtils.isEmpty(bean.userMemberCode)) {
                bean.memberCode = bean.userMemberCode;
            }
            return bean;
        } catch (Exception e) {
            try {
                return GSON.fromJson(raw, UserBean.class);
            } catch (Exception ignored) {
                return null;
            }
        }
    }

    private static JsonArray extractArray(JsonElement root) {
        if (root == null || root.isJsonNull()) {
            return null;
        }
        if (root.isJsonArray()) {
            return root.getAsJsonArray();
        }
        if (!root.isJsonObject()) {
            return null;
        }
        JsonObject obj = root.getAsJsonObject();
        String[] keys = {"list", "records", "rows", "data", "userInfos", "friends", "members"};
        for (String key : keys) {
            if (!obj.has(key)) {
                continue;
            }
            JsonElement el = obj.get(key);
            if (el != null && el.isJsonArray()) {
                return el.getAsJsonArray();
            }
            if (el != null && el.isJsonObject()) {
                JsonArray nested = extractArray(el);
                if (nested != null) {
                    return nested;
                }
            }
        }
        return null;
    }
}
