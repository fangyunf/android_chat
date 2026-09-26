// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.chatkit.ui.custom;

import com.netease.yunxin.kit.chatkit.ui.ChatMessageType;

/** 兼容历史错误 type=121 的群转账消息 */
public class GroupZhuanZhangLegacyAttachment extends ZhuanZhangAttachment {

  public GroupZhuanZhangLegacyAttachment() {
    super(ChatMessageType.GROUP_ZHUANZHANG_ATTACHMENT_LEGACY);
  }
}
