package com.turunsi.yaoxin.main.shop.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.chad.library.adapter4.BaseQuickAdapter;
import com.chad.library.adapter4.viewholder.QuickViewHolder;
import com.turunsi.yaoxin.R;
import com.turunsi.yaoxin.main.shop.model.ProductModel;
import com.turunsi.yaoxin.main.shop.manager.ProductManager;
import com.yaoxin.appbase.utils.GlideUtil;

import java.util.ArrayList;

/**
 * 商品列表适配器
 */
public class ProductAdapter extends BaseQuickAdapter<ProductModel, QuickViewHolder> {
    
    private ProductManager productManager;
    private OnCollectClickListener onCollectClickListener;
    
    public interface OnCollectClickListener {
        void onCollectClick(int position);
    }
    
    public ProductAdapter(Context context) {
        super(new ArrayList<>());
        this.productManager = ProductManager.getInstance(context);
    }
    
    public void setOnCollectClickListener(OnCollectClickListener listener) {
        this.onCollectClickListener = listener;
    }
    
    @Override
    protected void onBindViewHolder(@NonNull QuickViewHolder holder, int position, @Nullable ProductModel item) {
        if (item == null) return;
        
        // 商品图片（从assets加载）
        ImageView productIv = holder.getView(R.id.item_product_image_iv);
        if (item.productCoverImages != null && !item.productCoverImages.isEmpty()) {
            String imageName = item.productCoverImages.get(0);
            // 从assets加载图片：file:///android_asset/images/文件名
            String assetPath = "file:///android_asset/images/" + imageName;
            GlideUtil.yh_loadImageRoundedCorner(getContext(), productIv, assetPath, 10);
        }
        
        // 商品名称
        holder.setText(R.id.item_product_name_tv, item.productName);
        
        // 价格
        holder.setText(R.id.item_product_price_tv, "¥" + (int)item.monthlyRentPrice);
        
        // 收藏按钮
        ImageView collectIv = holder.getView(R.id.item_product_collect_iv);
        collectIv.setSelected(item.isCollected);
        collectIv.setOnClickListener(v -> {
            productManager.toggleCollectStatusForProduct(item.productId);
            item.isCollected = !item.isCollected;
            collectIv.setSelected(item.isCollected);
            if (onCollectClickListener != null) {
                onCollectClickListener.onCollectClick(position);
            }
        });
        
        // 立即租按钮
        TextView rentBtn = holder.getView(R.id.item_product_rent_tv);
        rentBtn.setOnClickListener(v -> {
            // 点击事件在Fragment中处理
        });
    }
    
    @NonNull
    @Override
    protected QuickViewHolder onCreateViewHolder(@NonNull Context context, @NonNull ViewGroup parent, int viewType) {
        return new QuickViewHolder(R.layout.item_product_cell, parent);
    }
}

