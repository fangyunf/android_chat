package com.turunsi.yaoxin.main.mine.collection;

import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.netease.nimlib.sdk.msg.MessageBuilder;
import com.netease.nimlib.sdk.msg.attachment.ImageAttachment;
import com.netease.nimlib.sdk.msg.constant.SessionTypeEnum;
import com.netease.nimlib.sdk.msg.model.CollectInfo;
import com.netease.nimlib.sdk.msg.model.IMMessage;
import com.yaoxin.appbase.utils.AESUtil;

import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Map;

/** 收藏列表：类型、解密、来源解析 */
public final class CollectionListHelper {

    public static final int TYPE_IMAGE = 1;
    public static final int TYPE_VIDEO = 2;
    public static final int TYPE_FILE = 3;
    public static final int TYPE_TEXT = 1024;

    public static final String CONTENT_TEXT = "text";
    public static final String CONTENT_IMAGE = "image";
    public static final String CONTENT_VIDEO = "video";
    public static final String CONTENT_FILE = "file";

    public static final String TAB_ALL = "all";
    public static final String TAB_TEXT = "text";
    public static final String TAB_IMAGE = "image";
    public static final String TAB_VIDEO = "video";
    public static final String TAB_FILE = "file";

    private static final SimpleDateFormat DATE_FORMAT =
            new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

    private CollectionListHelper() {}

    @NonNull
    public static String getContentType(@NonNull CollectInfo info) {
        String fromExt = parseExtString(info.getExt(), "contentType");
        if (!TextUtils.isEmpty(fromExt)) {
            return fromExt;
        }
        int type = info.getType();
        if (type == TYPE_IMAGE) {
            return CONTENT_IMAGE;
        }
        if (type == TYPE_VIDEO) {
            return CONTENT_VIDEO;
        }
        if (type == TYPE_FILE) {
            return CONTENT_FILE;
        }
        // 1024 及云信默认文本收藏类型
        if (type == TYPE_TEXT || type == 0) {
            return CONTENT_TEXT;
        }
        // 未知类型在「全部」中按文本展示
        return CONTENT_TEXT;
    }

    public static boolean matchTab(@NonNull CollectInfo info, @NonNull String tab) {
        if (TAB_ALL.equals(tab)) {
            return true;
        }
        return tab.equals(getContentType(info));
    }

    /** 展示用：文本/文件名等需解密 */
    @NonNull
    public static String getDisplayData(@NonNull CollectInfo info) {
        String raw = info.getData();
        if (raw == null) {
            return "";
        }
        String contentType = getContentType(info);
        if (CONTENT_IMAGE.equals(contentType) || CONTENT_VIDEO.equals(contentType)) {
            return raw;
        }
        return decryptCollectText(raw);
    }

    /**
     * 收藏文本解密。兼容：明文入库、单层加密、历史误对已加密消息再次加密（需解密两次）。
     */
    @NonNull
    private static String decryptCollectText(@NonNull String raw) {
        String result = AESUtil.safeMsgDecrypt(raw);
        // 第一次解密后仍无明文特征，且内容有变化，再解一次（双重加密历史数据）
        if (!TextUtils.equals(result, raw)
                && !AESUtil.containsChineseCharacters(result)
                && looksLikeCipherText(result)) {
            String second = AESUtil.safeMsgDecrypt(result);
            if (!TextUtils.isEmpty(second)) {
                result = second;
            }
        }
        return result == null ? "" : result;
    }

    private static boolean looksLikeCipherText(@NonNull String text) {
        if (text.length() < 8) {
            return false;
        }
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            boolean base64Char =
                    (c >= 'A' && c <= 'Z')
                            || (c >= 'a' && c <= 'z')
                            || (c >= '0' && c <= '9')
                            || c == '+'
                            || c == '/'
                            || c == '='
                            || c == '\n'
                            || c == '\r';
            if (!base64Char) {
                return false;
            }
        }
        return true;
    }

    @NonNull
    public static String getSourceName(@NonNull CollectInfo info) {
        String nick = parseExtString(info.getExt(), "fromNick");
        if (!TextUtils.isEmpty(nick)) {
            return nick;
        }
        nick = parseExtString(info.getExt(), "fromName");
        if (!TextUtils.isEmpty(nick)) {
            return nick;
        }
        nick = parseExtString(info.getExt(), "fromAccount");
        if (!TextUtils.isEmpty(nick)) {
            return nick;
        }
        return parseExtString(info.getExt(), "from");
    }

    @NonNull
    public static IMMessage buildImagePreviewMessage(@NonNull String imageUrl) {
        IMMessage message =
                MessageBuilder.createEmptyMessage(
                        "collection", SessionTypeEnum.P2P, System.currentTimeMillis());
        ImageAttachment attachment = new ImageAttachment();
        attachment.setUrl(imageUrl);
        message.setAttachment(attachment);
        return message;
    }

    public static boolean isSameCollectItem(@NonNull CollectInfo a, @NonNull CollectInfo b) {
        return a.getId() == b.getId() && a.getCreateTime() == b.getCreateTime();
    }

    @NonNull
    public static String formatCollectDate(long createTime) {
        try {
            return DATE_FORMAT.format(new Date(createTime));
        } catch (Exception e) {
            return "";
        }
    }

    @Nullable
    private static String parseExtString(@Nullable String ext, @NonNull String key) {
        if (ext == null || ext.isEmpty()) {
            return null;
        }
        try {
            Type type = new TypeToken<Map<String, Object>>() {}.getType();
            Map<String, Object> map = new Gson().fromJson(ext, type);
            if (map == null) {
                return null;
            }
            Object value = map.get(key);
            if (value instanceof String && !((String) value).isEmpty()) {
                return (String) value;
            }
        } catch (Exception ignored) {
        }
        return null;
    }
}
