package com.turunsi.yaoxin.main.shop;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.netease.yunxin.kit.common.utils.SizeUtils;
import com.yaoxin.appbase.fragment.BaseFragment;
import com.yaoxin.appbase.view.GridSpacingItemDecoration;
import com.turunsi.yaoxin.R;
import com.turunsi.yaoxin.databinding.FragmentShopBinding;
import com.turunsi.yaoxin.main.shop.adapter.BuyItemAdapter;
import com.turunsi.yaoxin.main.shop.adapter.ProductAdapter;
import com.turunsi.yaoxin.main.shop.manager.ProductManager;
import com.turunsi.yaoxin.main.shop.model.ProductModel;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * 商城首页Fragment
 */
public class ShopFragment extends BaseFragment {

    private FragmentShopBinding binding;
    private ProductManager productManager;
    private ProductAdapter productAdapter;
    private BuyItemAdapter buyItemAdapter;
    private List<ProductModel> productsData = new ArrayList<>();
    private List<ProductModel> buyData = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentShopBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        productManager = ProductManager.getInstance(requireContext());
        initViews();
        setupData();
    }

    @Override
    public void onResume() {
        super.onResume();
        // 每次显示时刷新数据
        setupData();
    }

    private void initViews() {
        // 设置背景
        binding.fragmentShopRootLl.setBackgroundResource(R.mipmap.home_bg);

        // 设置搜索框点击事件
        binding.fragmentShopSearchContainer.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), SearchActivity.class);
            startActivity(intent);
        });

        // 设置购买区域点击事件
        binding.fragmentShopBuyBgLl.setOnClickListener(v -> {
            // 切换到购买Tab（需要与MainActivity通信）
            // 可以通过EventBus或者回调实现
        });

        // 商品列表RecyclerView
        productAdapter = new ProductAdapter(requireContext());
        binding.fragmentShopProductRv.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.fragmentShopProductRv.setAdapter(productAdapter);

        productAdapter.setOnItemClickListener((adapter, view, position) -> {
            ProductModel product = productsData.get(position);
            Intent intent = new Intent(getActivity(), ProductDetailActivity.class);
            intent.putExtra("product", product);
            startActivity(intent);
        });

        // 购买商品3个商品RecyclerView（3列，只显示3个，不滑动）
        buyItemAdapter = new BuyItemAdapter(requireContext());
        GridLayoutManager buyGridLayoutManager = new GridLayoutManager(requireContext(), 3);
        binding.fragmentShopBuyRv.setLayoutManager(buyGridLayoutManager);
        // 禁用滑动，确保完整显示
        binding.fragmentShopBuyRv.setNestedScrollingEnabled(false);
        // 添加间距装饰器：3列，横向和纵向间距都是10dp，包含边缘（确保左右有间隙）
        GridSpacingItemDecoration spacingDecoration = new GridSpacingItemDecoration(
                3, 
                SizeUtils.dp2px(10), 
                SizeUtils.dp2px(10), 
                true
        );
        binding.fragmentShopBuyRv.addItemDecoration(spacingDecoration);
        binding.fragmentShopBuyRv.setAdapter(buyItemAdapter);

        buyItemAdapter.setOnItemClickListener((adapter, view, position) -> {
            ProductModel product = buyData.get(position);
            Intent intent = new Intent(getActivity(), ProductDetailActivity.class);
            intent.putExtra("product", product);
            startActivity(intent);
        });
    }

    private void setupData() {
        // 加载商品数据
        productManager.loadProducts();
        productManager.loadBuyProducts();

        // 获取随机商品
        productsData = productManager.getRandomProducts();
        productAdapter.submitList(new ArrayList<>(productsData));

        // 获取随机购买商品（只显示3个）
        buyData = getRandomThreeUniqueFromList(productManager.getBuyProducts());
        buyItemAdapter.submitList(new ArrayList<>(buyData));
    }

    /**
     * 从列表中随机选择3个不重复的商品
     */
    private List<ProductModel> getRandomThreeUniqueFromList(List<ProductModel> sourceList) {
        if (sourceList == null || sourceList.isEmpty()) {
            return new ArrayList<>();
        }

        if (sourceList.size() <= 3) {
            return new ArrayList<>(sourceList);
        }

        List<ProductModel> mutableCopy = new ArrayList<>(sourceList);
        List<ProductModel> result = new ArrayList<>();
        Random random = new Random();

        for (int i = 0; i < 3; i++) {
            int randomIndex = random.nextInt(mutableCopy.size());
            result.add(mutableCopy.remove(randomIndex));
        }

        return result;
    }
}
