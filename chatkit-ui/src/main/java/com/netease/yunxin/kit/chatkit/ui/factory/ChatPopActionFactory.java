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
        if (message.getMessageData() == null || message.getMessageData().getMessage() == null) {
            return actions;
        }
        // 业务自定义消息（红包/名片/转账等）：仅删除，自己发的名片可撤回。
        // 注意：不能用 viewType==0（文本也是 0）判断，否则部分机型文本消息带 attachStr 时会丢掉复制/转发。
        if (message.getMessageData().getMessage().getMsgType() == MsgTypeEnum.custom
                && !isCopyable(message)
                && !isForwardable(message)) {
            actions.add(getDeleteAction(message));
            String attachStr = message.getMessageData().getMessage().getAttachStr();
            if (attachStr != null
                    && attachStr.contains("memberCode")
                    && message.getMessageData().getMessage().getDirect() == MsgDirectionEnum.Out) {
                actions.add(getRecallAction(message));
            }
            return actions;
        }
        if (customPopMenu == null
                || customPopMenu.get() == null
                || customPopMenu.get().showDefaultPopMenu()) {
            if (message.getMessageData().getMessage().getStatus() == MsgStatusEnum.fail
                    || message.getMessageData().getMessage().getStatus() == MsgStatusEnum.sending
                    || message.getMessageData().getMessage().isInBlackList()) {
                if (isCopyable(message)) {
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

    /** 文本 / 富文本可复制；红包等业务自定义消息不可复制 */
    private boolean isCopyable(ChatMessageBean message) {
        if (message.getMessageData() == null || message.getMessageData().getMessage() == null) {
            return false;
        }
        MsgTypeEnum msgType = message.getMessageData().getMessage().getMsgType();
        if (msgType == MsgTypeEnum.text) {
            return true;
        }
        // 自定义消息仅富文本可复制；viewType==0 不能当文本（红包解析失败时也会是 0）
        if (msgType == MsgTypeEnum.custom) {
            return message.getViewType() == ChatMessageType.RICH_TEXT_ATTACHMENT;
        }
        return false;
    }

    /** 文本 / 图片 / 视频 / 文件 / 位置 / 富文本 / 合并转发 可转发；红包/转账/名片等不可转发 */
    private boolean isForwardable(ChatMessageBean message) {
        if (message.getMessageData() == null || message.getMessageData().getMessage() == null) {
            return false;
        }
        MsgTypeEnum msgType = message.getMessageData().getMessage().getMsgType();
        if (msgType == MsgTypeEnum.text
                || msgType == MsgTypeEnum.image
                || msgType == MsgTypeEnum.video
                || msgType == MsgTypeEnum.file
                || msgType == MsgTypeEnum.location) {
            return true;
        }
        if (msgType == MsgTypeEnum.custom) {
            int viewType = message.getViewType();
            return viewType == ChatMessageType.RICH_TEXT_ATTACHMENT
                    || viewType == ChatMessageType.MULTI_FORWARD_ATTACHMENT;
        }
        return false;
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
                    if (actionListener != null && actionListener.get() != null) {
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
                    if (actionListener != null && actionListener.get() != null) {
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
                    if (actionListener != null && actionListener.get() != null) {
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
                    if (actionListener != null && actionListener.get() != null) {
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
                    if (actionListener != null && actionListener.get() != null) {
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
                    if (actionListener != null && actionListener.get() != null) {
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
                    if (actionListener != null && actionListener.get() != null) {
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
