package com.turunsi.yaoxin.eggs;

import android.content.Context;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.chad.library.adapter4.BaseQuickAdapter;
import com.chad.library.adapter4.viewholder.QuickViewHolder;
import com.netease.yunxin.kit.common.utils.SizeUtils;
import com.turunsi.yaoxin.R;
import com.yaoxin.appbase.model.CustomMsgBean;
import com.yaoxin.appbase.model.GroupInfoBean;
import com.yaoxin.appbase.utils.DeviceUtils;
import com.yaoxin.appbase.utils.GlideUtil;
import com.yaoxin.appbase.utils.NumberUtil;

public class EggIndexListAdapter extends BaseQuickAdapter<CustomMsgBean, QuickViewHolder> {

    @Override
    protected void onBindViewHolder(@NonNull QuickViewHolder quickViewHolder, int i, @Nullable CustomMsgBean orderListBean) {

        RelativeLayout layout = quickViewHolder.getView(R.id.egg_list_index_item_view_egg_rl);
        ImageView iv = quickViewHolder.getView(R.id.egg_list_index_item_view_egg_iv);
        TextView tv = quickViewHolder.getView(R.id.egg_list_index_item_view_egg_tv);
        ViewGroup.MarginLayoutParams layoutParams = (ViewGroup.MarginLayoutParams) layout.getLayoutParams();
        if (i < 2) {
            layoutParams.topMargin = SizeUtils.dp2px(0);
        } else {
            layoutParams.topMargin = SizeUtils.dp2px(-81);
        }
        tv.setText("￥" + NumberUtil.formartMoney(orderListBean.price));
        GlideUtil.yh_loadImage(getContext(),iv,orderListBean.img);
//        ImageView imageView = quickViewHolder.getView(com.yaoxin.appbase.R.id.item_group_list_cell_iv);
//        if (orderListBean.head.startsWith("https://s.netease.im") || orderListBean.head.isEmpty()) {
//            imageView.setImageResource(com.yaoxin.appbase.R.mipmap.app_default_base_icon_group);
//        } else {
//            GlideUtil.yh_loadImage(getContext(),imageView,orderListBean.head);
//        }
//
//            ImageView selIv = quickViewHolder.getView(com.yaoxin.appbase.R.id.item_group_list_cell_sel_iv);
//        selIv.setSelected(orderListBean.isSelected);
//        quickViewHolder.setText(com.yaoxin.appbase.R.id.item_group_list_cell_tv,orderListBean.name);
    }

    @NonNull
    @Override
    protected QuickViewHolder onCreateViewHolder(@NonNull Context context, @NonNull ViewGroup viewGroup, int i) {
        return new QuickViewHolder(R.layout.egg_list_index_item_view,viewGroup);
    }
}

