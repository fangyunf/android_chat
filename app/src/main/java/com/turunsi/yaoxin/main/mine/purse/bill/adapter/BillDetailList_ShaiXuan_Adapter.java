package com.turunsi.yaoxin.main.mine.purse.bill.adapter;

import android.content.Context;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.chad.library.adapter4.BaseQuickAdapter;
import com.chad.library.adapter4.viewholder.QuickViewHolder;
import com.turunsi.yaoxin.R;
import com.turunsi.yaoxin.main.mine.purse.bill.bean.BillDetailBean;

public class BillDetailList_ShaiXuan_Adapter extends BaseQuickAdapter<BillDetailBean, QuickViewHolder> {


    @Override
    protected void onBindViewHolder(@NonNull QuickViewHolder quickViewHolder, int i, @Nullable BillDetailBean orderListBean) {
        quickViewHolder.setText(R.id.item_purse_bill_detail_shaixuan_list_tv,orderListBean.title);
        quickViewHolder.setBackgroundResource(R.id.item_purse_bill_detail_shaixuan_list_tv,orderListBean.isSelected ? R.drawable.bg_b591f5_rounded_4 : R.drawable.border_cccccc_1_rounded_4)
                .setTextColor(R.id.item_purse_bill_detail_shaixuan_list_tv,getContext().getResources().getColor(orderListBean.isSelected ? R.color.color_white:R.color.black))
        ;
    }

    @NonNull
    @Override
    protected QuickViewHolder onCreateViewHolder(@NonNull Context context, @NonNull ViewGroup viewGroup, int i) {
        return new QuickViewHolder(R.layout.item_purse_bill_detail_shaixuan_list, viewGroup);
    }
}

