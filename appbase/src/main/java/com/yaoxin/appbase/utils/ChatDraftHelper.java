package com.yaoxin.appbase.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;

import com.netease.nimlib.sdk.msg.constant.SessionTypeEnum;
import com.netease.yunxin.kit.corekit.im.IMKitClient;

/**
 * 本地聊天输入草稿：按当前账号 + 会话维度存储，供聊天页与会话列表共用。
 */
public final class ChatDraftHelper {

    private static final String PREF_NAME = "chat_input_draft";
    private static final String KEY_PREFIX = "draft_";

    private ChatDraftHelper() {}

    private static SharedPreferences prefs(Context context) {
        return context.getApplicationContext().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    private static String draftKey(String sessionId, SessionTypeEnum sessionType) {
        String account = IMKitClient.account();
        if (TextUtils.isEmpty(account)) {
            account = "_";
        }
        int typeValue = sessionType != null ? sessionType.getValue() : 0;
        return KEY_PREFIX + account + "|" + typeValue + "|" + sessionId;
    }

    public static void saveDraft(Context context, String sessionId, SessionTypeEnum sessionType, String draft) {
        if (context == null || TextUtils.isEmpty(sessionId)) {
            return;
        }
        String key = draftKey(sessionId, sessionType);
        SharedPreferences.Editor editor = prefs(context).edit();
        if (TextUtils.isEmpty(draft) || TextUtils.getTrimmedLength(draft) < 1) {
            editor.remove(key);
        } else {
            editor.putString(key, draft.trim());
        }
        editor.apply();
    }

    public static String getDraft(Context context, String sessionId, SessionTypeEnum sessionType) {
        if (context == null || TextUtils.isEmpty(sessionId)) {
            return "";
        }
        String draft = prefs(context).getString(draftKey(sessionId, sessionType), "");
        return draft == null ? "" : draft;
    }

    public static boolean hasDraft(Context context, String sessionId, SessionTypeEnum sessionType) {
        return !TextUtils.isEmpty(getDraft(context, sessionId, sessionType));
    }

    public static void clearDraft(Context context, String sessionId, SessionTypeEnum sessionType) {
        saveDraft(context, sessionId, sessionType, "");
    }

    public static String formatDraftPreview(Context context, String sessionId, SessionTypeEnum sessionType) {
        String draft = getDraft(context, sessionId, sessionType);
        if (TextUtils.isEmpty(draft)) {
            return "";
        }
        return context.getString(com.yaoxin.appbase.R.string.chat_draft_prefix) + draft;
    }
}
