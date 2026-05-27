// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.chatkit.ui.fun.view.message.viewholder;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import androidx.annotation.NonNull;
import com.netease.yunxin.kit.chatkit.ui.R;
import com.netease.yunxin.kit.chatkit.ui.common.ChatZhuanZhangHelper;
import com.netease.yunxin.kit.chatkit.ui.databinding.ChatBaseMessageViewHolderBinding;
import com.netease.yunxin.kit.chatkit.ui.databinding.FunChatMessageZhuanZhangViewHolderBinding;
import com.netease.yunxin.kit.chatkit.ui.model.ChatMessageBean;
import com.yaoxin.appbase.model.CustomMsgBean;
import com.yaoxin.appbase.utils.DataUtil;
import java.util.Map;

public class ChatZhuanZhangMessageViewHolder extends FunChatBaseMessageViewHolder {

  protected FunChatMessageZhuanZhangViewHolderBinding viewBinding;

  public ChatZhuanZhangMessageViewHolder(
      @NonNull ChatBaseMessageViewHolderBinding parent, int viewType) {
    super(parent, viewType);
  }

  @Override
  protected void addViewToMessageContainer() {
    viewBinding =
        FunChatMessageZhuanZhangViewHolderBinding.inflate(
            LayoutInflater.from(parent.getContext()), getMessageContainer(), true);
  }

  @Override
  public void bindData(ChatMessageBean message, ChatMessageBean lastMessage) {
    super.bindData(message, lastMessage);
    CustomMsgBean bean = ChatZhuanZhangHelper.parseFromMessage(message.getMessageData());
    if (bean == null) {
      return;
    }
    Map<String, Object> localExtension = message.getMessageData().getMessage().getLocalExtension();
    boolean hasDraw = false;
    if (localExtension != null
        && DataUtil.getUserid().equals(localExtension.get("userId"))
        && ChatZhuanZhangHelper.isReceiver(bean)) {
      hasDraw = true;
    }
    if (hasDraw) {
      viewBinding.funChatMessageRedPacketViewHolderBgIv.setImageResource(
          R.drawable.chat_zhuanzhang_bg_no_open);
      viewBinding.funChatMessageRedPacketViewHolderBgIv.setImageAlpha(128);
    } else {
      viewBinding.funChatMessageRedPacketViewHolderBgIv.setImageAlpha(255);
      viewBinding.funChatMessageRedPacketViewHolderBgIv.setImageResource(
          R.drawable.chat_zhuanzhang_bg_no_open);
    }

    boolean receiver = ChatZhuanZhangHelper.isReceiver(bean);
    viewBinding.funChatMessageRedPacketViewHolderTypeTv.setText(
        ChatZhuanZhangHelper.getTitleText(parent.getContext(), receiver));

    String subtitle = ChatZhuanZhangHelper.getSubtitleText(parent.getContext(), bean, receiver);
    if (!TextUtils.isEmpty(subtitle)) {
      viewBinding.funChatMessageRedPacketViewHolderGreetingTv.setVisibility(View.VISIBLE);
      viewBinding.funChatMessageRedPacketViewHolderGreetingTv.setText(subtitle);
    } else {
      viewBinding.funChatMessageRedPacketViewHolderGreetingTv.setVisibility(View.GONE);
    }

    viewBinding.funChatMessageRedPacketViewHolderTimeTv.setText(
        parent.getContext().getString(R.string.chat_message_zhuan_zhang));
    viewBinding.funChatMessageRedPacketViewHolderMoneyTv.setVisibility(View.VISIBLE);
    viewBinding.funChatMessageRedPacketViewHolderMoneyTv.setText(
        "¥" + ChatZhuanZhangHelper.formatAmount(bean.amount));
  }

  @Override
  protected void onMessageBackgroundConfig(ChatMessageBean messageBean) {
    super.onMessageBackgroundConfig(messageBean);
    viewBinding.getRoot().setBackgroundResource(R.color.title_transfer);
  }
}
