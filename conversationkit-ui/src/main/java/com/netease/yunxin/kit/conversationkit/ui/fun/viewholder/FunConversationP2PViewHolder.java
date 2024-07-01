// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.conversationkit.ui.fun.viewholder;

import android.view.View;

import androidx.annotation.NonNull;
import com.netease.yunxin.kit.common.ui.utils.AvatarColor;
import com.netease.yunxin.kit.conversationkit.ui.R;
import com.netease.yunxin.kit.conversationkit.ui.databinding.FunConversationViewHolderBinding;
import com.netease.yunxin.kit.conversationkit.ui.model.ConversationBean;
import com.yaoxin.appbase.utils.AppProxy;
import com.yaoxin.appbase.utils.DataUtil;

public class FunConversationP2PViewHolder extends FunConversationBaseViewHolder {

  public FunConversationP2PViewHolder(@NonNull FunConversationViewHolderBinding binding) {
    super(binding);
  }

  @Override
  public void onBindData(ConversationBean data, int position) {
    super.onBindData(data, position);

    if (data.param.equals(DataUtil.getKeFuId())) {

      String name = "客服";
      viewBinding.avatarView.setData(
              com.yaoxin.appbase.R.mipmap.app_default_base_icon_kefu,
              data.infoData.getAvatarName(),
              AvatarColor.avatarColor(data.infoData.getContactId()));
      viewBinding.nameTv.setText(name);
    } else if (data.param.equals(DataUtil.getXiaoZhuShouId())) {

      String name = "小助手";
      viewBinding.avatarView.setData(
              com.yaoxin.appbase.R.mipmap.app_default_base_icon_xiaozhushou,
              data.infoData.getAvatarName(),
              AvatarColor.avatarColor(data.infoData.getContactId()));
      viewBinding.nameTv.setText(name);
    } else {

      String name = data.infoData.getName();
      viewBinding.avatarView.setData(
              data.infoData.getAvatar(),
              data.infoData.getAvatarName(),
              AvatarColor.avatarColor(data.infoData.getContactId()));
      viewBinding.nameTv.setText(name);
    }
  }
}
