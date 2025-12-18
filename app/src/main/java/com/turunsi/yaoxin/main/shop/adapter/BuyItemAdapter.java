package com.turunsi.yaoxin.main.shop.adapter;

import android.content.Context;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.chad.library.adapter4.BaseQuickAdapter;
import com.chad.library.adapter4.viewholder.QuickViewHolder;
import com.turunsi.yaoxin.R;
import com.turunsi.yaoxin.main.shop.model.ProductModel;
import com.yaoxin.appbase.utils.GlideUtil;

import java.util.ArrayList;

/**
 * 购买商品项适配器（横向列表）
 */
public class BuyItemAdapter extends BaseQuickAdapter<ProductModel, QuickViewHolder> {
    
    public BuyItemAdapter(Context context) {
        super(new ArrayList<>());
    }
    
    @Override
    protected void onBindViewHolder(@NonNull QuickViewHolder holder, int position, @Nullable ProductModel item) {
        if (item == null) return;
        
        // 商品图片（从assets加载）
        ImageView iconIv = holder.getView(R.id.item_buy_product_icon_iv);
        if (item.productCoverImages != null && !item.productCoverImages.isEmpty()) {
            String imageName = item.productCoverImages.get(0);
            // 从assets加载图片：file:///android_asset/images/文件名
            String assetPath = "file:///android_asset/images/" + imageName;
            GlideUtil.yh_loadImageRoundedCorner(getContext(), iconIv, assetPath, 10);
        }
        
        // 商品名称
        holder.setText(R.id.item_buy_product_name_tv, item.productName);
        
        // 价格
        holder.setText(R.id.item_buy_product_price_tv, "¥" + (int)item.monthlyRentPrice + "/月");
    }
    
    @NonNull
    @Override
    protected QuickViewHolder onCreateViewHolder(@NonNull Context context, @NonNull ViewGroup parent, int viewType) {
        return new QuickViewHolder(R.layout.item_buy_product_cell, parent);
    }
}

