package com.turunsi.yaoxin.main.mine.collection.adapter;

import android.content.Context;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.chad.library.adapter4.BaseQuickAdapter;
import com.chad.library.adapter4.viewholder.QuickViewHolder;
import com.netease.nimlib.sdk.msg.model.CollectInfo;
import com.turunsi.yaoxin.R;
import com.turunsi.yaoxin.main.mine.collection.bean.CollectionListBean;
import com.turunsi.yaoxin.main.mine.order.bean.OrderListBean;
import com.yaoxin.appbase.utils.TimeUtil;

public class CollectionListAdapter extends BaseQuickAdapter<CollectInfo, QuickViewHolder> {

    @Override
    protected void onBindViewHolder(@NonNull QuickViewHolder quickViewHolder, int i, @Nullable CollectInfo orderListBean) {
        quickViewHolder.setText(R.id.item_collection_list_cell_content_tv,orderListBean.getData())
                .setText(R.id.item_collection_list_cell_date_tv, TimeUtil.stampToDate(orderListBean.getCreateTime() +""));
//                .setText(R.id.item_collection_list_cell_user_tv,orderListBean.)
    }

    @NonNull
    @Override
    protected QuickViewHolder onCreateViewHolder(@NonNull Context context, @NonNull ViewGroup viewGroup, int i) {
        return new QuickViewHolder(R.layout.item_collection_list_cell,viewGroup);
    }
}

