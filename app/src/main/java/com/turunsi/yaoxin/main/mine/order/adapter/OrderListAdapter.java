package com.turunsi.yaoxin.main.mine.order.adapter;

import android.content.Context;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.chad.library.adapter4.BaseQuickAdapter;
import com.chad.library.adapter4.viewholder.DataBindingHolder;
import com.chad.library.adapter4.viewholder.QuickViewHolder;
import com.netease.yunxin.kit.common.ui.viewholder.BaseViewHolder;
import com.turunsi.yaoxin.R;
import com.turunsi.yaoxin.databinding.ItemOrderListCellBinding;
import com.turunsi.yaoxin.main.mine.order.bean.OrderListBean;

public class OrderListAdapter extends BaseQuickAdapter<OrderListBean, QuickViewHolder> {

    @Override
    protected void onBindViewHolder(@NonNull QuickViewHolder quickViewHolder, int i, @Nullable OrderListBean orderListBean) {

    }

    @NonNull
    @Override
    protected QuickViewHolder onCreateViewHolder(@NonNull Context context, @NonNull ViewGroup viewGroup, int i) {
        return new QuickViewHolder(R.layout.item_order_list_cell,viewGroup);
    }
}

