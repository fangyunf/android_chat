package com.turunsi.yaoxin.main.conversation.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.chad.library.adapter4.BaseQuickAdapter;
import com.chad.library.adapter4.viewholder.QuickViewHolder;
import com.turunsi.yaoxin.R;
import com.yaoxin.appbase.model.GroupInfoBean;
import com.yaoxin.appbase.utils.GlideUtil;
import com.yaoxin.appbase.utils.NumberUtil;
import com.yaoxin.appbase.utils.TimeUtil;

import java.util.List;

public class XiaoZhuShouListAdapter extends BaseQuickAdapter<GroupInfoBean, QuickViewHolder> {


    @Override
    protected void onBindViewHolder(@NonNull QuickViewHolder quickViewHolder, int position, @Nullable GroupInfoBean infoBean) {
        int state = infoBean.state;
        quickViewHolder.setText(R.id.item_xiaozhushou_list_title_tv, titleText(state))
                .setText(R.id.item_xiaozhushou_list_money_tv, "¥ " + NumberUtil.formartMoney(infoBean.money))
                .setText(R.id.item_xiaozhushou_list_time_tv, TimeUtil.stampToDate(infoBean.createTime))
                .setText(R.id.item_xiaozhushou_list_tyle1_detail_tv, infoBean.payTerm != null ? infoBean.payTerm : "");

        // 按类型设置左侧「金额」和右侧各行标签，与设计一致
        quickViewHolder.setText(R.id.item_xiaozhushou_list_money_title_tv, moneyTitleLabel(state))
                .setText(R.id.item_xiaozhushou_list_tyle1_title_tv, tyle1Label(state))
                .setText(R.id.item_xiaozhushou_list_tyle3_title_tv, timeLabel(state));

        // 仅「红包退回」(state=106) 显示原因行，其他情况隐藏
        View reasonLayout = quickViewHolder.getView(R.id.item_xiaozhushou_list_reason_ll);
        if (state == 106) {
            reasonLayout.setVisibility(View.VISIBLE);
            quickViewHolder.setText(R.id.item_xiaozhushou_list_tyle2_title_tv, "退回原因")
                    .setText(R.id.item_xiaozhushou_list_tyle2_detail_tv, infoBean.payMsg != null ? infoBean.payMsg : "");
        } else {
            reasonLayout.setVisibility(View.GONE);
        }
    }

    private String moneyTitleLabel(int state) {
        if (state == 102) return "充值金额";
        if (state == 103 || state == 104 || state == 105) return "提现金额";
        if (state == 106) return "退回金额";
        return "金额";
    }

    private String tyle1Label(int state) {
        if (state == 102) return "到账方式";
        if (state == 103 || state == 104 || state == 105) return "到账方式";
        if (state == 106) return "退回方式";
        return "收款方式";
    }

    private String timeLabel(int state) {
        if (state == 102) return "充值时间";
        if (state == 103 || state == 104 || state == 105) return "提现时间";
        if (state == 106) return "退回时间";
        return "申请时间";
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

