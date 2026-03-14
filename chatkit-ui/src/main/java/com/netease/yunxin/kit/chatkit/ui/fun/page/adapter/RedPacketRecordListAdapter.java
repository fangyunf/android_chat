package com.netease.yunxin.kit.chatkit.ui.fun.page.adapter;

import android.content.Context;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.chad.library.adapter4.BaseQuickAdapter;
import com.chad.library.adapter4.viewholder.QuickViewHolder;
import com.makeramen.roundedimageview.RoundedImageView;
import com.netease.yunxin.kit.chatkit.ui.R;
import com.yaoxin.appbase.model.CustomMsgBean;
import com.yaoxin.appbase.model.GroupInfoBean;
import com.yaoxin.appbase.utils.DataUtil;
import com.yaoxin.appbase.utils.GlideUtil;
import com.yaoxin.appbase.utils.NumberUtil;
import com.yaoxin.appbase.utils.TimeUtil;

public class RedPacketRecordListAdapter extends BaseQuickAdapter<CustomMsgBean, QuickViewHolder> {

    // 0.我收到的  1我发出的
    public int _type;

    @Override
    protected void onBindViewHolder(@NonNull QuickViewHolder quickViewHolder, int i, @Nullable CustomMsgBean bean) {
        TextView nameTv = quickViewHolder.getView(R.id.item_fun_red_packet_result_detail_username_tv);
        RoundedImageView roundedImageView = quickViewHolder.getView(R.id.item_fun_red_packet_result_detail_head_iv);
        LinearLayout bestLl = quickViewHolder.getView(R.id.item_fun_red_packet_result_detail_best_win_ll);
        bestLl.setVisibility(View.GONE);
        if (_type == 0) {
            roundedImageView.setVisibility(View.VISIBLE);
            quickViewHolder.setText(R.id.item_fun_red_packet_result_detail_username_tv, bean.name)
                    .setText(R.id.item_fun_red_packet_result_detail_time_tv, TimeUtil.stampToDate(bean.createTime))
                    .setText(R.id.item_fun_red_packet_result_detail_money_tv, NumberUtil.formartMoney(bean.amount) + "元");
            GlideUtil.yh_loadImageRoundedCorner(getContext(), quickViewHolder.getView(R.id.item_fun_red_packet_result_detail_head_iv), bean.avatar, 20);
            try {
                for (GroupInfoBean tempBean : DataUtil.getFriendInfoList()) {
                    if (tempBean.userId.equals(bean.userId)) {
                        if (!TextUtils.isEmpty(tempBean.remark)) {
                            quickViewHolder.setText(R.id.item_fun_red_packet_result_detail_username_tv, tempBean.remark);
                        }
                        break;
                    }
                }
            } catch (Exception e) {
                quickViewHolder.setText(R.id.item_fun_red_packet_result_detail_username_tv, bean.name);
            }
        } else {

            roundedImageView.setVisibility(View.GONE);
            if (bean.type == 21) {
                nameTv.setText("专属红包");
            }
            if (bean.type == 22) {
                nameTv.setText("个人红包");
            }
            if (bean.type == 23) {
                nameTv.setText("群红包");
            }
            quickViewHolder
                    .setText(R.id.item_fun_red_packet_result_detail_time_tv, TimeUtil.stampToDate(bean.createTime))
                    .setText(R.id.item_fun_red_packet_result_detail_money_tv, NumberUtil.formartMoney(bean.amount) + "元");
        }

    }

    @NonNull
    @Override
    protected QuickViewHolder onCreateViewHolder(@NonNull Context context, @NonNull ViewGroup viewGroup, int i) {
        return new QuickViewHolder(R.layout.item_fun_red_packet_result_detail, viewGroup);
    }
}

