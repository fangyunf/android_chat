// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.chatkit.ui.fun.view.message.viewholder;

import android.view.LayoutInflater;

import androidx.annotation.NonNull;

import com.google.gson.Gson;
import com.netease.yunxin.kit.chatkit.ui.R;
import com.netease.yunxin.kit.chatkit.ui.databinding.ChatBaseMessageViewHolderBinding;
import com.netease.yunxin.kit.chatkit.ui.databinding.FunChatMessageMingpianBinding;
import com.netease.yunxin.kit.chatkit.ui.databinding.FunChatMessageRedPacketViewHolderBinding;
import com.netease.yunxin.kit.chatkit.ui.model.ChatMessageBean;
import com.yaoxin.appbase.model.CustomMsgBean;
import com.yaoxin.appbase.utils.DataUtil;
import com.yaoxin.appbase.utils.GlideUtil;
import com.yaoxin.appbase.utils.NumberUtil;
import com.yaoxin.appbase.utils.TimeUtil;

import java.util.Map;

public class ChatMingPianMessageViewHolder extends FunChatBaseMessageViewHolder {

  protected FunChatMessageMingpianBinding viewBinding;

  public ChatMingPianMessageViewHolder(
      @NonNull ChatBaseMessageViewHolderBinding parent, int viewType) {
    super(parent, viewType);
  }

  @Override
  protected void addViewToMessageContainer() {
    viewBinding =
            FunChatMessageMingpianBinding.inflate(
            LayoutInflater.from(parent.getContext()), getMessageContainer(), true);
  }

  @Override
  public void bindData(ChatMessageBean message, ChatMessageBean lastMessage) {
    super.bindData(message, lastMessage);
    if (message != null
            && message.getMessageData() != null
            && !message.getMessageData().getMessage().getAttachStr().isEmpty()){
      Map<String, Object> localExtension = message.getMessageData().getMessage().getLocalExtension();

      try {

        CustomMsgBean bean = new Gson().fromJson(message.getMessageData().getMessage().getAttachStr(), CustomMsgBean.class);
        bean.result = new Gson().fromJson(bean.data, CustomMsgBean.class);
        GlideUtil.yh_loadImage(viewBinding.funChatMessageMingpianHeadIv.getContext(),viewBinding.funChatMessageMingpianHeadIv,bean.result.avatar);
        viewBinding.funChatMessageMingpianNameTv.setText(bean.result.name);
        viewBinding.funChatMessageMingpianIdTv.setText(bean.result.memberCode);

      } catch (Exception e) {

      }
    }
  }

  @Override
  protected void onMessageBackgroundConfig(ChatMessageBean messageBean) {
    super.onMessageBackgroundConfig(messageBean);
    viewBinding.getRoot().setBackgroundResource(com.yaoxin.appbase.R.drawable.bg_white_rounded_10);
  }
}
