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
import androidx.constraintlayout.widget.ConstraintLayout;

import com.google.gson.Gson;
import com.netease.yunxin.kit.chatkit.ui.R;
import com.netease.yunxin.kit.chatkit.ui.common.MessageHelper;
import com.netease.yunxin.kit.chatkit.ui.custom.RedPacketAttachment;
import com.netease.yunxin.kit.chatkit.ui.custom.RichTextAttachment;
import com.netease.yunxin.kit.chatkit.ui.databinding.ChatBaseMessageViewHolderBinding;
import com.netease.yunxin.kit.chatkit.ui.databinding.FunChatMessageRedPacketViewHolderBinding;
import com.netease.yunxin.kit.chatkit.ui.databinding.FunChatMessageRichTextViewHolderBinding;
import com.netease.yunxin.kit.chatkit.ui.model.ChatMessageBean;
import com.yaoxin.appbase.model.CustomMsgBean;
import com.yaoxin.appbase.utils.DataUtil;
import com.yaoxin.appbase.utils.NumberUtil;
import com.yaoxin.appbase.utils.TimeUtil;

import java.util.Map;

public class ChatRedPacketMessageViewHolder extends FunChatBaseMessageViewHolder {

    protected FunChatMessageRedPacketViewHolderBinding viewBinding;

    public ChatRedPacketMessageViewHolder(@NonNull ChatBaseMessageViewHolderBinding parent, int viewType) {
        super(parent, viewType);
    }

    @Override
    protected void addViewToMessageContainer() {
        viewBinding = FunChatMessageRedPacketViewHolderBinding.inflate(LayoutInflater.from(parent.getContext()), getMessageContainer(), true);
    }

    @Override
    public void bindData(ChatMessageBean message, ChatMessageBean lastMessage) {
        super.bindData(message, lastMessage);
        if (message != null && message.getMessageData() != null && !message.getMessageData().getMessage().getAttachStr().isEmpty()) {
            Map<String, Object> localExtension = message.getMessageData().getMessage().getLocalExtension();
            boolean hasDraw = false;
//            if (localExtension != null && DataUtil.getUserid().equals(localExtension.get("userId"))) {
//                hasDraw = true;
//            }
            if (localExtension != null && (localExtension.get("hasDragDown") != null && (int) (localExtension.get("hasDragDown")) == 1)) {
                hasDraw = true;
            }
            CustomMsgBean bean = new Gson().fromJson(message.getMessageData().getMessage().getAttachStr(), CustomMsgBean.class);
            bean.result = new Gson().fromJson(bean.data, CustomMsgBean.class);
            if (hasDraw) {
                viewBinding.funChatMessageRedPacketViewHolderMengceng.setVisibility(View.VISIBLE);
            } else {
                viewBinding.funChatMessageRedPacketViewHolderMengceng.setVisibility(View.GONE);
            }
            viewBinding.funChatMessageRedPacketViewHolderBgIv.setImageResource((bean.type == 21) ? R.drawable.chat_redpacket_purple_bg_no_open : R.drawable.chat_red_packet_cell_bg_no_open);
            if (bean.type == 21) {
                viewBinding.funChatLookRed.setVisibility(View.VISIBLE);
                View root = viewBinding.getRoot();
                ViewGroup.LayoutParams lp = root.getLayoutParams();
                if (lp != null) {
                    lp.height = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 100f, root.getResources().getDisplayMetrics());
                    root.setLayoutParams(lp);
                }
                viewBinding.funChatMessageRedPacketViewHolderGreetingTv.setText(bean.result.toUserName);
                viewBinding.funChatMessageRedPacketViewHolderTypeTv.setText("专属红包" + "¥" + NumberUtil.formartMoney(bean.result.amount));
            } else {
                viewBinding.funChatLookRed.setVisibility(View.GONE);
                View root = viewBinding.getRoot();
                ViewGroup.LayoutParams lp = root.getLayoutParams();
                if (lp != null) {
                    lp.height = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 90f, root.getResources().getDisplayMetrics());
                    root.setLayoutParams(lp);
                }
            }
            if (bean.type == 22) {
                viewBinding.funChatMessageRedPacketViewHolderGreetingTv.setText(bean.result.title);
                viewBinding.funChatMessageRedPacketViewHolderTypeTv.setText("红包" + "¥" + NumberUtil.formartMoney(bean.result.amount));
            } else if (bean.type != 21) {
                viewBinding.funChatMessageRedPacketViewHolderGreetingTv.setText(bean.result.title);
                viewBinding.funChatMessageRedPacketViewHolderTypeTv.setText("拼手气" + "¥" + NumberUtil.formartMoney(bean.result.amount));
            }
            viewBinding.funChatMessageRedPacketViewHolderTimeTv.setText(TimeUtil.stampToTime(bean.result.createTime));
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
            // 红包的理想宽度是 216dp
            int idealRedPacketWidth = (int) TypedValue.applyDimension(
                    TypedValue.COMPLEX_UNIT_DIP, 216,
                    parent.getContext().getResources().getDisplayMetrics()
            );

            // 获取 messageContainer 的实际可用宽度
            int availableWidth = baseViewBinding.messageContainer.getWidth();
            // 如果 messageContainer 的宽度为 0，说明还没有布局完成，尝试获取测量宽度
            if (availableWidth <= 0) {
                availableWidth = baseViewBinding.messageContainer.getMeasuredWidth();
            }
            // 如果可用宽度小于红包理想宽度，动态调整红包宽度以适应可用空间
            if (availableWidth > 0 && availableWidth < idealRedPacketWidth) {
                // 调整红包的宽度以适应可用空间，防止被裁剪
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
