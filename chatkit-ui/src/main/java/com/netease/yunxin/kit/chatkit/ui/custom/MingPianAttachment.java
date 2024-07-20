// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.chatkit.ui.custom;

import static com.netease.yunxin.kit.chatkit.ui.ChatKitUIConstant.KEY_RICH_TEXT_BODY;
import static com.netease.yunxin.kit.chatkit.ui.ChatKitUIConstant.KEY_RICH_TEXT_TITLE;

import androidx.annotation.Nullable;

import com.google.gson.Gson;
import com.netease.nimlib.sdk.msg.attachment.MsgAttachment;
import com.netease.yunxin.kit.chatkit.ui.ChatMessageType;
import com.netease.yunxin.kit.corekit.im.custom.CustomAttachment;
import com.netease.yunxin.kit.corekit.im.model.AttachmentContent;

import org.json.JSONObject;

import java.util.HashMap;

/** 富文本消息自定义类型，102 { "type"：102, "data":{ "title":"我是标题XXX", "body":"我是内容XXX" } } */
public class MingPianAttachment extends CustomAttachment {

  public String avatar;
  public String memberCode;
  public String name;

  public MingPianAttachment() {
    super(ChatMessageType.MingPian_ATTACHMENT);
  }

  @Nullable
  @Override
  public String toJson(boolean send) {
    HashMap map = new HashMap<>();
    map.put("type",10086);
    HashMap tempMap = new HashMap<>();

    tempMap.put("avatar",avatar);
    tempMap.put("memberCode",memberCode);
    tempMap.put("name",name);
    map.put("data",new Gson().toJson(tempMap));
    return new Gson().toJson(map);
  }

  @Override
  protected void parseData(@Nullable JSONObject data) {

//    if (data == null) {
//      return;
//    }
//    try {
//      title = data.optString(KEY_RICH_TEXT_TITLE, "");
//      body = data.optString(KEY_RICH_TEXT_BODY, "");
//    } catch (Exception e) {
//      e.printStackTrace();
//    }
  }

  @Nullable
  @Override
  protected JSONObject packData() {

    JSONObject jsonObject = new JSONObject();
    try {
      jsonObject.put("avatar", avatar);
      jsonObject.put("memberCode", memberCode);
      jsonObject.put("name", name);

    } catch (Exception e) {
      e.printStackTrace();
    }
    return jsonObject;
  }

  @Nullable
  @Override
  public String getContent() {
    return "[名片]";
  }
}
