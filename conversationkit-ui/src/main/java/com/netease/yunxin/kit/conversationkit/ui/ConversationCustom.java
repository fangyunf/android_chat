// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.conversationkit.ui;

import android.content.Context;
import android.text.TextUtils;

import com.google.gson.Gson;
import com.netease.nimlib.sdk.NIMClient;
import com.netease.nimlib.sdk.msg.MsgService;
import com.netease.nimlib.sdk.msg.attachment.NetCallAttachment;
import com.netease.nimlib.sdk.msg.constant.MsgTypeEnum;
import com.netease.nimlib.sdk.msg.constant.SessionTypeEnum;
import com.netease.nimlib.sdk.msg.model.IMMessage;
import com.netease.yunxin.kit.chatkit.model.ConversationInfo;
import com.netease.yunxin.kit.corekit.im.model.AttachmentContent;
import com.yaoxin.appbase.model.CustomMsgBean;
import com.yaoxin.appbase.utils.AESUtil;

public class ConversationCustom {

    public String customContentText(Context context, ConversationInfo conversationInfo) {
        if (conversationInfo != null && context != null) {
            MsgTypeEnum typeEnum = conversationInfo.getMsgType();
            switch (typeEnum) {
                case notification:
                    return context.getString(R.string.msg_type_notification);
                case text:
//          return conversationInfo.getContent();
                    String content = conversationInfo.getContent();
                    try {
                        content = AESUtil.msgAseDecrypt(content);
                    } catch (Exception e) {

                    }
                    return content;
                case audio:
                    return context.getString(R.string.msg_type_audio);
                case video:
                    return context.getString(R.string.msg_type_video);
                case tip:
                    return context.getString(R.string.msg_type_tip);
                case image:
                    return context.getString(R.string.msg_type_image);
                case file:
                    return context.getString(R.string.msg_type_file);
                case location:
                    return context.getString(R.string.msg_type_location);
                case nrtc_netcall:
                    NetCallAttachment attachment = (NetCallAttachment) conversationInfo.getAttachment();
                    int type = attachment.getType();
                    if (type == 1) {
                        return context.getString(R.string.msg_type_rtc_audio);
                    } else {
                        return context.getString(R.string.msg_type_rtc_video);
                    }
                case custom:
                    String result = conversationInfo.getContent();
                    if (conversationInfo.getAttachment() instanceof AttachmentContent) {
                        result = ((AttachmentContent) conversationInfo.getAttachment()).getContent();
                    }
                    // 尝试从 getAttachStr() 获取数据
                    // 使用 queryLastMessage() 查询最后一条消息
                    try {
                        String account = conversationInfo.getContactId();
                        SessionTypeEnum sessionType = conversationInfo.getSessionType();
                        if (!TextUtils.isEmpty(account) && sessionType != null) {
                            IMMessage message = NIMClient.getService(MsgService.class).queryLastMessage(account, sessionType);
                            if (message != null && !TextUtils.isEmpty(message.getAttachStr())) {
                                // 从 getAttachStr() 解析数据
                                CustomMsgBean bean = new Gson().fromJson(message.getAttachStr(), CustomMsgBean.class);
                                if (bean != null) {
                                    // 根据类型返回对应的文本
                                    if (bean.type == 21) {
                                        return "[红包]";
                                    } else if (bean.type == 22) {
                                        return "[红包]";
                                    } else if (bean.type == 23) {
                                        return "[红包]";
                                    } else if (bean.type == 28) {
                                        return "[转账]";
                                    }
                                }
                            }
                        }
                    } catch (Exception e) {
                        // 查询失败，继续使用原来的逻辑
                    }
                    return result;
                default:
                    return context.getString(R.string.msg_type_no_tips);
            }
        }
        return "";
    }
}
