package com.turunsi.yaoxin.main.shop;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.yaoxin.appbase.activity.BaseActivity;
import com.yaoxin.appbase.navbar.NavToolbar;
import com.turunsi.yaoxin.R;
import com.turunsi.yaoxin.databinding.ActivityProductDetailBinding;
import com.turunsi.yaoxin.main.shop.adapter.ProductImageAdapter;
import com.turunsi.yaoxin.main.shop.dialog.BuyConfirmDialog;
import com.turunsi.yaoxin.main.shop.manager.ProductManager;
import com.turunsi.yaoxin.main.shop.model.ProductModel;
import com.turunsi.yaoxin.main.shop.utils.ImageLoadUtil;
import com.turunsi.yaoxin.main.mine.address.bean.AddressListBean;
import com.yaoxin.appbase.utils.StatusBarUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * 商品详情页
 */
public class ProductDetailActivity extends BaseActivity {

    private ActivityProductDetailBinding binding;
    private ProductModel productModel;
    private ProductManager productManager;
    private BuyConfirmDialog buyConfirmDialog;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityProductDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        StatusBarUtils.transtStatusBar(this, binding.activityProductDetailNav);
        productModel = (ProductModel) getIntent().getSerializableExtra("product");
        if (productModel == null) {
            finish();
            return;
        }

        productManager = ProductManager.getInstance(this);
        initViews();
        setupData();
    }

    private void initViews() {
        // 返回按钮（布局文件中已设置back_icon，这里只需要设置点击事件）
        binding.activityProductDetailNav.setBackIconClickListener(() -> finish());

        // 收藏按钮
        View favoriteBtn = findViewById(R.id.activity_product_detail_favorite_btn);
        if (favoriteBtn != null) {
            favoriteBtn.setOnClickListener(v -> {
                productManager.toggleCollectStatusForProduct(productModel.productId);
                productModel.isCollected = !productModel.isCollected;
                updateFavoriteButton();
            });
        }

        // 立即购买按钮
        binding.activityProductDetailBuyBtn.setOnClickListener(v -> {
            showBuyDialog();
        });

        // 商品图片轮播
        ProductImageAdapter imageAdapter = new ProductImageAdapter(productModel.productCoverImages);
        binding.activityProductDetailViewPager.setAdapter(imageAdapter);
        binding.activityProductDetailViewPager.setOrientation(ViewPager2.ORIENTATION_HORIZONTAL);
    }

    private void setupData() {
        // 价格
        binding.activityProductDetailPriceTv.setText("¥" + (int) productModel.monthlyRentPrice + "/月");

        // 商品名称
        binding.activityProductDetailTitleTv.setText(productModel.productName);

        // 推荐标签
        if (productModel.productRecommendationTags != null && !productModel.productRecommendationTags.isEmpty()) {
            binding.activityProductDetailSubtitleTv.setText(productModel.productRecommendationTags);
            binding.activityProductDetailSubtitleTv.setVisibility(View.VISIBLE);
        } else {
            binding.activityProductDetailSubtitleTv.setVisibility(View.GONE);
        }

        // 详情图片（从assets加载）
        if (productModel.productDetailImage != null && !productModel.productDetailImage.isEmpty()) {
            // 从assets加载图片：images/文件名
            String assetPath = "images/" + productModel.productDetailImage;
            ImageLoadUtil.loadFromAssets(this, binding.activityProductDetailDetailIv, assetPath);
            binding.activityProductDetailDetailIv.setVisibility(View.VISIBLE);
        } else {
            binding.activityProductDetailDetailIv.setVisibility(View.GONE);
        }

        // 更新收藏按钮状态
        updateFavoriteButton();
    }

    private void updateFavoriteButton() {
        View favoriteBtn = findViewById(R.id.activity_product_detail_favorite_btn);
        if (favoriteBtn != null) {
            favoriteBtn.setSelected(productModel.isCollected);
            TextView favoriteTv = favoriteBtn.findViewById(R.id.activity_product_detail_favorite_tv);
            ImageView favoriteIv = favoriteBtn.findViewById(R.id.activity_product_detail_favorite_iv);
            if (favoriteTv != null) {
                favoriteTv.setText(productModel.isCollected ? "已收藏" : "收藏");
            }
            if (favoriteIv != null) {
                favoriteIv.setSelected(productModel.isCollected);
                favoriteIv.setImageResource(productModel.isCollected ?
                        R.mipmap.icon_collect_sel : R.mipmap.icon_collect);
            }
        }
    }

    /**
     * 显示购买对话框
     */
    private void showBuyDialog() {
        if (buyConfirmDialog == null) {
            buyConfirmDialog = new BuyConfirmDialog(this, productModel);
            buyConfirmDialog.setOnConfirmListener((product, months, address) -> {
                // TODO: 处理购买确认，跳转到支付确认页面
                // Intent intent = new Intent(this, PaymentConfirmActivity.class);
                // intent.putExtra("product", product);
                // intent.putExtra("months", months);
                // intent.putExtra("address", address);
                // startActivity(intent);
            });
        }
        buyConfirmDialog.show();
    }
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 100 && resultCode == RESULT_OK && data != null) {
            AddressListBean address = (AddressListBean) data.getSerializableExtra("address");
            if (buyConfirmDialog != null && buyConfirmDialog.isShowing()) {
                buyConfirmDialog.setSelectedAddress(address);
            }
        }
    }
}

