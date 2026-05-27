// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.chatkit.ui.fun.view.message.viewholder;

import android.view.LayoutInflater;
import android.view.View;

import androidx.annotation.NonNull;

import com.netease.nimlib.sdk.msg.constant.MsgDirectionEnum;
import com.netease.nimlib.sdk.msg.constant.MsgTypeEnum;
import com.netease.yunxin.kit.chatkit.ui.R;
import com.netease.yunxin.kit.chatkit.ui.common.MessageHelper;
import com.netease.yunxin.kit.chatkit.ui.databinding.ChatBaseMessageViewHolderBinding;
import com.netease.yunxin.kit.chatkit.ui.databinding.FunChatMessageTextViewHolderBinding;
import com.netease.yunxin.kit.chatkit.ui.model.ChatMessageBean;

/**
 * view holder for Text message
 */
public class ChatTextMessageViewHolder extends FunChatBaseMessageViewHolder {

    FunChatMessageTextViewHolderBinding textBinding;

    public ChatTextMessageViewHolder(@NonNull ChatBaseMessageViewHolderBinding parent, int viewType) {
        super(parent, viewType);
    }

    @Override
    public void addViewToMessageContainer() {
        textBinding = FunChatMessageTextViewHolderBinding.inflate(LayoutInflater.from(parent.getContext()), getMessageContainer(), true);
    }

    @Override
    public void bindData(ChatMessageBean message, ChatMessageBean lastMessage) {
        super.bindData(message, lastMessage);
        if (properties.getMessageTextSize() != null) {
            textBinding.messageText.setTextSize(properties.getMessageTextSize());
        }
        if (properties.getMessageTextColor() != null) {
            textBinding.messageText.setTextColor(properties.getMessageTextColor());
        } else if (isForwardMsg()) {
            // 合并转发详情页是白底列表，统一黑字，避免「白底白字」
            textBinding.messageText.setTextColor(parent.getContext().getResources().getColor(R.color.color_333333));
        } else {
            // 用发送方向判断「我的消息」，避免 fromUser 未加载时先黑后白
            if (message.getMessageData().getMessage().getDirect() == MsgDirectionEnum.Out) {
                textBinding.messageText.setTextColor(parent.getContext().getResources().getColor(com.yaoxin.appbase.R.color.color_white));
            } else {
                textBinding.messageText.setTextColor(parent.getContext().getResources().getColor(R.color.color_333333));
            }
        }
        if (message.getMessageData().getMessage().getMsgType() == MsgTypeEnum.text) {

            if (isForwardMsg()) {
                MessageHelper.identifyExpression(
                    textBinding.getRoot().getContext(),
                    textBinding.messageText,
                    message.getMessageData().getMessage());
            } else {
                MessageHelper.identifyExpression(textBinding.getRoot().getContext(), textBinding.messageText, message.getMessageData().getMessage());
            }
        } else {
            //文件消息暂不支持所以展示提示信息
            textBinding.messageText.setText(parent.getContext().getResources().getString(R.string.chat_message_not_support_tips));
        }
    }

    @Override
    public void onMessageRevokeStatus(ChatMessageBean data) {
        super.onMessageRevokeStatus(data);
        if (revokedViewBinding != null) {
            if (!MessageHelper.revokeMsgIsEdit(data)) {
                revokedViewBinding.tvAction.setVisibility(View.GONE);
            }
        }
    }
}
