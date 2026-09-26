// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.contactkit.ui.normal.contact;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.chad.library.adapter4.BaseQuickAdapter;
import com.google.gson.Gson;
import com.netease.yunxin.kit.common.utils.SizeUtils;
import com.netease.yunxin.kit.contactkit.ui.databinding.ShopNewFragmentBinding;
import com.netease.yunxin.kit.contactkit.ui.normal.contact.adapter.ShopCategoryAdapter;
import com.netease.yunxin.kit.contactkit.ui.normal.contact.adapter.ShoprListAdapter;
import com.yaoxin.appbase.fragment.BaseFragment;
import com.yaoxin.appbase.model.GroupInfoBean;
import com.yaoxin.appbase.utils.BarUtils;
import com.yaoxin.appbase.utils.GlideUtil;
import com.yaoxin.appbase.view.CommonGridSpacingItemDecoration;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 商城页：商品数据来自 assets/shop_android_export.json
 */
public class ShopNewFragment extends BaseFragment {
    private ShopNewFragmentBinding binding;
    private final ShoprListAdapter adapter = new ShoprListAdapter();
    private final ShopCategoryAdapter categoryAdapter = new ShopCategoryAdapter();
    private final List<GroupInfoBean> allProducts = new ArrayList<>();
    private String selectedCategory = "全部";
    private String keyword = "";

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        binding = ShopNewFragmentBinding.inflate(inflater, container, false);

        LinearLayoutManager categoryLm =
                new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false);
        binding.shopNewFragmentCategoryRv.setLayoutManager(categoryLm);
        binding.shopNewFragmentCategoryRv.setAdapter(categoryAdapter);
        binding.shopNewFragmentCategoryRv.setNestedScrollingEnabled(false);

        GridLayoutManager gridLayoutManager = new GridLayoutManager(getContext(), 2);
        binding.shopNewFragmentRv.setLayoutManager(gridLayoutManager);
        CommonGridSpacingItemDecoration gridSpacingItemDecoration =
                new CommonGridSpacingItemDecoration(2, SizeUtils.dp2px(8), false);
        binding.shopNewFragmentRv.addItemDecoration(gridSpacingItemDecoration);
        binding.shopNewFragmentRv.setNestedScrollingEnabled(false);
        binding.shopNewFragmentRv.setAdapter(adapter);

        loadShopData();

        categoryAdapter.setOnItemClickListener(
                (baseQuickAdapter, view, i) -> {
                    String category = categoryAdapter.getItem(i);
                    if (TextUtils.isEmpty(category)) {
                        return;
                    }
                    categoryAdapter.setSelectedIndex(i);
                    selectedCategory = category;
                    applyFilter();
                });

        adapter.setOnItemClickListener(
                new BaseQuickAdapter.OnItemClickListener<GroupInfoBean>() {
                    @Override
                    public void onClick(
                            @NonNull BaseQuickAdapter<GroupInfoBean, ?> baseQuickAdapter,
                            @NonNull View view,
                            int i) {
                        Map map = new HashMap();
                        map.put("data", new Gson().toJson(adapter.getItem(i)));
                        SubmitOrderActivity.start(
                                SubmitOrderActivity.class, requireActivity(), map);
                    }
                });

        binding.funConversationFragmentEt.addTextChangedListener(
                new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {}

                    @Override
                    public void afterTextChanged(Editable s) {
                        keyword = s == null ? "" : s.toString().trim();
                        applyFilter();
                    }
                });
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ViewGroup.MarginLayoutParams layoutParams =
                (ViewGroup.MarginLayoutParams) binding.tvTitle.getLayoutParams();
        layoutParams.topMargin = BarUtils.getStatusBarHeight() + SizeUtils.dp2px(12);
        binding.tvTitle.setLayoutParams(layoutParams);
    }

    private void loadShopData() {
        try {
            String json = readAsset("shop_android_export.json");
            ShopData shopData = new Gson().fromJson(json, ShopData.class);
            if (shopData == null) {
                return;
            }
            if (!TextUtils.isEmpty(shopData.searchPlaceholder)) {
                binding.funConversationFragmentEt.setHint(shopData.searchPlaceholder);
            }
            binding.shopNewFragmentSectionTv.setText("热门推荐");
            if (shopData.banner != null) {
                if (!TextUtils.isEmpty(shopData.banner.badge)) {
                    binding.shopNewFragmentBannerBadgeTv.setText(shopData.banner.badge);
                    binding.shopNewFragmentBannerBadgeTv.setVisibility(View.VISIBLE);
                }
                if (!TextUtils.isEmpty(shopData.banner.title)) {
                    binding.shopNewFragmentBannerTitleTv.setText(shopData.banner.title);
                }
                if (!TextUtils.isEmpty(shopData.banner.subtitle)) {
                    binding.shopNewFragmentBannerSubtitleTv.setText(shopData.banner.subtitle);
                }
                if (!TextUtils.isEmpty(shopData.banner.image)) {
                    GlideUtil.yh_loadImage(
                            requireContext(),
                            binding.shopNewFragmentBannerIv,
                            shopData.banner.image);
                    binding.shopNewFragmentBannerFl.setClipToOutline(true);
                    binding.shopNewFragmentBannerFl.setOutlineProvider(
                            new android.view.ViewOutlineProvider() {
                                @Override
                                public void getOutline(
                                        android.view.View view, android.graphics.Outline outline) {
                                    outline.setRoundRect(
                                            0,
                                            0,
                                            view.getWidth(),
                                            view.getHeight(),
                                            SizeUtils.dp2px(12));
                                }
                            });
                }
            }

            List<String> categories = shopData.categories;
            if (categories == null || categories.isEmpty()) {
                categories = new ArrayList<>();
                categories.add("全部");
            }
            categoryAdapter.setItems(categories);
            categoryAdapter.setSelectedIndex(0);
            selectedCategory = categories.get(0);
            categoryAdapter.notifyDataSetChanged();

            allProducts.clear();
            List<ShopProduct> goods =
                    shopData.goods != null
                            ? shopData.goods
                            : (shopData.products != null ? shopData.products : null);
            if (goods != null) {
                for (ShopProduct product : goods) {
                    if (product == null) {
                        continue;
                    }
                    GroupInfoBean bean = new GroupInfoBean();
                    bean.name = product.name;
                    int priceVal = product.price > 0 ? product.price : product.priceValue;
                    bean.price = priceVal;
                    if (!TextUtils.isEmpty(product.priceText)) {
                        bean.price1 = product.priceText;
                    } else {
                        bean.price1 = "¥" + priceVal;
                    }
                    bean.avatar = product.image;
                    bean.remark = product.category;
                    bean.title = product.sold;
                    allProducts.add(bean);
                }
            }
            applyFilter();
        } catch (Exception ignored) {
        }
    }

    private void applyFilter() {
        List<GroupInfoBean> filtered = new ArrayList<>();
        for (GroupInfoBean bean : allProducts) {
            if (bean == null) {
                continue;
            }
            if (!"全部".equals(selectedCategory)
                    && (TextUtils.isEmpty(bean.remark) || !selectedCategory.equals(bean.remark))) {
                continue;
            }
            if (!TextUtils.isEmpty(keyword)
                    && (TextUtils.isEmpty(bean.name) || !bean.name.contains(keyword))) {
                continue;
            }
            filtered.add(bean);
        }
        adapter.setItems(filtered);
        adapter.notifyDataSetChanged();
    }

    private String readAsset(String fileName) throws Exception {
        InputStream is = requireContext().getAssets().open(fileName);
        BufferedReader reader =
                new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            sb.append(line);
        }
        reader.close();
        return sb.toString();
    }

    private static class ShopData {
        public String searchPlaceholder;
        public String sectionTitle;
        public ShopBanner banner;
        public List<String> categories;
        public List<ShopProduct> goods;
        public List<ShopProduct> products;
    }

    private static class ShopBanner {
        public String image;
        public String badge;
        public String title;
        public String subtitle;
        public float maskAlpha;
        public int cornerRadius;
    }

    private static class ShopProduct {
        public String id;
        public String name;
        public String priceText;
        public int price;
        public int priceValue;
        public String category;
        public String sold;
        public String image;
    }
}
