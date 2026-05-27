// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.chatkit.ui.normal.viewholder.pin;

import android.view.LayoutInflater;
import androidx.annotation.NonNull;
import com.netease.yunxin.kit.chatkit.ui.ChatBriefUtils;
import com.netease.yunxin.kit.chatkit.ui.R;
import com.netease.yunxin.kit.chatkit.ui.custom.MultiForwardAttachment;
import com.netease.yunxin.kit.chatkit.ui.databinding.ChatBasePinViewHolderBinding;
import com.netease.yunxin.kit.chatkit.ui.databinding.ChatForwardPinViewHolderBinding;
import com.netease.yunxin.kit.chatkit.ui.model.ChatMessageBean;

/** view holder for Text message */
public class ChatForwardPinViewHolder extends ChatBasePinViewHolder {

  ChatForwardPinViewHolderBinding viewBinding;

  public ChatForwardPinViewHolder(@NonNull ChatBasePinViewHolderBinding parent, int viewType) {
    super(parent, viewType);
  }

  @Override
  public void addContainer() {
    viewBinding =
        ChatForwardPinViewHolderBinding.inflate(
            LayoutInflater.from(parent.getContext()), getContainer(), true);
    viewBinding.messageText.setOnClickListener(
        v -> itemListener.onViewClick(v, position, currentMessage));
  }

  @Override
  public void onBindData(ChatMessageBean message, int position) {
    super.onBindData(message, position);
    if (message != null
        && message.getMessageData() != null
        && message.getMessageData().getMessage().getAttachment()
            instanceof MultiForwardAttachment) {
      MultiForwardAttachment attachment =
          (MultiForwardAttachment) message.getMessageData().getMessage().getAttachment();
      String titleText =
          String.format(
              getContainer().getContext().getString(R.string.chat_message_multi_record_title),
              attachment.sessionName);
      viewBinding.messageMultiTitle.setText(titleText);
      if (attachment.abstractsList != null) {
        viewBinding.messageText.setText(
            ChatBriefUtils.buildForwardAbstractPreview(
                getContainer().getContext(), attachment.abstractsList));
      }
    }
  }
}
