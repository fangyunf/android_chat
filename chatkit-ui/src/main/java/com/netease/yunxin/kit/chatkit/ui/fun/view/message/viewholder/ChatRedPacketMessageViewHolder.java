// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.chatkit.ui.fun.view.message.viewholder;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;

import androidx.annotation.NonNull;

import com.google.gson.Gson;
import com.netease.yunxin.kit.chatkit.ui.R;
import com.netease.yunxin.kit.chatkit.ui.common.MessageHelper;
import com.netease.yunxin.kit.chatkit.ui.custom.RedPacketAttachment;
import com.netease.yunxin.kit.chatkit.ui.custom.RichTextAttachment;
import com.netease.yunxin.kit.chatkit.ui.databinding.ChatBaseMessageViewHolderBinding;
import com.netease.yunxin.kit.chatkit.ui.databinding.FunChatMessageRedPacketViewHolderBinding;
import com.netease.yunxin.kit.chatkit.ui.databinding.FunChatMessageRichTextViewHolderBinding;
import com.netease.yunxin.kit.chatkit.ui.model.ChatMessageBean;
import com.yaoxin.appbase.model.CustomMsgBean;
import com.yaoxin.appbase.utils.DataUtil;
import com.yaoxin.appbase.utils.NumberUtil;
import com.yaoxin.appbase.utils.TimeUtil;

import java.util.Map;

public class ChatRedPacketMessageViewHolder extends FunChatBaseMessageViewHolder {

    protected FunChatMessageRedPacketViewHolderBinding viewBinding;

    public ChatRedPacketMessageViewHolder(
            @NonNull ChatBaseMessageViewHolderBinding parent, int viewType) {
        super(parent, viewType);
    }

    @Override
    protected void addViewToMessageContainer() {
        viewBinding =
                FunChatMessageRedPacketViewHolderBinding.inflate(
                        LayoutInflater.from(parent.getContext()), getMessageContainer(), true);
    }

    @Override
    public void bindData(ChatMessageBean message, ChatMessageBean lastMessage) {
        super.bindData(message, lastMessage);
        if (message != null
                && message.getMessageData() != null
                && !message.getMessageData().getMessage().getAttachStr().isEmpty()) {
            Map<String, Object> localExtension = message.getMessageData().getMessage().getLocalExtension();
            boolean hasDraw = false;
            if (localExtension != null && DataUtil.getUserid().equals(localExtension.get("userId"))) {
                hasDraw = true;
            }

            CustomMsgBean bean = new Gson().fromJson(message.getMessageData().getMessage().getAttachStr(), CustomMsgBean.class);
            bean.result = new Gson().fromJson(bean.data, CustomMsgBean.class);
            if (hasDraw) {
                viewBinding.funChatMessageRedPacketViewHolderBgIv.setImageResource((bean.type == 21) ? R.drawable.chat_redpacket_purple_bg_is_open : R.drawable.chat_red_packet_cell_bg_is_open);

            } else {

                viewBinding.funChatMessageRedPacketViewHolderBgIv.setImageResource((bean.type == 21) ? R.drawable.chat_redpacket_purple_bg_no_open : R.drawable.chat_red_packet_cell_bg_no_open);
            }
            if (bean.type == 21) {
                viewBinding.funChatMessageRedPacketViewHolderGreetingTv.setText(bean.result.toUserName);
                viewBinding.funChatMessageRedPacketViewHolderTypeTv.setText("专属红包");
            } else if (bean.type == 22) {
                viewBinding.funChatMessageRedPacketViewHolderGreetingTv.setText(bean.result.title);
                viewBinding.funChatMessageRedPacketViewHolderTypeTv.setText("红包");
            } else {
                viewBinding.funChatMessageRedPacketViewHolderGreetingTv.setText(bean.result.title);
                viewBinding.funChatMessageRedPacketViewHolderTypeTv.setText("拼手气红包");
            }
            viewBinding.funChatMessageRedPacketViewHolderTimeTv.setText(TimeUtil.stampToDate(bean.result.createTime));
//      if (bean.type == 21) {
            viewBinding.funChatMessageRedPacketViewHolderMoneyTv.setText("¥" + NumberUtil.formartMoney(bean.result.amount));
//      } else {
//        viewBinding.funChatMessageRedPacketViewHolderMoneyTv.setText("");
//      }


        }
    }

    @Override
    protected void onMessageBackgroundConfig(ChatMessageBean messageBean) {
        super.onMessageBackgroundConfig(messageBean);
        viewBinding.getRoot().setBackgroundResource(R.color.title_transfer);
    }
}
