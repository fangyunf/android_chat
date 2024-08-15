package com.turunsi.yaoxin.eggs;

import android.content.Context;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.chad.library.adapter4.BaseQuickAdapter;
import com.chad.library.adapter4.viewholder.QuickViewHolder;
import com.netease.yunxin.kit.common.ui.utils.AvatarColor;
import com.turunsi.yaoxin.R;
import com.turunsi.yaoxin.main.mine.address.bean.AddressListBean;
import com.yaoxin.appbase.model.GroupInfoBean;
import com.yaoxin.appbase.utils.GlideUtil;

public class GroupListAdapter extends BaseQuickAdapter<GroupInfoBean, QuickViewHolder> {

    @Override
    protected void onBindViewHolder(@NonNull QuickViewHolder quickViewHolder, int i, @Nullable GroupInfoBean orderListBean) {

        ImageView imageView = quickViewHolder.getView(R.id.item_group_list_cell_iv);
        if (orderListBean.head.startsWith("https://s.netease.im") || orderListBean.head.isEmpty()) {
            imageView.setImageResource(com.yaoxin.appbase.R.mipmap.app_default_base_icon_group);
        } else {
            GlideUtil.yh_loadImage(getContext(),imageView,orderListBean.head);
        }

            ImageView selIv = quickViewHolder.getView(R.id.item_group_list_cell_sel_iv);
        selIv.setSelected(orderListBean.isSelected);
        quickViewHolder.setText(R.id.item_group_list_cell_tv,orderListBean.name);
    }

    @NonNull
    @Override
    protected QuickViewHolder onCreateViewHolder(@NonNull Context context, @NonNull ViewGroup viewGroup, int i) {
        return new QuickViewHolder(R.layout.item_group_list_cell,viewGroup);
    }
}

