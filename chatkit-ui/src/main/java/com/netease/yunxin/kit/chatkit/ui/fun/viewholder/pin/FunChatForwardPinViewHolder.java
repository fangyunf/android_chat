// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.chatkit.ui.fun.viewholder.pin;

import android.view.LayoutInflater;
import android.view.View;
import androidx.annotation.NonNull;
import com.netease.yunxin.kit.chatkit.ui.ChatBriefUtils;
import com.netease.yunxin.kit.chatkit.ui.R;
import com.netease.yunxin.kit.chatkit.ui.custom.MultiForwardAttachment;
import com.netease.yunxin.kit.chatkit.ui.databinding.FunChatBasePinViewHolderBinding;
import com.netease.yunxin.kit.chatkit.ui.databinding.FunChatForwardPinViewHolderBinding;
import com.netease.yunxin.kit.chatkit.ui.model.ChatMessageBean;

/** view holder for Text message */
public class FunChatForwardPinViewHolder extends FunChatBasePinViewHolder {

  FunChatForwardPinViewHolderBinding viewBinding;

  public FunChatForwardPinViewHolder(
      @NonNull FunChatBasePinViewHolderBinding parent, int viewType) {
    super(parent, viewType);
  }

  @Override
  public void addContainer() {
    viewBinding =
        FunChatForwardPinViewHolderBinding.inflate(
            LayoutInflater.from(parent.getContext()), getContainer(), true);
    viewBinding.messageText.setOnClickListener(
        v -> itemListener.onViewClick(v, position, currentMessage));
  }

  @Override
  public void onBindData(ChatMessageBean message, int position) {
    super.onBindData(message, position);
    baseViewBinding.dividerLine.setVisibility(View.GONE);
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
