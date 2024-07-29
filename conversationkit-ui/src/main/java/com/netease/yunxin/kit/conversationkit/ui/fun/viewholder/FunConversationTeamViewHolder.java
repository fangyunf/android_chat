// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.conversationkit.ui.fun.viewholder;

import android.annotation.SuppressLint;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import com.netease.nimlib.sdk.team.model.Team;
import com.netease.yunxin.kit.common.ui.utils.AvatarColor;
import com.netease.yunxin.kit.common.utils.SizeUtils;
import com.netease.yunxin.kit.conversationkit.ui.R;
import com.netease.yunxin.kit.conversationkit.ui.common.ConversationConstant;
import com.netease.yunxin.kit.conversationkit.ui.common.ConversationHelper;
import com.netease.yunxin.kit.conversationkit.ui.databinding.FunConversationViewHolderBinding;
import com.netease.yunxin.kit.conversationkit.ui.model.ConversationBean;
import com.yaoxin.appbase.utils.AppProxy;
import com.yaoxin.appbase.utils.DataUtil;

public class FunConversationTeamViewHolder extends FunConversationBaseViewHolder {

  public FunConversationTeamViewHolder(@NonNull FunConversationViewHolderBinding binding) {
    super(binding);
  }

  @SuppressLint("UseCompatLoadingForDrawables")
  @Override
  public void onBindData(ConversationBean data, int position) {
    super.onBindData(data, position);

    viewBinding.funConversationViewHolderIdTv.setVisibility(View.GONE);
    if (data.infoData.getTeamInfo() != null) {
      Team teamInfo = data.infoData.getTeamInfo();

      if (teamInfo.getIcon().startsWith("https://s.netease.im") || teamInfo.getIcon().isEmpty()) {

        viewBinding.avatarView.setData(
                com.yaoxin.appbase.R.mipmap.app_default_base_icon_group, teamInfo.getName(), AvatarColor.avatarColor(teamInfo.getId()));
      } else {
        viewBinding.avatarView.setData(
                teamInfo.getIcon(), teamInfo.getName(), AvatarColor.avatarColor(teamInfo.getId()));
      }

//      viewBinding.avatarView.setData(
//          teamInfo.getIcon(), teamInfo.getName(), AvatarColor.avatarColor(teamInfo.getId()));
      viewBinding.nameTv.setText(teamInfo.getName());
    }
    if (data.viewType == ConversationConstant.ViewType.TEAM_VIEW
        && data.infoData.getUnreadCount() > 0
        && ConversationHelper.hasAit(data.infoData.getContactId())) {
      viewBinding.aitTv.setVisibility(View.VISIBLE);
    } else {
      viewBinding.aitTv.setVisibility(View.GONE);
    }
//    viewBinding.rootLayout.setBackgroundColor(viewBinding.rootLayout.getContext().getResources().getColor(com.yaoxin.appbase.R.color.app_theme_color));
//    viewBinding.rootLayout.setBackground(viewBinding.rootLayout.getContext().getDrawable(com.yaoxin.appbase.R.drawable.bg_white_rounded_12));

    ViewGroup.MarginLayoutParams layoutParams = (ViewGroup.MarginLayoutParams) viewBinding.rootLayout.getLayoutParams();
    if (AppProxy.getInstance().showType == 1) {
      layoutParams.topMargin = 0;
      layoutParams.height = 0;
    } else {
      layoutParams.height = SizeUtils.dp2px(72);
      layoutParams.topMargin = SizeUtils.dp2px(0);
    }
    viewBinding.rootLayout.setLayoutParams(layoutParams);
  }
}
