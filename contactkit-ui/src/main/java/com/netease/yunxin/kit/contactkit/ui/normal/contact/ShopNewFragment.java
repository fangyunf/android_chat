// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.contactkit.ui.normal.contact;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.GridLayoutManager;

import com.chad.library.adapter4.BaseQuickAdapter;
import com.netease.yunxin.kit.common.utils.SizeUtils;
import com.netease.yunxin.kit.contactkit.ui.databinding.ShopNewFragmentBinding;
import com.netease.yunxin.kit.contactkit.ui.normal.contact.adapter.ShoprListAdapter;
import com.yaoxin.appbase.fragment.BaseFragment;
import com.yaoxin.appbase.model.GroupInfoBean;
import com.yaoxin.appbase.view.CommonGridSpacingItemDecoration;

import java.util.ArrayList;
import java.util.List;

/**
 * contact page
 */
public class ShopNewFragment extends BaseFragment {
    private final String TAG = "ContactFragment";
    private ShopNewFragmentBinding binding;
    ShoprListAdapter adapter = new ShoprListAdapter();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = ShopNewFragmentBinding.inflate(inflater, container, false);

        GridLayoutManager gridLayoutManager = new GridLayoutManager(getContext(), 2);
        binding.shopNewFragmentRv.setLayoutManager(gridLayoutManager);
        CommonGridSpacingItemDecoration gridSpacingItemDecoration =
                new CommonGridSpacingItemDecoration(2, SizeUtils.dp2px(10), false);
        binding.shopNewFragmentRv.addItemDecoration(gridSpacingItemDecoration);
        List<GroupInfoBean> data = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            GroupInfoBean groupInfoBean = new GroupInfoBean();
            groupInfoBean.name = "name" + i;
            groupInfoBean.price = i;
            data.add(groupInfoBean);
        }
        adapter.setItems(data);
        binding.shopNewFragmentRv.setAdapter(adapter);
        adapter.setOnItemClickListener(new BaseQuickAdapter.OnItemClickListener<GroupInfoBean>() {
            @Override
            public void onClick(@NonNull BaseQuickAdapter<GroupInfoBean, ?> baseQuickAdapter, @NonNull View view, int i) {
                SubmitOrderActivity.start(SubmitOrderActivity.class, requireActivity(), null);
            }
        });
        return binding.getRoot();
    }



    @Override
    public void onPause() {
        super.onPause();
    }

}
