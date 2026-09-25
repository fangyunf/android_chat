// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.chatkit.ui.fun.view.message.viewholder;

import android.text.TextUtils;
import android.view.LayoutInflater;

import androidx.annotation.NonNull;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.netease.yunxin.kit.chatkit.ui.databinding.ChatBaseMessageViewHolderBinding;
import com.netease.yunxin.kit.chatkit.ui.databinding.FunChatMessageMingpianBinding;
import com.netease.yunxin.kit.chatkit.ui.model.ChatMessageBean;
import com.yaoxin.appbase.model.CustomMsgBean;
import com.yaoxin.appbase.utils.GlideUtil;

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
    if (message == null
        || message.getMessageData() == null
        || message.getMessageData().getMessage() == null) {
      return;
    }
    String attachStr = message.getMessageData().getMessage().getAttachStr();
    if (TextUtils.isEmpty(attachStr)) {
      return;
    }
    try {
      JsonObject root = JsonParser.parseString(attachStr).getAsJsonObject();
      if (!root.has("data") || root.get("data").isJsonNull()) {
        return;
      }
      JsonElement dataEl = root.get("data");
      String dataJson = dataEl.isJsonPrimitive() ? dataEl.getAsString() : dataEl.toString();
      CustomMsgBean bean = new Gson().fromJson(dataJson, CustomMsgBean.class);
      if (bean == null) {
        return;
      }
      GlideUtil.yh_loadImage(
          viewBinding.funChatMessageMingpianHeadIv.getContext(),
          viewBinding.funChatMessageMingpianHeadIv,
          bean.avatar);
      viewBinding.funChatMessageMingpianNameTv.setText(bean.name);
    } catch (Exception ignored) {
    }
  }

  @Override
  protected void onMessageBackgroundConfig(ChatMessageBean messageBean) {
    super.onMessageBackgroundConfig(messageBean);
    viewBinding.getRoot().setBackgroundResource(com.yaoxin.appbase.R.drawable.bg_white_rounded_10);
  }
}
