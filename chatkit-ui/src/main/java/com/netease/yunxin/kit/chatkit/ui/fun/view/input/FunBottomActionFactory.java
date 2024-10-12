// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.chatkit.ui.fun.view.input;

import com.netease.nimlib.sdk.msg.constant.SessionTypeEnum;
import com.netease.yunxin.kit.chatkit.ui.ChatKitClient;
import com.netease.yunxin.kit.chatkit.ui.R;
import com.netease.yunxin.kit.chatkit.ui.view.input.ActionConstants;
import com.netease.yunxin.kit.common.ui.action.ActionItem;
import java.util.ArrayList;
import java.util.List;

public class FunBottomActionFactory {

  public static List<ActionItem> assembleInputMoreActions(SessionTypeEnum sessionType) {
    ArrayList<ActionItem> actions = new ArrayList<>();
    actions.add(
        new ActionItem(
            ActionConstants.ACTION_TYPE_ALBUM,
            R.drawable.chat_fragment_toolbar_photo,
            R.string.chat_input_more_album_title));
    actions.add(
        new ActionItem(
            ActionConstants.ACTION_TYPE_CAMERA,
            R.drawable.chat_fragment_toolbar_camera,
            R.string.chat_message_more_shoot));
//    actions.add(
//        new ActionItem(
//            ActionConstants.ACTION_TYPE_LOCATION,
//            R.drawable.ic_location,
//            R.string.chat_message_location));
//    actions.add(
//        new ActionItem(
//            ActionConstants.ACTION_TYPE_FILE, R.drawable.chat_fragment_toolbar_collection, R.string.chat_message_file));

    actions.add(
            new ActionItem(
                    ActionConstants.ACTION_TYPE_MING_PIAN, R.drawable.chat_fragment_toolbar_person_card, R.string.chat_message_ming_pian));

    actions.add(
        new ActionItem(
            ActionConstants.ACTION_TYPE_SHOPPING_COUPON, R.drawable.chat_fragment_toolbar_redpacket, R.string.chat_message_shopping_coupon));

    actions.add(
            new ActionItem(
                    ActionConstants.ACTION_TYPE_SHOU_CANG, R.drawable.chat_fragment_toolbar_shoucang, R.string.chat_message_shou_cang));

    if (sessionType == SessionTypeEnum.P2P) {
      actions.add(
              new ActionItem(
                      ActionConstants.ACTION_TYPE_ZHUAN_ZHANG, R.drawable.chat_fragment_toolbar_zhuanzhang, R.string.chat_message_zhuan_zhang));

    }
//    if (sessionType == SessionTypeEnum.P2P) {
//      actions.add(
//          new ActionItem(
//              ActionConstants.ACTION_TYPE_VIDEO_CALL,
//              R.drawable.ic_video_call,
//              R.string.chat_message_video_call));
//    }
    if (ChatKitClient.getChatUIConfig() != null
        && ChatKitClient.getChatUIConfig().chatInputMenu != null) {
      return ChatKitClient.getChatUIConfig().chatInputMenu.customizeInputMore(actions);
    }
    return actions;
  }

  public static ArrayList<ActionItem> assembleTakeShootActions() {
    ArrayList<ActionItem> actions = new ArrayList<>();
    actions.add(
        new ActionItem(ActionConstants.ACTION_TYPE_TAKE_PHOTO, 0, R.string.chat_message_take_photo)
            .setTitleColorResId(R.color.color_333333));
    actions.add(
        new ActionItem(ActionConstants.ACTION_TYPE_TAKE_VIDEO, 0, R.string.chat_message_take_video)
            .setTitleColorResId(R.color.color_333333));
    return actions;
  }

  public static ArrayList<ActionItem> assembleVideoCallActions() {
    ArrayList<ActionItem> actions = new ArrayList<>();
    actions.add(
        new ActionItem(
                ActionConstants.ACTION_TYPE_VIDEO_CALL_ACTION,
                0,
                R.string.chat_message_video_call_action)
            .setTitleColorResId(R.color.color_333333));
    actions.add(
        new ActionItem(
                ActionConstants.ACTION_TYPE_AUDIO_CALL_ACTION,
                0,
                R.string.chat_message_audio_call_action)
            .setTitleColorResId(R.color.color_333333));
    return actions;
  }
}
