// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.chatkit.ui.common;

import android.content.Context;
import android.text.TextUtils;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.gson.Gson;
import com.netease.nimlib.sdk.msg.model.IMMessage;
import com.netease.yunxin.kit.chatkit.model.IMMessageInfo;
import com.netease.yunxin.kit.chatkit.ui.ChatMessageType;
import com.netease.yunxin.kit.chatkit.ui.R;
import com.netease.yunxin.kit.chatkit.ui.fun.page.FunZhuanZhangResultActivity;
import com.yaoxin.appbase.model.CustomMsgBean;
import com.yaoxin.appbase.utils.DataUtil;
import java.util.HashMap;
import java.util.Map;

/** 个人转账(28) / 群内转账(121) 解析与跳转 */
public final class ChatZhuanZhangHelper {

  private ChatZhuanZhangHelper() {}

  public static boolean isZhuanZhangMessageType(int type) {
    return type == ChatMessageType.ZhuanZhang_ATTACHMENT
        || type == ChatMessageType.GROUP_ZHUANZHANG_ATTACHMENT;
  }

  /** 自定义消息 viewType：121 与 28 共用转账卡片 */
  public static int toZhuanZhangViewType(int type) {
    return isZhuanZhangMessageType(type) ? ChatMessageType.ZhuanZhang_ATTACHMENT : type;
  }

  @Nullable
  public static CustomMsgBean parseFromMessage(@Nullable IMMessageInfo messageInfo) {
    if (messageInfo == null || messageInfo.getMessage() == null) {
      return null;
    }
    IMMessage message = messageInfo.getMessage();
    String attachStr = message.getAttachStr();
    if (TextUtils.isEmpty(attachStr) || !attachStr.contains("type")) {
      return null;
    }
    try {
      CustomMsgBean msgBean = new Gson().fromJson(attachStr, CustomMsgBean.class);
      if (msgBean == null || !isZhuanZhangMessageType(msgBean.type)) {
        return null;
      }
      if (!TextUtils.isEmpty(msgBean.data)) {
        msgBean.result = new Gson().fromJson(msgBean.data, CustomMsgBean.class);
      }
      CustomMsgBean detail = msgBean.result != null ? msgBean.result : msgBean;
      mergeOuterFields(msgBean, detail);
      normalizeDetail(detail);
      return detail;
    } catch (Exception ignored) {
      return null;
    }
  }

  /** 兼容服务端字段差异，避免详情页空指针 */
  public static void normalizeDetail(@Nullable CustomMsgBean bean) {
    if (bean == null) {
      return;
    }
    if (TextUtils.isEmpty(bean.sendName) && !TextUtils.isEmpty(bean.sendUserName)) {
      bean.sendName = bean.sendUserName;
    }
    if (TextUtils.isEmpty(bean.toUserName) && !TextUtils.isEmpty(bean.receiveUserName)) {
      bean.toUserName = bean.receiveUserName;
    }
    if (TextUtils.isEmpty(bean.toUserId) && !TextUtils.isEmpty(bean.receiveUserId)) {
      bean.toUserId = bean.receiveUserId;
    }
    if (TextUtils.isEmpty(bean.toUserId) && !TextUtils.isEmpty(bean.userId)) {
      bean.toUserId = bean.userId;
    }
    if (TextUtils.isEmpty(bean.sendAvatar) && !TextUtils.isEmpty(bean.avatar)) {
      bean.sendAvatar = bean.avatar;
    }
    if (TextUtils.isEmpty(bean.fromUserId) && !TextUtils.isEmpty(bean.sendUserId)) {
      bean.fromUserId = bean.sendUserId;
    }
    if (TextUtils.isEmpty(bean.sendName) && !TextUtils.isEmpty(bean.name)) {
      bean.sendName = bean.name;
    }
  }

  public static boolean isReceiver(@Nullable CustomMsgBean bean) {
    return bean != null && TextUtils.equals(bean.toUserId, DataUtil.getUserid());
  }

  @NonNull
  public static String getCounterpartyName(@Nullable CustomMsgBean bean, boolean receiver) {
    if (bean == null) {
      return "";
    }
    if (receiver) {
      return firstNonEmpty(bean.sendName, bean.sendUserName, bean.name);
    }
    return firstNonEmpty(bean.toUserName, bean.receiveUserName);
  }

  @NonNull
  public static String getTitleText(@NonNull Context context, boolean receiver) {
    return context.getString(
        receiver ? R.string.chat_zhuanzhang_received_title : R.string.chat_zhuanzhang_sent_title);
  }

  @NonNull
  public static String getSubtitleText(
      @NonNull Context context, @Nullable CustomMsgBean bean, boolean receiver) {
    String name = getCounterpartyName(bean, receiver);
    if (TextUtils.isEmpty(name)) {
      return "";
    }
    return context.getString(receiver ? R.string.chat_zhuanzhang_from : R.string.chat_zhuanzhang_to, name);
  }

  /** 金额展示，兼容分/元及非法字符串 */
  @NonNull
  public static String formatAmount(@Nullable String amount) {
    if (TextUtils.isEmpty(amount)) {
      return "0.00";
    }
    try {
      return com.yaoxin.appbase.utils.NumberUtil.formartMoney(amount);
    } catch (Exception e) {
      try {
        double value = Double.parseDouble(amount);
        if (value >= 100) {
          return String.format("%.2f", value / 100);
        }
        return String.format("%.2f", value);
      } catch (Exception ignored) {
        return "0.00";
      }
    }
  }

  private static void mergeOuterFields(@NonNull CustomMsgBean outer, @NonNull CustomMsgBean detail) {
    if (TextUtils.isEmpty(detail.sendName)) {
      detail.sendName = outer.sendName;
    }
    if (TextUtils.isEmpty(detail.sendAvatar)) {
      detail.sendAvatar = outer.sendAvatar;
    }
    if (TextUtils.isEmpty(detail.toUserId)) {
      detail.toUserId = outer.toUserId;
    }
    if (TextUtils.isEmpty(detail.amount)) {
      detail.amount = outer.amount;
    }
  }

  public static void openZhuanZhangDetail(@Nullable Context context, @Nullable CustomMsgBean detail) {
    if (context == null || detail == null) {
      return;
    }
    normalizeDetail(detail);
    Map<String, String> map = new HashMap<>();
    map.put("bean", new Gson().toJson(detail));
    FunZhuanZhangResultActivity.start(FunZhuanZhangResultActivity.class, context, map);
  }

  @NonNull
  private static String firstNonEmpty(String... values) {
    if (values == null) {
      return "";
    }
    for (String value : values) {
      if (!TextUtils.isEmpty(value)) {
        return value;
      }
    }
    return "";
  }
}
