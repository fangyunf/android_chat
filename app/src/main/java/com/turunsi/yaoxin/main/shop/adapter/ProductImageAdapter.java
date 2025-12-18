package com.turunsi.yaoxin.main.shop.adapter;

import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.turunsi.yaoxin.main.shop.utils.ImageLoadUtil;

import java.util.List;

/**
 * 商品图片轮播适配器
 */
public class ProductImageAdapter extends RecyclerView.Adapter<ProductImageAdapter.ViewHolder> {

    private List<String> imageUrls;

    public ProductImageAdapter(List<String> imageUrls) {
        this.imageUrls = imageUrls != null ? imageUrls : new java.util.ArrayList<>();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ImageView imageView = new ImageView(parent.getContext());
        // 设置ImageView的宽高，确保填满ViewPager2
        ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT);
        imageView.setLayoutParams(params);
        imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        imageView.setAdjustViewBounds(false);
        return new ViewHolder(imageView);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        if (position < imageUrls.size()) {
            String imageName = imageUrls.get(position);
            // 从assets加载图片：images/文件名
            String assetPath = "images/" + imageName;
            ImageLoadUtil.loadFromAssets(holder.imageView.getContext(), holder.imageView, assetPath);
        }
    }

    @Override
    public int getItemCount() {
        return imageUrls.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;

        ViewHolder(ImageView imageView) {
            super(imageView);
            this.imageView = imageView;
        }
    }
}

