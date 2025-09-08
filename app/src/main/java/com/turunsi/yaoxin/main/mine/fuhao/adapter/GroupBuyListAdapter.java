package com.turunsi.yaoxin.main.mine.fuhao.adapter;

import android.content.Context;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.chad.library.adapter4.BaseQuickAdapter;
import com.chad.library.adapter4.viewholder.QuickViewHolder;
import com.turunsi.yaoxin.R;
import com.turunsi.yaoxin.main.mine.purse.bill.bean.BillDetailBean;
import com.yaoxin.appbase.model.GroupInfoBean;
import com.yaoxin.appbase.utils.NumberUtil;
import com.yaoxin.appbase.utils.TimeUtil;

public class GroupBuyListAdapter extends BaseQuickAdapter<GroupInfoBean, QuickViewHolder> {


    @Override
    protected void onBindViewHolder(@NonNull QuickViewHolder quickViewHolder, int i, @Nullable GroupInfoBean bean) {
        quickViewHolder.setText(R.id.cell_buy_group_feature_money_tv, "￥" + NumberUtil.formartMoney(bean.price + ""))
                .setText(R.id.cell_buy_group_feature_detail_tv, "购买即升级当前群组为" + bean.groupMemberNum + "人群");
        if (bean.groupMemberNum == -1) {

            quickViewHolder
                    .setText(R.id.cell_buy_group_feature_detail_tv, "不限制人数");
        }
        ImageView headIv = quickViewHolder.getView(R.id.cell_buy_group_feature_select_iv);
        headIv.setSelected(bean.isSelected);
    }


    @NonNull
    @Override
    protected QuickViewHolder onCreateViewHolder(@NonNull Context context, @NonNull ViewGroup viewGroup, int i) {
        return new QuickViewHolder(R.layout.cell_buy_group_feature, viewGroup);
    }
}

