package com.netease.yunxin.kit.contactkit.ui.normal.contact.adapter;

import android.content.Context;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.chad.library.adapter4.BaseQuickAdapter;
import com.chad.library.adapter4.viewholder.QuickViewHolder;
import com.netease.yunxin.kit.contactkit.ui.R;
import com.yaoxin.appbase.model.GroupInfoBean;

public class ShopaddressListAdapter extends BaseQuickAdapter<GroupInfoBean, QuickViewHolder> {

public int opt_type = 0;

    @Override
    protected void onBindViewHolder(@NonNull QuickViewHolder quickViewHolder, int i, @Nullable GroupInfoBean infoBean) {
            ImageView iv = quickViewHolder.getView(R.id.shop_fragment_item_cell_shop_iv);

            TextView tv1 = quickViewHolder.getView(R.id.shop_fragment_address_list_cell_name_tv);
            TextView tv2 = quickViewHolder.getView(R.id.shop_fragment_address_list_cell_phone_tv);
            TextView tv3 = quickViewHolder.getView(R.id.shop_fragment_address_list_cell_address_tv);
    }
    @NonNull
    @Override
    protected QuickViewHolder onCreateViewHolder(@NonNull Context context, @NonNull ViewGroup viewGroup, int i) {
        return new QuickViewHolder(R.layout.shop_fragment_address_list_cell,viewGroup);
    }
}

