package com.turunsi.yaoxin.main.mine.address.adapter;

import android.content.Context;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.chad.library.adapter4.BaseQuickAdapter;
import com.chad.library.adapter4.viewholder.QuickViewHolder;
import com.turunsi.yaoxin.R;
import com.turunsi.yaoxin.main.mine.address.bean.AddressListBean;
import com.turunsi.yaoxin.main.mine.order.bean.OrderListBean;

public class AddressListAdapter extends BaseQuickAdapter<AddressListBean, QuickViewHolder> {

    @Override
    protected void onBindViewHolder(@NonNull QuickViewHolder quickViewHolder, int i, @Nullable AddressListBean orderListBean) {

    }

    @NonNull
    @Override
    protected QuickViewHolder onCreateViewHolder(@NonNull Context context, @NonNull ViewGroup viewGroup, int i) {
        return new QuickViewHolder(R.layout.item_address_list_cell,viewGroup);
    }
}

