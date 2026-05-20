// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.chatkit.ui.model;

import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import com.netease.nimlib.sdk.msg.model.IMMessage;
import com.netease.nimlib.sdk.search.model.RecordHitInfo;
import com.netease.yunxin.kit.chatkit.model.IMMessageRecord;
import com.netease.yunxin.kit.chatkit.ui.common.MessageHelper;
import com.netease.yunxin.kit.common.ui.viewholder.BaseBean;
import com.netease.yunxin.kit.corekit.im.utils.RouterConstant;
import com.yaoxin.appbase.utils.AESUtil;
import java.util.List;

/** history message search bean used to locate the message */
public class ChatSearchBean extends BaseBean {
  IMMessageRecord msgRecord;

  public ChatSearchBean(IMMessageRecord record) {
    this.msgRecord = record;
    this.paramKey = RouterConstant.KEY_MESSAGE;
    this.param = getMessage();
    this.router = RouterConstant.PATH_CHAT_TEAM_PAGE;
  }

  public String getNickName() {
    return MessageHelper.getChatSearchMessageUserName(msgRecord);
  }

  public String getAccount() {
    if (msgRecord != null) {
      return msgRecord.getIndexRecord().getMessage().getFromAccount();
    }
    return null;
  }

  public long getTime() {
    if (msgRecord != null) {
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
    if (msgRecord != null) {
      return msgRecord.getIndexRecord().getMessage();
    }
    return null;
  }

  public List<RecordHitInfo> getHitInfo() {
    if (msgRecord != null) {
      return msgRecord.getIndexRecord().getHitInfo();
    }
    return null;
  }

  public SpannableString getSpannableString(int color) {
    if (msgRecord == null || msgRecord.getIndexRecord() == null) {
      return new SpannableString("");
    }
    String text = decryptSearchText(msgRecord.getIndexRecord().getText());
    SpannableString spannable = new SpannableString(text);
    List<RecordHitInfo> hitInfoList = msgRecord.getIndexRecord().getHitInfo();
    if (hitInfoList == null) {
      return spannable;
    }
    int length = text.length();
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
