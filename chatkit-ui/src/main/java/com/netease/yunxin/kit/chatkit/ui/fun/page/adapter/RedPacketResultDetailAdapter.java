package com.netease.yunxin.kit.chatkit.ui.fun.page.adapter;

import android.content.Context;
import android.graphics.Color;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.chad.library.adapter4.BaseQuickAdapter;
import com.chad.library.adapter4.viewholder.QuickViewHolder;
import com.netease.yunxin.kit.chatkit.ui.R;
import com.yaoxin.appbase.model.CustomMsgBean;
import com.yaoxin.appbase.model.GroupInfoBean;
import com.yaoxin.appbase.utils.DataUtil;
import com.yaoxin.appbase.utils.GlideUtil;
import com.yaoxin.appbase.utils.NumberUtil;
import com.yaoxin.appbase.utils.ResourceHelper;
import com.yaoxin.appbase.utils.TimeUtil;

public class RedPacketResultDetailAdapter extends BaseQuickAdapter<CustomMsgBean, QuickViewHolder> {

    @Override
    protected void onBindViewHolder(@NonNull QuickViewHolder quickViewHolder, int i, @Nullable CustomMsgBean bean) {
        String remark = "";
        try {
            for (GroupInfoBean groupInfoBean : DataUtil.getFriendInfoList()) {
                if (groupInfoBean.userId.equals(bean.userId)) {
                    if (groupInfoBean.remark != null && !groupInfoBean.remark.isEmpty()) {
                        remark = "(" + groupInfoBean.remark + ")";
                    }
                    break;
                }
            }
        } catch (Exception ignored) {
        }
        quickViewHolder
                .setText(R.id.item_fun_red_packet_result_detail_username_tv, bean.name + remark)
                .setText(R.id.item_fun_red_packet_result_detail_time_tv, TimeUtil.stampToDate(bean.reciveTime))
                .setText(R.id.item_fun_red_packet_result_detail_money_tv, NumberUtil.formartMoney(bean.amount) + "元");
        GlideUtil.yh_loadImageRoundedCorner(
                getContext(),
                quickViewHolder.getView(R.id.item_fun_red_packet_result_detail_head_iv),
                bean.avatar,
                20);
        ImageView bestLl = quickViewHolder.getView(R.id.item_fun_red_packet_result_detail_best_win_ll);
        bestLl.setVisibility(bean.isBest ? View.VISIBLE : View.GONE);

        ImageView ivGrade = quickViewHolder.getView(R.id.ivGrade);
        ImageView ivGradeBg = quickViewHolder.getView(R.id.ivGradeBg);
        if (!TextUtils.isEmpty(bean.level) && Integer.parseInt(bean.level) > 0) {
            ivGrade.setVisibility(View.GONE);
            ivGradeBg.setVisibility(View.VISIBLE);
            ivGrade.setImageDrawable(ResourceHelper.getGradeDrawable(getContext(), Integer.parseInt(bean.level)));
            quickViewHolder.setTextColor(
                    R.id.item_fun_red_packet_result_detail_username_tv,
                    ResourceHelper.getGradeColor(getContext(), Integer.parseInt(bean.level)));
            ivGradeBg.setImageDrawable(ResourceHelper.getGradeBackground(getContext(), Integer.parseInt(bean.level)));
        } else {
            ivGrade.setVisibility(View.GONE);
            ivGradeBg.setVisibility(View.GONE);
            quickViewHolder.setTextColor(R.id.item_fun_red_packet_result_detail_username_tv, Color.parseColor("#333333"));
        }
    }

    @NonNull
    @Override
    protected QuickViewHolder onCreateViewHolder(@NonNull Context context, @NonNull ViewGroup viewGroup, int i) {
        return new QuickViewHolder(R.layout.item_fun_red_packet_result_detail, viewGroup);
    }
}
