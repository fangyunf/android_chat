package com.turunsi.yaoxin.main.mine.collection.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.chad.library.adapter4.BaseQuickAdapter;
import com.chad.library.adapter4.viewholder.QuickViewHolder;
import com.netease.nimlib.sdk.msg.model.CollectInfo;
import com.turunsi.yaoxin.R;
import com.turunsi.yaoxin.main.mine.collection.bean.CollectionListBean;
import com.turunsi.yaoxin.main.mine.order.bean.OrderListBean;
import com.yaoxin.appbase.utils.AESUtil;
import com.yaoxin.appbase.utils.GlideUtil;
import com.yaoxin.appbase.utils.TimeUtil;

public class CollectionListAdapter extends BaseQuickAdapter<CollectInfo, QuickViewHolder> {

    @Override
    protected void onBindViewHolder(@NonNull QuickViewHolder quickViewHolder, int i, @Nullable CollectInfo orderListBean) {
        String content = orderListBean.getData();
        try {
            content = AESUtil.decryptWithFallback(content);
        }catch (Exception e) {

        }
        ImageView imageView = quickViewHolder.findView(R.id.item_collection_list_cell_content_iv);
        LinearLayout textLl = quickViewHolder.findView(R.id.item_collection_list_cell_text_ll);
        if (imageView == null || textLl == null) {
            return;
        }
        imageView.setVisibility(View.GONE);
        textLl.setVisibility(View.GONE);
        if (orderListBean.getType() == 1) {
            imageView.setVisibility(View.VISIBLE);
            GlideUtil.yh_loadImage(getContext(),imageView,orderListBean.getData());
        } else {
            textLl.setVisibility(View.VISIBLE);
            quickViewHolder.setText(R.id.item_collection_list_cell_content_tv,content);
        }

        quickViewHolder.setText(R.id.item_collection_list_cell_date_tv, TimeUtil.stampToDate(orderListBean.getCreateTime() +""));
    }

    @NonNull
    @Override
    protected QuickViewHolder onCreateViewHolder(@NonNull Context context, @NonNull ViewGroup viewGroup, int i) {
        return new QuickViewHolder(R.layout.item_collection_list_cell,viewGroup);
    }
}

