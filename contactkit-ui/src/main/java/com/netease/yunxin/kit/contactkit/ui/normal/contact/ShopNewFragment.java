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

import com.netease.yunxin.kit.contactkit.ui.databinding.ShopNewFragmentBinding;
import com.yaoxin.appbase.fragment.BaseFragment;

/**
 * contact page
 */
public class ShopNewFragment extends BaseFragment {
    private final String TAG = "ContactFragment";
    private ShopNewFragmentBinding binding;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = ShopNewFragmentBinding.inflate(inflater, container, false);

        return binding.getRoot();
    }



    @Override
    public void onPause() {
        super.onPause();
    }

}
