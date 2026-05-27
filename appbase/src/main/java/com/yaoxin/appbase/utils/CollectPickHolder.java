package com.yaoxin.appbase.utils;

import androidx.annotation.Nullable;

/** 聊天内从收藏列表选条发送，点击后不关闭收藏页 */
public final class CollectPickHolder {

    public interface OnCollectPickListener {
        void onPick(String content, int type);
    }

    @Nullable private static OnCollectPickListener listener;

    private CollectPickHolder() {}

    public static void setListener(@Nullable OnCollectPickListener pickListener) {
        listener = pickListener;
    }

    @Nullable
    public static OnCollectPickListener getListener() {
        return listener;
    }

    public static void clear() {
        listener = null;
    }
}
