// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.chatkit.ui.fun.view.message.viewholder;

import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;

import com.google.gson.Gson;
import com.netease.yunxin.kit.chatkit.ui.R;
import com.netease.yunxin.kit.chatkit.ui.databinding.ChatBaseMessageViewHolderBinding;
import com.netease.yunxin.kit.chatkit.ui.databinding.FunChatMessageRedPacketViewHolderBinding;
import com.netease.yunxin.kit.chatkit.ui.model.ChatMessageBean;
import com.yaoxin.appbase.model.CustomMsgBean;
import com.yaoxin.appbase.utils.NumberUtil;
import com.yaoxin.appbase.utils.TimeUtil;

import java.util.Map;

public class ChatRedPacketMessageViewHolder extends FunChatBaseMessageViewHolder {

    protected FunChatMessageRedPacketViewHolderBinding viewBinding;

    public ChatRedPacketMessageViewHolder(
            @NonNull ChatBaseMessageViewHolderBinding parent, int viewType) {
        super(parent, viewType);
    }

    @Override
    protected void addViewToMessageContainer() {
        viewBinding =
                FunChatMessageRedPacketViewHolderBinding.inflate(
                        LayoutInflater.from(parent.getContext()), getMessageContainer(), true);
    }

    @Override
    public void bindData(ChatMessageBean message, ChatMessageBean lastMessage) {
        super.bindData(message, lastMessage);
        if (message != null
                && message.getMessageData() != null
                && !message.getMessageData().getMessage().getAttachStr().isEmpty()) {
            Map<String, Object> localExtension =
                    message.getMessageData().getMessage().getLocalExtension();
            boolean hasDraw = false;
            if (localExtension != null
                    && localExtension.get("hasDragDown") != null
                    && (int) localExtension.get("hasDragDown") == 1) {
                hasDraw = true;
            }

            CustomMsgBean bean =
                    new Gson()
                            .fromJson(
                                    message.getMessageData().getMessage().getAttachStr(),
                                    CustomMsgBean.class);
            bean.result = new Gson().fromJson(bean.data, CustomMsgBean.class);
            if (hasDraw) {
                viewBinding.funChatMessageRedPacketViewHolderMengceng.setVisibility(View.VISIBLE);
                viewBinding.funChatMessageRedPacketViewHolderBgIv.setImageAlpha(128);
            } else {
                viewBinding.funChatMessageRedPacketViewHolderBgIv.setImageAlpha(255);
                viewBinding.funChatMessageRedPacketViewHolderMengceng.setVisibility(View.GONE);
            }
            viewBinding.funChatMessageRedPacketViewHolderBgIv.setImageResource(
                    (bean.type == 21)
                            ? R.drawable.chat_redpacket_purple_bg_no_open
                            : R.drawable.chat_red_packet_cell_bg_no_open);
            if (bean.type == 21) {
                viewBinding.funChatMessageRedPacketViewHolderGreetingTv.setText(bean.result.toUserName);
                viewBinding.funChatMessageRedPacketViewHolderTypeTv.setText("专属红包");
            } else if (bean.type == 22) {
                viewBinding.funChatMessageRedPacketViewHolderGreetingTv.setText(bean.result.title);
                viewBinding.funChatMessageRedPacketViewHolderTypeTv.setText("红包");
            } else {
                viewBinding.funChatMessageRedPacketViewHolderGreetingTv.setText(bean.result.title);
                viewBinding.funChatMessageRedPacketViewHolderTypeTv.setText("拼手气");
            }
            viewBinding.funChatMessageRedPacketViewHolderTimeTv.setText(TimeUtil.stampToDate(bean.result.createTime));
            viewBinding.funChatMessageRedPacketViewHolderMoneyTv.setText(String.format("¥%s", NumberUtil.formartMoney(bean.result.amount)));
        }
    }

    @Override
    protected void onLayoutConfig(ChatMessageBean messageBean) {
        super.onLayoutConfig(messageBean);
        if (viewBinding != null) {
            View rootView = viewBinding.getRoot();
            int idealRedPacketWidth =
                    (int)
                            TypedValue.applyDimension(
                                    TypedValue.COMPLEX_UNIT_DIP,
                                    216,
                                    parent.getContext().getResources().getDisplayMetrics());

            int availableWidth = baseViewBinding.messageContainer.getWidth();
            if (availableWidth <= 0) {
                availableWidth = baseViewBinding.messageContainer.getMeasuredWidth();
            }
            if (availableWidth > 0 && availableWidth < idealRedPacketWidth) {
                ViewGroup.LayoutParams redPacketLayoutParams = rootView.getLayoutParams();
                if (redPacketLayoutParams != null) {
                    redPacketLayoutParams.width = availableWidth;
                    rootView.setLayoutParams(redPacketLayoutParams);
                }
            }
        }
    }

    @Override
    protected void onMessageBackgroundConfig(ChatMessageBean messageBean) {
        super.onMessageBackgroundConfig(messageBean);
        viewBinding.getRoot().setBackgroundResource(R.color.title_transfer);
    }
}
