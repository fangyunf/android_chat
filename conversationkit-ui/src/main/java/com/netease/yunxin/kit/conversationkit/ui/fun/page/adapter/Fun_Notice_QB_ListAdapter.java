package com.netease.yunxin.kit.conversationkit.ui.fun.page.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.chad.library.adapter4.BaseQuickAdapter;
import com.chad.library.adapter4.viewholder.QuickViewHolder;
import com.netease.yunxin.kit.conversationkit.ui.R;
import com.yaoxin.appbase.model.GroupInfoBean;
import com.yaoxin.appbase.utils.GlideUtil;
import com.yaoxin.appbase.utils.NumberUtil;
import com.yaoxin.appbase.utils.TimeUtil;

import java.util.List;

public class Fun_Notice_QB_ListAdapter extends BaseQuickAdapter<GroupInfoBean, QuickViewHolder> {

    public int type = 0;

    @Override
    protected void onBindViewHolder(@NonNull QuickViewHolder quickViewHolder, int position, @Nullable GroupInfoBean infoBean) {
        TextView tv1 = quickViewHolder.getView(R.id.cell_notice_qb_title_tv);
        TextView tv2 = quickViewHolder.getView(R.id.cell_notice_qb_time_tv);
        TextView tv3 = quickViewHolder.getView(R.id.cell_notice_qb_detail_tv);

        tv1.setText(type == 0 ? "用户充值" : "用户提现");
        tv2.setText(TimeUtil.stampToDate(infoBean.createTime));
        String contentStr = "";
        if (type == 0) {
            if (infoBean.state == 1) {
                contentStr = "成功充值"+ NumberUtil.formartMoney(infoBean.amount) +"元到您的余额";
            } else if (infoBean.state == 0) {
                contentStr = "充值中, 充值"+ NumberUtil.formartMoney(infoBean.amount) +"元";
            } else {
                contentStr = "充值"+ NumberUtil.formartMoney(infoBean.amount) +"元失败";
            }
        } else {
            if (infoBean.state == 0) {
                contentStr = "发起申请提现操作:提现"+ NumberUtil.formartMoney(infoBean.amount) +"元";
            } else if (infoBean.state == 1) {
                contentStr = "您提现"+ NumberUtil.formartMoney(infoBean.remitAmount) +"元审核通过,注意查看银行短信通知";
            } else {
                contentStr = "您提现"+ NumberUtil.formartMoney(infoBean.amount) +"元失败";
            }
        }
        tv3.setText(contentStr);
    }

    @NonNull
    @Override
    protected QuickViewHolder onCreateViewHolder(@NonNull Context context, @NonNull ViewGroup viewGroup, int i) {
        return new QuickViewHolder(R.layout.cell_notice_qb, viewGroup);
    }
}

