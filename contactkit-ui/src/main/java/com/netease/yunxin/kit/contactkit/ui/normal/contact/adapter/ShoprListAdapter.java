package com.netease.yunxin.kit.contactkit.ui.normal.contact.adapter;

import android.content.Context;
import android.graphics.Outline;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewOutlineProvider;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.chad.library.adapter4.BaseQuickAdapter;
import com.chad.library.adapter4.viewholder.QuickViewHolder;
import com.netease.yunxin.kit.common.utils.SizeUtils;
import com.netease.yunxin.kit.contactkit.ui.R;
import com.yaoxin.appbase.model.GroupInfoBean;
import com.yaoxin.appbase.utils.GlideUtil;

public class ShoprListAdapter extends BaseQuickAdapter<GroupInfoBean, QuickViewHolder> {

    public int opt_type = 0;

    @Override
    protected void onBindViewHolder(
            @NonNull QuickViewHolder quickViewHolder, int i, @Nullable GroupInfoBean infoBean) {
        if (infoBean == null) {
            return;
        }
        ImageView iv = quickViewHolder.getView(R.id.shop_fragment_item_cell_shop_iv);
        GlideUtil.yh_loadImage(getContext(), iv, infoBean.avatar);
        View root = quickViewHolder.itemView;
        root.setClipToOutline(true);
        root.setOutlineProvider(
                new ViewOutlineProvider() {
                    @Override
                    public void getOutline(View view, Outline outline) {
                        outline.setRoundRect(
                                0, 0, view.getWidth(), view.getHeight(), SizeUtils.dp2px(12));
                    }
                });
        TextView nameTv = quickViewHolder.getView(R.id.shop_fragment_item_cell_shop_name_tv);
        TextView priceTv = quickViewHolder.getView(R.id.shop_fragment_item_cell_shop_price_tv);
        TextView tagTv = quickViewHolder.getView(R.id.shop_fragment_item_cell_tag_tv);
        TextView soldTv = quickViewHolder.getView(R.id.shop_fragment_item_cell_sold_tv);
        nameTv.setText(infoBean.name);
        priceTv.setText(!TextUtils.isEmpty(infoBean.price1) ? infoBean.price1 : ("¥" + infoBean.price));
        tagTv.setText(!TextUtils.isEmpty(infoBean.remark) ? infoBean.remark : "商品");
        soldTv.setText(!TextUtils.isEmpty(infoBean.title) ? ("已售 " + infoBean.title) : "");
        soldTv.setVisibility(TextUtils.isEmpty(infoBean.title) ? View.GONE : View.VISIBLE);
    }

    @NonNull
    @Override
    protected QuickViewHolder onCreateViewHolder(
            @NonNull Context context, @NonNull ViewGroup viewGroup, int i) {
        return new QuickViewHolder(R.layout.shop_fragment_item_cell, viewGroup);
    }
}
