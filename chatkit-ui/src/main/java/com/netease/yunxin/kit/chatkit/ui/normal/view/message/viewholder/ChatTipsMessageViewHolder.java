// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.chatkit.ui.normal.view.message.viewholder;

import static com.netease.yunxin.kit.corekit.im.utils.RouterConstant.KEY_TEAM_CREATED_TIP;

import android.text.SpannableString;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import com.google.gson.Gson;
import com.netease.yunxin.kit.chatkit.ui.ChatKitUIConstant;
import com.netease.yunxin.kit.chatkit.ui.R;
import com.netease.yunxin.kit.chatkit.ui.databinding.ChatBaseMessageViewHolderBinding;
import com.netease.yunxin.kit.chatkit.ui.databinding.NormalChatMessageTipViewHolderBinding;
import com.netease.yunxin.kit.chatkit.ui.model.ChatMessageBean;
import com.netease.yunxin.kit.chatkit.ui.common.MessageHelper;
import com.netease.yunxin.kit.corekit.im.IMKitClient;
import com.netease.yunxin.kit.corekit.im.model.UserInfo;
import com.netease.yunxin.kit.corekit.route.XKitRouter;
import com.yaoxin.appbase.model.UserBean;
import java.util.Map;

/** view holder for Text message */
public class ChatTipsMessageViewHolder extends NormalChatBaseMessageViewHolder {

  private static final String TAG = "ChatTipsMessageViewHolder";

  NormalChatMessageTipViewHolderBinding textBinding;

  public ChatTipsMessageViewHolder(@NonNull ChatBaseMessageViewHolderBinding parent, int viewType) {
    super(parent, viewType);
  }

  @Override
  public void addViewToMessageContainer() {
    textBinding =
        NormalChatMessageTipViewHolderBinding.inflate(
            LayoutInflater.from(parent.getContext()), getMessageContainer(), true);
  }

  @Override
  protected void onMessageBackgroundConfig(ChatMessageBean messageBean) {
    baseViewBinding.contentWithTopLayer.setBackgroundResource(R.color.title_transfer);
  }

  @Override
  protected void onLayoutConfig(ChatMessageBean messageBean) {
    ConstraintLayout.LayoutParams messageContainerLayoutParams =
        (ConstraintLayout.LayoutParams) baseViewBinding.messageContainer.getLayoutParams();
    ConstraintLayout.LayoutParams messageTopLayoutParams =
        (ConstraintLayout.LayoutParams) baseViewBinding.messageTopGroup.getLayoutParams();
    ConstraintLayout.LayoutParams messageBottomLayoutParams =
        (ConstraintLayout.LayoutParams) baseViewBinding.messageBottomGroup.getLayoutParams();
    messageContainerLayoutParams.horizontalBias = 0.5f;
    messageTopLayoutParams.horizontalBias = 0.5f;
    messageBottomLayoutParams.horizontalBias = 0.5f;
  }

  @Override
  protected void onCommonViewVisibleConfig(ChatMessageBean messageBean) {
    baseViewBinding.otherUsername.setVisibility(View.GONE);
    baseViewBinding.otherUserAvatar.setVisibility(View.GONE);
    baseViewBinding.otherUserAvatarRole.setVisibility(View.GONE);
    baseViewBinding.myAvatar.setVisibility(View.GONE);
    baseViewBinding.myName.setVisibility(View.GONE);
    baseViewBinding.messageStatus.setVisibility(View.GONE);

    baseViewBinding.chatBaseMessageViewHolderMineGradeIv.setVisibility(View.GONE);
    baseViewBinding.chatBaseMessageViewHolderOtherGradeIv.setVisibility(View.GONE);
  }

  @Override
  public void bindData(ChatMessageBean message, ChatMessageBean lastMessage) {
    super.bindData(message, lastMessage);
    loadData(message, lastMessage, true);
  }

  private void loadData(ChatMessageBean message, ChatMessageBean lastMessage, boolean refreshTime) {
    String content = message.getMessageData().getMessage().getContent();
    if (content == null || content.isEmpty()) {
      // create team tip
      Map<String, Object> extension = MessageHelper.safeGetRemoteExtension(message.getMessageData().getMessage());
      if (extension != null && extension.get(KEY_TEAM_CREATED_TIP) != null) {
        content = extension.get(KEY_TEAM_CREATED_TIP).toString();
      }
    }
    if (content != null && !content.isEmpty()) {
      textBinding.messageTipText.setGravity(Gravity.CENTER);
      textBinding.messageTipText.setTextColor(
          IMKitClient.getApplicationContext().getResources().getColor(R.color.color_999999));
      textBinding.messageTipText.setTextSize(12);
      // 非好友 tip：仅「添加」文字可点击，跳转添加好友详情页
      Map<String, Object> localExt = message.getMessageData().getMessage().getLocalExtension();
      if (localExt != null && Boolean.TRUE.equals(localExt.get(ChatKitUIConstant.KEY_ADD_FRIEND_TIP))) {
        String addFriendClickText = "添加";
        int start = content.indexOf(addFriendClickText);
        if (start >= 0) {
          SpannableString spannable = new SpannableString(content);
          final String sessionId = message.getMessageData().getMessage().getSessionId();
          ClickableSpan clickableSpan =
              new ClickableSpan() {
                @Override
                public void onClick(@NonNull View widget) {
                  String account = sessionId;
                  UserInfo userInfo = MessageHelper.getChatMessageUserInfo(account);
                  UserBean userBean = new UserBean();
                  userBean.userId = account;
                  userBean.memberCode = account;
                  userBean.name = userInfo != null && userInfo.getName() != null ? userInfo.getName() : account;
                  userBean.avatar = userInfo != null && userInfo.getAvatar() != null ? userInfo.getAvatar() : "";
                  String userJson = new Gson().toJson(userBean);
                  XKitRouter.withKey(com.yaoxin.appbase.net.Constant.FunAddFriendVerifyActivityKey)
                      .withContext(widget.getContext())
                      .withParam("user", userJson)
                      .navigate();
                }
              };
          spannable.setSpan(
              clickableSpan, start, start + addFriendClickText.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
          textBinding.messageTipText.setMovementMethod(LinkMovementMethod.getInstance());
          textBinding.messageTipText.setText(spannable);
        } else {
          textBinding.messageTipText.setText(content);
        }
        baseViewBinding.baseRoot.setClickable(false);
        baseViewBinding.baseRoot.setOnClickListener(null);
      } else {
        textBinding.messageTipText.setText(content);
        baseViewBinding.baseRoot.setClickable(false);
        baseViewBinding.baseRoot.setOnClickListener(null);
      }
    } else {
      baseViewBinding.baseRoot.setVisibility(View.GONE);
    }
  }

  @Override
  protected boolean needMessageClickAndExtra() {
    return false;
  }

  @Override
  protected boolean needShowTimeView(ChatMessageBean message, ChatMessageBean lastMessage) {
    return lastMessage == null;
  }
}
