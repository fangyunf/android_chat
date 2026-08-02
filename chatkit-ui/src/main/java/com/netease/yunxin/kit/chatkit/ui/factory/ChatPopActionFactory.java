// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.chatkit.ui.factory;

import android.text.TextUtils;
import com.netease.nimlib.sdk.msg.constant.MsgDirectionEnum;
import com.netease.nimlib.sdk.msg.constant.MsgStatusEnum;
import com.netease.nimlib.sdk.msg.constant.MsgTypeEnum;
import com.netease.yunxin.kit.chatkit.ui.ChatMessageType;
import com.netease.yunxin.kit.chatkit.ui.R;
import com.netease.yunxin.kit.chatkit.ui.model.ChatMessageBean;
import com.netease.yunxin.kit.chatkit.ui.view.input.ActionConstants;
import com.netease.yunxin.kit.chatkit.ui.view.popmenu.ChatPopMenuAction;
import com.netease.yunxin.kit.chatkit.ui.view.popmenu.IChatPopMenu;
import com.netease.yunxin.kit.chatkit.ui.view.popmenu.IChatPopMenuClickListener;
import com.netease.yunxin.kit.common.ui.utils.ToastX;
import com.netease.yunxin.kit.common.utils.NetworkUtils;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

/** 聊天界面长按弹窗工厂类，根据长按的消息返回对应的弹窗中的内容 */
public class ChatPopActionFactory {

    private static volatile ChatPopActionFactory instance;

    private WeakReference<IChatPopMenuClickListener> actionListener;

    private WeakReference<IChatPopMenu> customPopMenu;

    private ChatPopActionFactory() {}

    public static ChatPopActionFactory getInstance() {
        if (instance == null) {
            synchronized (ChatPopActionFactory.class) {
                if (instance == null) {
                    instance = new ChatPopActionFactory();
                }
            }
        }
        return instance;
    }

    public void setActionListener(IChatPopMenuClickListener actionListener) {
        this.actionListener = new WeakReference<>(actionListener);
    }

    public void setChatPopMenu(IChatPopMenu popMenu) {
        this.customPopMenu = new WeakReference<>(popMenu);
    }

    /**
     * 获取长按弹窗中的内容
     *
     * @param message
     * @return
     */
    public List<ChatPopMenuAction> getNormalActions(ChatMessageBean message) {
        List<ChatPopMenuAction> actions = new ArrayList<>();
        if (message.getMessageData() == null) {
            return actions;
        }
        int viewType = message.getViewType();

        if (viewType == 0 &&message.getMessageData().getMessage().getAttachStr() != null && !message.getMessageData().getMessage().getAttachStr().isEmpty()) {

            actions.add(getDeleteAction(message));
            if (message.getMessageData().getMessage().getAttachStr().contains("memberCode")) {
                if (message.getMessageData().getMessage().getDirect() == MsgDirectionEnum.Out) {
                    actions.add(getRecallAction(message));
                }
            }
            return actions;
        }
        if (customPopMenu == null
                || customPopMenu.get() == null
                || customPopMenu.get().showDefaultPopMenu()) {
            if (message.getMessageData().getMessage().getStatus() == MsgStatusEnum.fail
                    || message.getMessageData().getMessage().getStatus() == MsgStatusEnum.sending
                    || message.getMessageData().getMessage().isInBlackList()) {
                if (message.getViewType() == MsgTypeEnum.text.getValue()) {
                    actions.add(getCopyAction(message));
                }
                actions.add(getDeleteAction(message));
//        actions.add(getMultiSelectAction(message));
                return actions;
            }

            if (message.getViewType() == MsgTypeEnum.nrtc_netcall.getValue()) {
                // call
                actions.add(getDeleteAction(message));
//        actions.add(getMultiSelectAction(message));
                return actions;
            }
            if (message.getViewType() == MsgTypeEnum.audio.getValue()) {
                actions.add(getPlayAudioEarpieceAction(message));
                actions.add(getPlayAudioSpeakerAction(message));
                actions.add(getDeleteAction(message));
                if (message.getMessageData().getMessage().getDirect() == MsgDirectionEnum.Out) {
                    actions.add(getRecallAction(message));
                }
                return actions;
            }
            // 基础消息类型都在MsgTypeEnum中定义,自定义消息类型都是MsgTypeEnum.custom，
            // 自定义消息，根据自定义消息的Type区分IMUIKIt内置从101开始，客户定义从1000开始
            if (isCopyable(message)) {
                actions.add(getCopyAction(message));
            }
            if (isForwardable(message)) {
                actions.add(getTransmitAction(message));
            }
//      actions.add(getReplyAction(message));
//      actions.add(getPinAction(message));
            actions.add(getDeleteAction(message));
//            actions.add(getMultiSelectAction(message));
//            actions.add(getCollectionAction(message));
            if (message.getMessageData().getMessage().getDirect() == MsgDirectionEnum.Out) {
                actions.add(getRecallAction(message));
            }
        }
        if (customPopMenu != null && customPopMenu.get() != null) {
            return customPopMenu.get().customizePopMenu(actions, message);
        }
        return actions;
    }

    /** 文本 / 富文本可复制 */
    private boolean isCopyable(ChatMessageBean message) {
        int viewType = message.getViewType();
        return viewType == MsgTypeEnum.text.getValue()
                || viewType == ChatMessageType.RICH_TEXT_ATTACHMENT;
    }

    /** 文本 / 图片 / 视频 / 文件 / 位置 / 富文本 / 合并转发 可转发；红包等业务自定义消息不可转发 */
    private boolean isForwardable(ChatMessageBean message) {
        int viewType = message.getViewType();
        return viewType == MsgTypeEnum.text.getValue()
                || viewType == MsgTypeEnum.image.getValue()
                || viewType == MsgTypeEnum.video.getValue()
                || viewType == MsgTypeEnum.file.getValue()
                || viewType == MsgTypeEnum.location.getValue()
                || viewType == ChatMessageType.RICH_TEXT_ATTACHMENT
                || viewType == ChatMessageType.MULTI_FORWARD_ATTACHMENT;
    }

    private ChatPopMenuAction getReplyAction(ChatMessageBean message) {
        return new ChatPopMenuAction(
                ActionConstants.POP_ACTION_REPLY,
                R.string.chat_message_action_reply,
                R.drawable.ic_message_reply,
                (view, messageInfo) -> {
                    if (actionListener != null) {
                        actionListener.get().onReply(messageInfo);
                    }
                });
    }

    private ChatPopMenuAction getCopyAction(ChatMessageBean message) {
        return new ChatPopMenuAction(
                ActionConstants.POP_ACTION_COPY,
                R.string.chat_message_action_copy,
                R.drawable.ic_message_copy,
                (view, messageInfo) -> {
                    if (actionListener != null) {
                        actionListener.get().onCopy(messageInfo);
                    }
                });
    }

    private ChatPopMenuAction getRecallAction(ChatMessageBean message) {
        return new ChatPopMenuAction(
                ActionConstants.POP_ACTION_RECALL,
                R.string.chat_message_action_recall,
                R.drawable.ic_message_recall,
                (view, messageInfo) -> {
                    if (!NetworkUtils.isConnected()) {
                        ToastX.showShortToast(R.string.chat_network_error_tip);
                        return;
                    }
                    if (actionListener != null) {
                        actionListener.get().onRecall(messageInfo);
                    }
                });
    }

    private ChatPopMenuAction getPinAction(ChatMessageBean message) {
        return new ChatPopMenuAction(
                ActionConstants.POP_ACTION_PIN,
                !TextUtils.isEmpty(message.getPinAccid())
                        ? R.string.chat_message_action_pin_cancel
                        : R.string.chat_message_action_pin,
                R.drawable.ic_message_sign,
                (view, messageInfo) -> {
                    if (!NetworkUtils.isConnected()) {
                        ToastX.showShortToast(R.string.chat_network_error_tip);
                        return;
                    }
                    if (actionListener != null) {
                        actionListener
                                .get()
                                .onSignal(messageInfo, !TextUtils.isEmpty(messageInfo.getPinAccid()));
                    }
                });
    }

    private ChatPopMenuAction getMultiSelectAction(ChatMessageBean message) {
        return new ChatPopMenuAction(
                ActionConstants.POP_ACTION_MULTI_SELECT,
                R.string.chat_message_action_multi_select,
                R.drawable.ic_message_multi_select,
                (view, messageInfo) -> {
                    if (actionListener != null) {
                        actionListener.get().onMultiSelected(messageInfo);
                    }
                });
    }

    private ChatPopMenuAction getCollectionAction(ChatMessageBean message) {
        return new ChatPopMenuAction(
                ActionConstants.POP_ACTION_COLLECTION,
                R.string.chat_message_action_collection,
                R.drawable.ic_message_collection,
                (view, messageInfo) -> {
                    if (actionListener != null) {
                        actionListener.get().onCollection(messageInfo);
                    }
                });
    }

    private ChatPopMenuAction getDeleteAction(ChatMessageBean message) {
        return new ChatPopMenuAction(
                ActionConstants.POP_ACTION_DELETE,
                R.string.chat_message_action_delete,
                R.drawable.ic_message_delete,
                (view, messageInfo) -> {
                    if (!NetworkUtils.isConnected()) {
                        ToastX.showShortToast(R.string.chat_network_error_tip);
                        return;
                    }
                    if (actionListener != null) {
                        actionListener.get().onDelete(message);
                    }
                });
    }

    private ChatPopMenuAction getTransmitAction(ChatMessageBean message) {
        return new ChatPopMenuAction(
                ActionConstants.POP_ACTION_TRANSMIT,
                R.string.chat_message_action_transmit,
                R.drawable.ic_message_transmit,
                (view, messageInfo) -> {
                    if (!NetworkUtils.isConnected()) {
                        ToastX.showShortToast(R.string.chat_network_error_tip);
                        return;
                    }
                    if (actionListener != null) {
                        actionListener.get().onForward(messageInfo);
                    }
                });
    }

    private ChatPopMenuAction getPlayAudioEarpieceAction(ChatMessageBean message) {
        return new ChatPopMenuAction(
                ActionConstants.POP_ACTION_PLAY_AUDIO_EARPIECE,
                R.string.chat_message_action_play_earpiece,
                R.drawable.ic_message_play_earpiece,
                (view, messageInfo) -> {
                    if (actionListener != null && actionListener.get() != null) {
                        actionListener.get().onCustom(view, messageInfo, ActionConstants.POP_ACTION_PLAY_AUDIO_EARPIECE);
                    }
                });
    }

    private ChatPopMenuAction getPlayAudioSpeakerAction(ChatMessageBean message) {
        return new ChatPopMenuAction(
                ActionConstants.POP_ACTION_PLAY_AUDIO_SPEAKER,
                R.string.chat_message_action_play_speaker,
                R.drawable.ic_message_play_speaker,
                (view, messageInfo) -> {
                    if (actionListener != null && actionListener.get() != null) {
                        actionListener.get().onCustom(view, messageInfo, ActionConstants.POP_ACTION_PLAY_AUDIO_SPEAKER);
                    }
                });
    }
}
