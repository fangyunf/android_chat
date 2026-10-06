package com.yaoxin.appbase.utils;

import android.text.TextUtils;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.yaoxin.appbase.model.CustomMsgBean;

import org.json.JSONObject;

/**
 * 红包/转账云信自定义消息解码：type 明文，data 可能是明文 JSON 或 AES 密文。
 * 算法与 IM 文本相同：AES-128/ECB/PKCS7、密钥 wallstreetimchat（MSG_ENCODE_KEY），不要用 HTTP NetAesKey。
 */
public final class CustomMoneyAttachCodec {

    public static final int TYPE_EXCLUSIVE = 21;
    public static final int TYPE_PERSON = 22;
    public static final int TYPE_GROUP = 23;
    public static final int TYPE_TRANSFER = 28;

    private static final Gson GSON = new Gson();

    private CustomMoneyAttachCodec() {}

    public static boolean isMoneyType(int type) {
        return type == TYPE_EXCLUSIVE
                || type == TYPE_PERSON
                || type == TYPE_GROUP
                || type == TYPE_TRANSFER;
    }

    public static boolean isMoneyAttach(String attachStr) {
        if (TextUtils.isEmpty(attachStr) || !attachStr.trim().startsWith("{")) {
            return false;
        }
        try {
            JsonElement el = new JsonParser().parse(attachStr);
            if (el == null || !el.isJsonObject()) {
                return false;
            }
            JsonObject obj = el.getAsJsonObject();
            if (!obj.has("type")) {
                return false;
            }
            return isMoneyType(obj.get("type").getAsInt());
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 解析附件。钱类消息会按 enc/data 解密。
     * 解密失败返回 null，调用方应丢弃该条，不要把密文当 JSON 解析。
     */
    public static CustomMsgBean parse(String attachStr) {
        if (TextUtils.isEmpty(attachStr)) {
            return null;
        }
        CustomMsgBean bean;
        try {
            bean = GSON.fromJson(attachStr, CustomMsgBean.class);
        } catch (Exception e) {
            return null;
        }
        if (bean == null) {
            return null;
        }
        if (!isMoneyType(bean.type)) {
            fillResultIfPlainJson(bean);
            return bean;
        }
        String data = bean.data;
        boolean needDecrypt = bean.enc == 1 || !startsWithJsonObject(data);
        if (needDecrypt) {
            if (TextUtils.isEmpty(data)) {
                return null;
            }
            String plain;
            try {
                plain = AESUtil.msgAseDecrypt(data.trim());
            } catch (Exception e) {
                return null;
            }
            if (!startsWithJsonObject(plain)) {
                return null;
            }
            bean.data = plain;
        } else if (!startsWithJsonObject(data)) {
            return null;
        }
        try {
            bean.result = GSON.fromJson(bean.data, CustomMsgBean.class);
        } catch (Exception e) {
            return null;
        }
        if (bean.result == null) {
            return null;
        }
        return bean;
    }

    /** 回写本地自定义消息：enc=1，data 为 Base64 密文。 */
    public static String encode(int type, String plainDataJson) {
        if (!isMoneyType(type) || TextUtils.isEmpty(plainDataJson)) {
            return null;
        }
        try {
            String cipher = AESUtil.msgAesEncrypt(plainDataJson);
            if (cipher != null) {
                cipher = cipher.replaceAll("\\s", "");
            }
            JSONObject obj = new JSONObject();
            obj.put("type", type);
            obj.put("enc", 1);
            obj.put("data", cipher);
            return obj.toString();
        } catch (Exception e) {
            return null;
        }
    }

    public static String encode(CustomMsgBean bean) {
        if (bean == null) {
            return null;
        }
        return encode(bean.type, bean.data);
    }

    private static boolean startsWithJsonObject(String data) {
        if (TextUtils.isEmpty(data)) {
            return false;
        }
        return data.trim().startsWith("{");
    }

    private static void fillResultIfPlainJson(CustomMsgBean bean) {
        if (bean == null || bean.result != null || !startsWithJsonObject(bean.data)) {
            return;
        }
        try {
            bean.result = GSON.fromJson(bean.data, CustomMsgBean.class);
        } catch (Exception ignored) {
        }
    }
}
