// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.chatkit.ui.normal.view.message.viewholder;

import android.text.style.ImageSpan;
import android.view.LayoutInflater;
import android.view.View;

import androidx.annotation.NonNull;

import com.netease.nimlib.sdk.msg.constant.MsgTypeEnum;
import com.netease.yunxin.kit.chatkit.ui.R;
import com.netease.yunxin.kit.chatkit.ui.common.MessageHelper;
import com.netease.yunxin.kit.chatkit.ui.databinding.ChatBaseMessageViewHolderBinding;
import com.netease.yunxin.kit.chatkit.ui.databinding.NormalChatMessageTextViewHolderBinding;
import com.netease.yunxin.kit.chatkit.ui.model.ChatMessageBean;
import com.netease.yunxin.kit.chatkit.ui.view.message.viewholder.options.CommonUIOption;

/**
 * view holder for Text message
 */
public class ChatTextMessageViewHolder extends NormalChatBaseMessageViewHolder {

    NormalChatMessageTextViewHolderBinding textBinding;

    public ChatTextMessageViewHolder(@NonNull ChatBaseMessageViewHolderBinding parent, int viewType) {
        super(parent, viewType);
    }

    @Override
    public void addViewToMessageContainer() {
        textBinding =
                NormalChatMessageTextViewHolderBinding.inflate(
                        LayoutInflater.from(parent.getContext()), getMessageContainer(), true);
    }

    @Override
    public void bindData(ChatMessageBean message, ChatMessageBean lastMessage) {
        super.bindData(message, lastMessage);
        CommonUIOption commonUIOption = uiOptions.commonUIOption;
        if (isForwardMsg()) {
            // 合并转发详情页是白底列表，统一黑字，避免「白底白字」
            textBinding.messageText.setTextColor(
                    parent.getContext().getResources().getColor(R.color.color_333333));
        } else if (commonUIOption.messageTextColor != null) {
            textBinding.messageText.setTextColor(commonUIOption.messageTextColor);
        } else if (properties.getMessageTextColor() != null) {
            textBinding.messageText.setTextColor(properties.getMessageTextColor());
        }
        if (commonUIOption.messageTextSize != null) {
            textBinding.messageText.setTextSize(commonUIOption.messageTextSize);
        } else if (properties.getMessageTextSize() != null) {
            textBinding.messageText.setTextSize(properties.getMessageTextSize());
        }

        if (message.getMessageData().getMessage().getMsgType() == MsgTypeEnum.text) {
            //转发消息不需要展示@的高亮
            if (isForwardMsg()) {
                MessageHelper.identifyExpression(
                        textBinding.getRoot().getContext(),
                        textBinding.messageText,
                        message.getMessageData().getMessage());
            } else {
                MessageHelper.identifyExpression(
                        textBinding.getRoot().getContext(),
                        textBinding.messageText,
                        message.getMessageData().getMessage());
            }

        } else {
            //文件消息暂不支持所以展示提示信息
            textBinding.messageText.setText(
                    parent.getContext().getResources().getString(R.string.chat_message_not_support_tips));
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
