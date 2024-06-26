package com.netease.yunxin.kit.conversationkit.ui.fun.page.Bean;

import com.netease.nimlib.sdk.msg.attachment.MsgAttachment;
import com.netease.nimlib.sdk.msg.constant.MsgStatusEnum;
import com.netease.nimlib.sdk.msg.constant.MsgTypeEnum;
import com.netease.nimlib.sdk.msg.constant.SessionTypeEnum;
import com.netease.nimlib.sdk.msg.model.IMMessage;
import com.netease.nimlib.sdk.msg.model.RecentContact;

import java.util.Map;

public class ConversationCustomInfoBean implements RecentContact {
    public String sessionId;
    @Override
    public String getContactId() {
        return sessionId;
    }

    @Override
    public String getFromAccount() {
        return null;
    }

    @Override
    public String getFromNick() {
        return "客服";
    }

    @Override
    public SessionTypeEnum getSessionType() {
        return SessionTypeEnum.P2P;
    }

    @Override
    public String getRecentMessageId() {
        return null;
    }

    @Override
    public MsgTypeEnum getMsgType() {
        return null;
    }

    @Override
    public MsgStatusEnum getMsgStatus() {
        return null;
    }

    @Override
    public void setMsgStatus(MsgStatusEnum msgStatusEnum) {

    }

    @Override
    public int getUnreadCount() {
        return 0;
    }

    @Override
    public String getContent() {
        return null;
    }

    @Override
    public long getTime() {
        return 0;
    }

    @Override
    public MsgAttachment getAttachment() {
        return null;
    }

    @Override
    public void setTag(long l) {

    }

    @Override
    public long getTag() {
        return 0;
    }

    @Override
    public Map<String, Object> getExtension() {
        return null;
    }

    @Override
    public void setExtension(Map<String, Object> map) {

    }

    @Override
    public boolean setLastMsg(IMMessage imMessage) {
        return false;
    }
}
