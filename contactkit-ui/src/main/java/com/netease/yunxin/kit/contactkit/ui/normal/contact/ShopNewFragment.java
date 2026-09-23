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

import com.chad.library.adapter4.BaseQuickAdapter;
import com.google.gson.Gson;
import com.netease.yunxin.kit.common.utils.SizeUtils;
import com.netease.yunxin.kit.contactkit.ui.databinding.ShopNewFragmentBinding;
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
 * 商城页：商品数据来自 assets/shop_mid_autumn.json
 */
public class ShopNewFragment extends BaseFragment {
    private ShopNewFragmentBinding binding;
    private final ShoprListAdapter adapter = new ShoprListAdapter();
    private final List<GroupInfoBean> allProducts = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        binding = ShopNewFragmentBinding.inflate(inflater, container, false);
        GridLayoutManager gridLayoutManager = new GridLayoutManager(getContext(), 2);
        binding.shopNewFragmentRv.setLayoutManager(gridLayoutManager);
        CommonGridSpacingItemDecoration gridSpacingItemDecoration =
                new CommonGridSpacingItemDecoration(2, SizeUtils.dp2px(8), false);
        binding.shopNewFragmentRv.addItemDecoration(gridSpacingItemDecoration);
        binding.shopNewFragmentRv.setNestedScrollingEnabled(false);
        binding.shopNewFragmentRv.setAdapter(adapter);

        loadShopData();

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
                        filterProducts(s == null ? "" : s.toString().trim());
                    }
                });
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ViewGroup.MarginLayoutParams layoutParams =
                (ViewGroup.MarginLayoutParams) binding.tvTitle.getLayoutParams();
        layoutParams.topMargin = BarUtils.getStatusBarHeight() + SizeUtils.dp2px(20);
        binding.tvTitle.setLayoutParams(layoutParams);
    }

    private void loadShopData() {
        try {
            String json = readAsset("shop_mid_autumn.json");
            ShopData shopData = new Gson().fromJson(json, ShopData.class);
            if (shopData == null) {
                return;
            }
            if (!TextUtils.isEmpty(shopData.searchPlaceholder)) {
                binding.funConversationFragmentEt.setHint(shopData.searchPlaceholder);
            }
            if (!TextUtils.isEmpty(shopData.sectionTitle)) {
                binding.shopNewFragmentSectionTv.setText(shopData.sectionTitle);
            }
            if (shopData.banner != null) {
                if (!TextUtils.isEmpty(shopData.banner.title)) {
                    binding.shopNewFragmentBannerTitleTv.setText(shopData.banner.title);
                }
                if (!TextUtils.isEmpty(shopData.banner.subtitle)) {
                    binding.shopNewFragmentBannerSubtitleTv.setText(shopData.banner.subtitle);
                }
                if (!TextUtils.isEmpty(shopData.banner.image)) {
                    final int radiusDp =
                            shopData.banner.cornerRadius > 0 ? shopData.banner.cornerRadius : 12;
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
                                            SizeUtils.dp2px(radiusDp));
                                }
                            });
                }
            }
            allProducts.clear();
            if (shopData.products != null) {
                for (ShopProduct product : shopData.products) {
                    if (product == null) {
                        continue;
                    }
                    GroupInfoBean bean = new GroupInfoBean();
                    bean.name = product.name;
                    bean.price1 = product.price;
                    bean.price = product.priceValue;
                    bean.avatar = product.image;
                    allProducts.add(bean);
                }
            }
            adapter.setItems(new ArrayList<>(allProducts));
            adapter.notifyDataSetChanged();
        } catch (Exception ignored) {
        }
    }

    private void filterProducts(String keyword) {
        if (TextUtils.isEmpty(keyword)) {
            adapter.setItems(new ArrayList<>(allProducts));
            adapter.notifyDataSetChanged();
            return;
        }
        List<GroupInfoBean> filtered = new ArrayList<>();
        for (GroupInfoBean bean : allProducts) {
            if (bean != null && bean.name != null && bean.name.contains(keyword)) {
                filtered.add(bean);
            }
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
        public List<ShopProduct> products;
    }

    private static class ShopBanner {
        public String image;
        public String title;
        public String subtitle;
        public float maskAlpha;
        public int cornerRadius;
    }

    private static class ShopProduct {
        public String name;
        public String price;
        public int priceValue;
        public String image;
    }
}
