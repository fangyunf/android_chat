// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.chatkit.ui.model;

import android.text.Spannable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import com.netease.nimlib.sdk.msg.attachment.MsgAttachment;
import com.netease.nimlib.sdk.msg.constant.MsgTypeEnum;
import com.netease.nimlib.sdk.msg.model.IMMessage;
import com.netease.nimlib.sdk.search.model.RecordHitInfo;
import com.netease.yunxin.kit.chatkit.model.IMMessageRecord;
import com.netease.yunxin.kit.chatkit.ui.common.MessageHelper;
import com.netease.yunxin.kit.corekit.im.custom.CustomAttachment;
import com.netease.yunxin.kit.common.ui.viewholder.BaseBean;
import com.netease.yunxin.kit.corekit.im.utils.RouterConstant;
import com.yaoxin.appbase.utils.AESUtil;
import java.util.List;

/** history message search bean used to locate the message */
public class ChatSearchBean extends BaseBean {
  IMMessageRecord msgRecord;
  IMMessage message;
  String searchText;
  String keyword;

  public ChatSearchBean(IMMessageRecord record) {
    this.msgRecord = record;
    this.message = record == null || record.getIndexRecord() == null ? null : record.getIndexRecord().getMessage();
    this.searchText = record == null || record.getIndexRecord() == null
        ? ""
        : decryptSearchText(record.getIndexRecord().getText());
    this.paramKey = RouterConstant.KEY_MESSAGE;
    this.param = getMessage();
    this.router = RouterConstant.PATH_CHAT_TEAM_PAGE;
  }

  public ChatSearchBean(IMMessage message, String keyword) {
    this.message = message;
    this.keyword = keyword;
    this.searchText = buildMessageSummary(message);
    this.paramKey = RouterConstant.KEY_MESSAGE;
    this.param = getMessage();
    this.router = RouterConstant.PATH_CHAT_TEAM_PAGE;
  }

  public String getNickName() {
    if (message != null) {
      return MessageHelper.getChatMessageUserName(message);
    }
    return MessageHelper.getChatSearchMessageUserName(msgRecord);
  }

  public String getAccount() {
    if (message != null) {
      return message.getFromAccount();
    }
    if (msgRecord != null && msgRecord.getIndexRecord() != null) {
      return msgRecord.getIndexRecord().getMessage().getFromAccount();
    }
    return null;
  }

  public long getTime() {
    if (message != null) {
      return message.getTime();
    }
    if (msgRecord != null && msgRecord.getIndexRecord() != null) {
      return msgRecord.getIndexRecord().getTime();
    }
    return 0;
  }

  public String getAvatar() {
    if (msgRecord != null && msgRecord.getFromUser() != null) {
      return msgRecord.getFromUser().getAvatar();
    }
    return null;
  }

  public IMMessage getMessage() {
    return message;
  }

  public List<RecordHitInfo> getHitInfo() {
    if (msgRecord != null && msgRecord.getIndexRecord() != null) {
      return msgRecord.getIndexRecord().getHitInfo();
    }
    return null;
  }

  public SpannableString getSpannableString(int color) {
    if (TextUtils.isEmpty(searchText)) {
      return new SpannableString("");
    }
    SpannableString spannable = new SpannableString(searchText);
    List<RecordHitInfo> hitInfoList = getHitInfo();
    if (hitInfoList != null && !hitInfoList.isEmpty()) {
      int length = searchText.length();
      for (RecordHitInfo hitInfo : hitInfoList) {
        if (hitInfo == null) {
          continue;
        }
        int start = hitInfo.start;
        int end = hitInfo.end + 1;
        if (start >= 0 && end > start && end <= length) {
          spannable.setSpan(
              new ForegroundColorSpan(color), start, end, Spanned.SPAN_INCLUSIVE_INCLUSIVE);
        }
      }
      return spannable;
    }
    if (TextUtils.isEmpty(keyword)) {
      return spannable;
    }
    int startIndex = 0;
    while (startIndex < searchText.length()) {
      int matchIndex = searchText.indexOf(keyword, startIndex);
      if (matchIndex < 0) {
        break;
      }
      int endIndex = matchIndex + keyword.length();
      spannable.setSpan(
          new ForegroundColorSpan(color), matchIndex, endIndex, Spannable.SPAN_INCLUSIVE_INCLUSIVE);
      startIndex = endIndex;
    }
    return spannable;
  }

  public String getSearchText() {
    return searchText == null ? "" : searchText;
  }

  private static String buildMessageSummary(IMMessage message) {
    if (message == null || message.getMsgType() == null) {
      return "";
    }
    MsgTypeEnum msgType = message.getMsgType();
    switch (msgType) {
      case text:
        return decryptSearchText(message.getContent());
      case image:
        return "[图片]";
      case video:
        return "[视频]";
      case file:
        return "[文件]";
      case audio:
        return "[语音]";
      case location:
        return "[位置]";
      case notification:
      case tip:
        return TextUtils.isEmpty(message.getContent()) ? "[通知消息]" : message.getContent();
      case custom:
        MsgAttachment attachment = message.getAttachment();
        if (attachment instanceof CustomAttachment) {
          String content = ((CustomAttachment) attachment).getContent();
          return TextUtils.isEmpty(content) ? "[自定义消息]" : content;
        }
        return "[自定义消息]";
      default:
        return TextUtils.isEmpty(message.getContent()) ? "[消息]" : decryptSearchText(message.getContent());
    }
  }

  private static String decryptSearchText(String rawText) {
    if (TextUtils.isEmpty(rawText)) {
      return "";
    }
    try {
      return AESUtil.msgAseDecrypt(rawText);
    } catch (Exception ignored) {
      return rawText;
    }
  }
}
