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
import com.yaoxin.appbase.model.GroupInfoBean;
import com.yaoxin.appbase.utils.AppProxy;
import com.yaoxin.appbase.utils.DataUtil;

import java.util.List;

public class FunConversationP2PViewHolder extends FunConversationBaseViewHolder {

  public FunConversationP2PViewHolder(@NonNull FunConversationViewHolderBinding binding) {
    super(binding);
  }

  @Override
  public void onBindData(ConversationBean data, int position) {
    super.onBindData(data, position);

    viewBinding.funConversationViewHolderIdTv.setVisibility(View.GONE);

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
//      https://ao/defaultAvatar/8.png

      String name = data.infoData.getName();
      if (data.infoData != null && data.infoData.getAvatar() != null) {
        if (data.infoData.getAvatar().startsWith("https://ao")) {

            viewBinding.avatarView.setData(
                    com.yaoxin.appbase.R.mipmap.app_default_base_icon_geren,
                    data.infoData.getAvatarName(),
                    AvatarColor.avatarColor(data.infoData.getContactId()));
        } else {
            viewBinding.avatarView.setData(
                    data.infoData.getAvatar(),
                    data.infoData.getAvatarName(),
                    AvatarColor.avatarColor(data.infoData.getContactId()));
        }
      }

      viewBinding.nameTv.setText(name);
      List<GroupInfoBean> friendInfoList = DataUtil.getFriendInfoList();
      if (!friendInfoList.isEmpty()) {
        for (GroupInfoBean tempBean :friendInfoList) {
          if (tempBean.userId.equals((String) data.param)) {
            viewBinding.funConversationViewHolderIdTv.setVisibility(View.VISIBLE);
            viewBinding.funConversationViewHolderIdTv.setText("ID:" + tempBean.memberCode);
            break;
          }

        }
      }
    }
    viewBinding.rootLayout.setBackground(viewBinding.rootLayout.getContext().getDrawable(com.yaoxin.appbase.R.drawable.bg_white_rounded_12));

  }
}
