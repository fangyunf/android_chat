// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.chatkit.ui;

import android.content.Context;
import android.text.TextUtils;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.netease.nimlib.sdk.msg.attachment.NetCallAttachment;
import com.netease.nimlib.sdk.msg.constant.MsgTypeEnum;
import com.netease.yunxin.kit.chatkit.model.IMMessageInfo;
import com.netease.yunxin.kit.chatkit.ui.common.ChatUtils;
import com.netease.yunxin.kit.chatkit.ui.custom.MultiForwardAttachment;
import com.netease.yunxin.kit.chatkit.ui.custom.RichTextAttachment;
import com.netease.yunxin.kit.corekit.im.custom.CustomAttachment;
import com.netease.yunxin.kit.corekit.im.model.AttachmentContent;
import com.yaoxin.appbase.utils.AESUtil;
import java.util.List;

/** 获取消息的简要信息，合并转发中展示的消息内容 */
public class ChatBriefUtils {

  /** 合并转发摘要、气泡预览等场景下的文本解密（兼容双重加密历史数据） */
  @NonNull
  public static String decryptBriefText(@Nullable String raw) {
    if (TextUtils.isEmpty(raw)) {
      return "";
    }
    String result = AESUtil.safeMsgDecrypt(raw);
    if (!TextUtils.equals(result, raw)
        && !AESUtil.containsChineseCharacters(result)
        && result.length() >= 8) {
      String second = AESUtil.safeMsgDecrypt(result);
      if (!TextUtils.isEmpty(second)) {
        result = second;
      }
    }
    return result;
  }

  public static String customContentText(Context context, IMMessageInfo messageInfo) {
    if (messageInfo != null && context != null) {
      MsgTypeEnum typeEnum = messageInfo.getMessage().getMsgType();
      switch (typeEnum) {
        case notification:
          return context.getString(R.string.msg_type_notification);
        case text:
          return decryptBriefText(messageInfo.getMessage().getContent());
        case audio:
          return context.getString(R.string.msg_type_audio);
        case video:
          return context.getString(R.string.msg_type_video);
        case tip:
          return context.getString(R.string.msg_type_tip);
        case image:
          return context.getString(R.string.msg_type_image);
        case file:
          return context.getString(R.string.msg_type_file);
        case location:
          return context.getString(R.string.msg_type_location);
        case nrtc_netcall:
          NetCallAttachment attachment =
              (NetCallAttachment) messageInfo.getMessage().getAttachment();
          int type = attachment.getType();
          if (type == 1) {
            return context.getString(R.string.msg_type_rtc_audio);
          } else {
            return context.getString(R.string.msg_type_rtc_video);
          }
        case custom:
          if (messageInfo.getMessage().getAttachment() instanceof MultiForwardAttachment) {
            return context.getString(R.string.chat_message_multi_record);
          }
          if (messageInfo.getMessage().getAttachment() instanceof RichTextAttachment) {
            RichTextAttachment richTextAttachment =
                (RichTextAttachment) messageInfo.getMessage().getAttachment();
            String title = decryptBriefText(richTextAttachment.title);
            String body = decryptBriefText(richTextAttachment.body);
            if (!TextUtils.isEmpty(title) && !TextUtils.isEmpty(body)) {
              return title + ": " + body;
            }
            return !TextUtils.isEmpty(body) ? body : title;
          }
          if (messageInfo.getMessage().getAttachment() instanceof CustomAttachment) {
            return decryptBriefText(
                ((CustomAttachment) messageInfo.getMessage().getAttachment()).getContent());
          }
          if (messageInfo.getMessage().getAttachment() instanceof AttachmentContent) {
            return decryptBriefText(
                ((AttachmentContent) messageInfo.getMessage().getAttachment()).getContent());
          }
          return decryptBriefText(messageInfo.getMessage().getContent());
        default:
          return context.getString(R.string.msg_type_no_tips);
      }
    }
    return "";
  }

  /** 合并转发卡片上的摘要预览文案 */
  @NonNull
  public static String buildForwardAbstractPreview(
      @NonNull Context context, @Nullable List<MultiForwardAttachment.Abstracts> abstractsList) {
    if (abstractsList == null || abstractsList.isEmpty()) {
      return "";
    }
    String contentFormat = context.getString(R.string.chat_message_multi_record_content);
    StringBuilder textBuilder = new StringBuilder();
    for (int i = 0; i < abstractsList.size(); i++) {
      MultiForwardAttachment.Abstracts item = abstractsList.get(i);
      textBuilder.append(
          String.format(
              contentFormat,
              ChatUtils.getEllipsizeMiddleNick(item.senderNick),
              decryptBriefText(item.content)));
      if (i < abstractsList.size() - 1) {
        textBuilder.append('\n');
      }
    }
    return textBuilder.toString();
  }
}
