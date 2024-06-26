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
import com.yaoxin.appbase.utils.NumberUtil;
import com.yaoxin.appbase.utils.TimeUtil;

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
            && !message.getMessageData().getMessage().getAttachStr().isEmpty()){

      CustomMsgBean bean = new Gson().fromJson(message.getMessageData().getMessage().getAttachStr(), CustomMsgBean.class);
      bean.result = new Gson().fromJson(bean.data, CustomMsgBean.class);
      viewBinding.funChatMessageRedPacketViewHolderBgIv.setImageResource( (bean.type == 21) ? R.drawable.chat_redpacket_purple_bg_no_open:R.drawable.chat_red_packet_cell_bg_no_open);
      viewBinding.funChatMessageRedPacketViewHolderGreetingTv.setText(bean.result.title);
      viewBinding.funChatMessageRedPacketViewHolderTimeTv.setText(TimeUtil.stampToDate(bean.result.createTime));
      viewBinding.funChatMessageRedPacketViewHolderMoneyTv.setText(NumberUtil.formartMoney(bean.result.amount));


    }
  }

  @Override
  protected void onMessageBackgroundConfig(ChatMessageBean messageBean) {
    super.onMessageBackgroundConfig(messageBean);
    viewBinding.getRoot().setBackgroundResource(R.color.title_transfer);
  }
}
