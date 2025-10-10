// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.chatkit.ui.fun.view.message.viewholder;

import android.view.LayoutInflater;

import androidx.annotation.NonNull;

import com.google.gson.Gson;
import com.netease.yunxin.kit.chatkit.ui.R;
import com.netease.yunxin.kit.chatkit.ui.databinding.ChatBaseMessageViewHolderBinding;
import com.netease.yunxin.kit.chatkit.ui.databinding.FunChatMessageZhuanZhangViewHolderBinding;
import com.netease.yunxin.kit.chatkit.ui.model.ChatMessageBean;
import com.yaoxin.appbase.model.CustomMsgBean;
import com.yaoxin.appbase.utils.DataUtil;
import com.yaoxin.appbase.utils.NumberUtil;
import com.yaoxin.appbase.utils.TimeUtil;

import java.util.Map;

public class ChatZhuanZhangMessageViewHolder extends FunChatBaseMessageViewHolder {

    protected FunChatMessageZhuanZhangViewHolderBinding viewBinding;

    public ChatZhuanZhangMessageViewHolder(
            @NonNull ChatBaseMessageViewHolderBinding parent, int viewType) {
        super(parent, viewType);
    }

    @Override
    protected void addViewToMessageContainer() {
        viewBinding = FunChatMessageZhuanZhangViewHolderBinding.inflate(
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
                viewBinding.funChatMessageRedPacketViewHolderBgIv.setImageResource(R.drawable.chat_zhuanzhang_bg_is_open);
            } else {
                viewBinding.funChatMessageRedPacketViewHolderBgIv.setImageResource(R.drawable.chat_zhuanzhang_bg_no_open);
            }
            if (bean.result.toUserId.equals(DataUtil.getUserid())) {
                viewBinding.funChatMessageRedPacketViewHolderGreetingTv.setText("你收到了一笔转账");
            } else {
                viewBinding.funChatMessageRedPacketViewHolderGreetingTv.setText("你发起了一笔转账");
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
