// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.chatkit.ui.custom;

import com.netease.yunxin.kit.chatkit.ui.ChatMessageType;

/** 群内转账自定义消息，type=121，展示与个人转账一致 */
public class GroupZhuanZhangAttachment extends ZhuanZhangAttachment {

  public GroupZhuanZhangAttachment() {
    super(ChatMessageType.GROUP_ZHUANZHANG_ATTACHMENT);
  }
}
