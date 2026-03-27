// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.chatkit.ui.fun.view.message.viewholder;

import android.text.TextUtils;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;

import com.google.gson.Gson;
import com.netease.yunxin.kit.chatkit.ui.R;
import com.netease.yunxin.kit.chatkit.ui.common.MessageHelper;
import com.netease.yunxin.kit.chatkit.ui.custom.RedPacketAttachment;
import com.netease.yunxin.kit.chatkit.ui.custom.RichTextAttachment;
import com.netease.yunxin.kit.chatkit.ui.databinding.ChatBaseMessageViewHolderBinding;
import com.netease.yunxin.kit.chatkit.ui.databinding.FunChatMessageRedPacketViewHolderBinding;
import com.netease.yunxin.kit.chatkit.ui.databinding.FunChatMessageRichTextViewHolderBinding;
import com.netease.yunxin.kit.chatkit.ui.model.ChatMessageBean;
import com.netease.yunxin.kit.common.utils.SizeUtils;
import com.yaoxin.appbase.model.CustomMsgBean;
import com.yaoxin.appbase.utils.DataUtil;
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
            Map<String, Object> localExtension = message.getMessageData().getMessage().getLocalExtension();
            boolean hasDraw = false;
            if (localExtension != null && DataUtil.getUserid().equals(localExtension.get("userId"))) {
                hasDraw = true;
            }

            CustomMsgBean bean = new Gson().fromJson(message.getMessageData().getMessage().getAttachStr(), CustomMsgBean.class);
            bean.result = new Gson().fromJson(bean.data, CustomMsgBean.class);
            if (hasDraw) {
                viewBinding.funChatMessageRedPacketViewHolderBgIv.setImageResource((bean.type == 21) ? R.drawable.chat_redpacket_purple_bg_is_open : R.drawable.chat_red_packet_cell_bg_is_open);
                viewBinding.funChatMessageRedPacketViewHolderBgIv.setImageAlpha(128);
                /*viewBinding.funChatMessageRedPacketViewHolderBgIv.setImageAlpha(80);*/
            } else {
//                viewBinding.funChatMessageRedPacketViewHolderBgIv.setImageAlpha(100);
                viewBinding.funChatMessageRedPacketViewHolderBgIv.setImageAlpha(255);
                viewBinding.funChatMessageRedPacketViewHolderBgIv.setImageResource((bean.type == 21) ? R.drawable.chat_redpacket_purple_bg_no_open : R.drawable.chat_red_packet_cell_bg_no_open);
            }
            if (bean.type == 21) {
                viewBinding.funChatMessageRedPacketViewHolderGreetingTv.setText(bean.result.toUserName);
                viewBinding.funChatMessageRedPacketViewHolderTypeTv.setText("专属红包" + "¥" + NumberUtil.formartMoney(bean.result.amount));
            } else if (bean.type == 22) {
                viewBinding.funChatMessageRedPacketViewHolderGreetingTv.setText(bean.result.title);
                viewBinding.funChatMessageRedPacketViewHolderTypeTv.setText("红包" + "¥" + NumberUtil.formartMoney(bean.result.amount));
            } else {
                viewBinding.funChatMessageRedPacketViewHolderGreetingTv.setText(bean.result.title);
                viewBinding.funChatMessageRedPacketViewHolderTypeTv.setText("拼手气红包" + "¥" + NumberUtil.formartMoney(bean.result.amount));
            }
            viewBinding.funChatMessageRedPacketViewHolderTimeTv.setText(TimeUtil.stampToDate(bean.result.createTime));
//      if (bean.type == 21) {
            viewBinding.funChatMessageRedPacketViewHolderMoneyTv.setText("¥" + NumberUtil.formartMoney(bean.result.amount));
//      } else {
//        viewBinding.funChatMessageRedPacketViewHolderMoneyTv.setText("");
//      }
        }
    }

    @Override
    protected void onLayoutConfig(ChatMessageBean messageBean) {
        super.onLayoutConfig(messageBean);
        // 为红包消息设置宽度限制，防止在小屏幕手机上被裁剪
        if (viewBinding != null) {
            View rootView = viewBinding.getRoot();
            // 红包的理想宽度是 230dp
            int idealRedPacketWidth = (int) TypedValue.applyDimension(
                    TypedValue.COMPLEX_UNIT_DIP, 230,
                    parent.getContext().getResources().getDisplayMetrics()
            );

            // 获取 messageContainer 的实际可用宽度
            int availableWidth = baseViewBinding.messageContainer.getWidth();
            // 如果 messageContainer 的宽度为 0，说明还没有布局完成，尝试获取测量宽度
            if (availableWidth <= 0) {
                availableWidth = baseViewBinding.messageContainer.getMeasuredWidth();
            }
            // 每次都重设宽度，避免 RecyclerView 复用导致红包宽度残留（出现一大一小）
            int targetWidth = idealRedPacketWidth;
            if (availableWidth > 0) {
                targetWidth = Math.min(availableWidth, idealRedPacketWidth);
            }
            ViewGroup.LayoutParams redPacketLayoutParams = rootView.getLayoutParams();
            if (redPacketLayoutParams != null && redPacketLayoutParams.width != targetWidth) {
                redPacketLayoutParams.width = targetWidth;
                rootView.setLayoutParams(redPacketLayoutParams);
            }
        }
    }

    @Override
    protected void onMessageBackgroundConfig(ChatMessageBean messageBean) {
        super.onMessageBackgroundConfig(messageBean);
        viewBinding.getRoot().setBackgroundResource(R.color.title_transfer);
    }
}
