package com.netease.yunxin.kit.chatkit.ui.custom;

import com.netease.nimlib.sdk.msg.attachment.MsgAttachment;

/** 红包/转账回写用：整段 attach JSON（含 type/enc/密文 data）原样写入。 */
public class EncryptedMoneyAttachment implements MsgAttachment {

    private final String rawJson;

    public EncryptedMoneyAttachment(String rawJson) {
        this.rawJson = rawJson;
    }

    @Override
    public String toJson(boolean send) {
        return rawJson;
    }
}
