package com.netease.yunxin.kit.conversationkit.ui.fun.page.adapter;

import android.content.Context;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.chad.library.adapter4.BaseQuickAdapter;
import com.chad.library.adapter4.viewholder.QuickViewHolder;
import com.netease.yunxin.kit.conversationkit.ui.R;
import com.yaoxin.appbase.model.GroupInfoBean;
import com.yaoxin.appbase.utils.NumberUtil;
import com.yaoxin.appbase.utils.TimeUtil;

public class XiaoZhuShouListAdapter extends BaseQuickAdapter<GroupInfoBean, QuickViewHolder> {


    @Override
    protected void onBindViewHolder(@NonNull QuickViewHolder quickViewHolder, int position, @Nullable GroupInfoBean infoBean) {
        quickViewHolder.setText(R.id.item_xiaozhushou_list_title_tv,titleText(infoBean.state))
                .setText(R.id.item_xiaozhushou_list_money_tv, "¥ " + NumberUtil.formartMoney(infoBean.money))
                .setText(R.id.item_xiaozhushou_list_time_tv, TimeUtil.stampToDate(infoBean.createTime))
                .setText(R.id.item_xiaozhushou_list_tyle1_detail_tv, infoBean.payTerm)
                .setText(R.id.item_xiaozhushou_list_tyle2_detail_tv, infoBean.payMsg);
    }

    String titleText(int type) {
        String result = "";
        switch (type) {
            case 101:
                result = "小助手消息";
                break;
            case 102:
                result = "充值成功";
                break;
            case 103:
                result = "提现审核";
                break;
            case 104:
                result = "提现成功";
                break;
            case 105:
                result = "提现失败";
                break;
            case 106:
                result = "红包退回";
                break;
            case 107:
                result = "系统消息";
                break;
        }
        return result;
    }
    @NonNull
    @Override
    protected QuickViewHolder onCreateViewHolder(@NonNull Context context, @NonNull ViewGroup viewGroup, int i) {
        return new QuickViewHolder(R.layout.item_xiaozhushou_list, viewGroup);
    }
}

