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
import com.yaoxin.appbase.model.GroupInfoBean;
import com.yaoxin.appbase.model.UserBean;
import com.yaoxin.appbase.utils.GlideUtil;

public class NewFriendListAdapter extends BaseQuickAdapter<UserBean, QuickViewHolder> {

    @Override
    protected void onBindViewHolder(@NonNull QuickViewHolder quickViewHolder, int i, @Nullable UserBean orderListBean) {

        ImageView imageView = quickViewHolder.getView(com.yaoxin.appbase.R.id.item_group_list_cell_iv);
        TextView seeTv = quickViewHolder.getView(com.yaoxin.appbase.R.id.item_group_list_cell_see_tv);
        if (orderListBean.avatar.startsWith("https://s.netease.im") || orderListBean.avatar.isEmpty()) {
            imageView.setImageResource(com.yaoxin.appbase.R.mipmap.app_default_base_icon_geren);
        } else {
            GlideUtil.yh_loadImage(getContext(),imageView,orderListBean.avatar);
        }
        seeTv.setVisibility(View.VISIBLE);
        quickViewHolder.setText(com.yaoxin.appbase.R.id.item_group_list_cell_tv,orderListBean.name);
    }

    @NonNull
    @Override
    protected QuickViewHolder onCreateViewHolder(@NonNull Context context, @NonNull ViewGroup viewGroup, int i) {
        return new QuickViewHolder(com.yaoxin.appbase.R.layout.item_group_list_cell,viewGroup);
    }
}

