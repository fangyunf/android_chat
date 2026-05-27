// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.turunsi.yaoxin.main.mine.setting;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.turunsi.yaoxin.R;
import com.turunsi.yaoxin.databinding.ActivityFontSizeSettingBinding;
import com.yaoxin.appbase.activity.BaseActivity;
import com.yaoxin.appbase.utils.StatusBarUtils;

/**
 * 字体大小设置（仅展示大/中/小选项界面，暂不生效）
 */
public class FontSizeSettingActivity extends BaseActivity implements View.OnClickListener {

    private static final int SIZE_SMALL = 0;
    private static final int SIZE_MEDIUM = 1;
    private static final int SIZE_LARGE = 2;

    private ActivityFontSizeSettingBinding viewBinding;
    private int selectedSize = SIZE_MEDIUM;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewBinding = ActivityFontSizeSettingBinding.inflate(getLayoutInflater());
        setContentView(viewBinding.getRoot());
        StatusBarUtils.transtStatusBar(this, viewBinding.activityFontSizeNav);
        initView();
    }

    private void initView() {
        viewBinding.activityFontSizeNav.addCloseImageButton().setOnClickListener(this);
        viewBinding.activityFontSizeSmallLl.setOnClickListener(this);
        viewBinding.activityFontSizeMediumLl.setOnClickListener(this);
        viewBinding.activityFontSizeLargeLl.setOnClickListener(this);
        updateSelectionUi();
    }

    @Override
    public void onClick(View v) {
        if (v == viewBinding.activityFontSizeNav.addCloseImageButton()) {
            finish();
            return;
        }
        if (v == viewBinding.activityFontSizeSmallLl) {
            selectedSize = SIZE_SMALL;
        } else if (v == viewBinding.activityFontSizeMediumLl) {
            selectedSize = SIZE_MEDIUM;
        } else if (v == viewBinding.activityFontSizeLargeLl) {
            selectedSize = SIZE_LARGE;
        }
        updateSelectionUi();
    }

    private void updateSelectionUi() {
        int checkedColor = getResources().getColor(R.color.tab_checked_color);
        int normalColor = getResources().getColor(R.color.color_333333);

        setOptionStyle(
                viewBinding.activityFontSizeSmallLabel,
                viewBinding.activityFontSizeSmallCheck,
                selectedSize == SIZE_SMALL,
                14f,
                checkedColor,
                normalColor);
        setOptionStyle(
                viewBinding.activityFontSizeMediumLabel,
                viewBinding.activityFontSizeMediumCheck,
                selectedSize == SIZE_MEDIUM,
                16f,
                checkedColor,
                normalColor);
        setOptionStyle(
                viewBinding.activityFontSizeLargeLabel,
                viewBinding.activityFontSizeLargeCheck,
                selectedSize == SIZE_LARGE,
                18f,
                checkedColor,
                normalColor);

        float previewSp = 16f;
        if (selectedSize == SIZE_SMALL) {
            previewSp = 14f;
        } else if (selectedSize == SIZE_LARGE) {
            previewSp = 20f;
        }
        viewBinding.activityFontSizePreviewTv.setTextSize(previewSp);
    }

    private void setOptionStyle(
            TextView label,
            TextView check,
            boolean selected,
            float labelSp,
            int checkedColor,
            int normalColor) {
        label.setTextSize(labelSp);
        label.setTextColor(selected ? checkedColor : normalColor);
        check.setVisibility(selected ? View.VISIBLE : View.GONE);
    }
}
