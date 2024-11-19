package com.netease.yunxin.kit.contactkit.ui.normal.contact.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.chad.library.adapter4.BaseQuickAdapter;
import com.chad.library.adapter4.viewholder.QuickViewHolder;
import com.netease.yunxin.kit.contactkit.ui.R;
import com.yaoxin.appbase.model.GroupInfoBean;
import com.yaoxin.appbase.utils.GlideUtil;

public class ShoprListAdapter extends BaseQuickAdapter<GroupInfoBean, QuickViewHolder> {

public int opt_type = 0;

    @Override
    protected void onBindViewHolder(@NonNull QuickViewHolder quickViewHolder, int i, @Nullable GroupInfoBean infoBean) {
            ImageView iv = quickViewHolder.getView(R.id.shop_fragment_item_cell_shop_iv);
            GlideUtil.yh_loadImageRoundedCorner(getContext(), iv, infoBean.avatar, 22);
            TextView tv1 = quickViewHolder.getView(R.id.shop_fragment_item_cell_shop_name_tv);
            TextView tv2 = quickViewHolder.getView(R.id.shop_fragment_item_cell_shop_price_tv);
            tv1.setText(infoBean.name);
            tv2.setText(infoBean.price1);
    }
    @NonNull
    @Override
    protected QuickViewHolder onCreateViewHolder(@NonNull Context context, @NonNull ViewGroup viewGroup, int i) {
        return new QuickViewHolder(R.layout.shop_fragment_item_cell,viewGroup);
    }
}

