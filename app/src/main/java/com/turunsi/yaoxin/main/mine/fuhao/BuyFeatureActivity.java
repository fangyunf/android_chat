package com.turunsi.yaoxin.main.mine.fuhao;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.GridLayoutManager;

import com.chad.library.adapter4.BaseQuickAdapter;
import com.netease.yunxin.kit.common.utils.SizeUtils;
import com.turunsi.yaoxin.R;
import com.turunsi.yaoxin.databinding.ActivityBuyFeatureBinding;
import com.turunsi.yaoxin.databinding.ActivityMineMyFuhaoListBinding;
import com.turunsi.yaoxin.main.mine.fuhao.adapter.MyFuHaoListAdapter;
import com.yaoxin.appbase.activity.BaseActivity;
import com.yaoxin.appbase.model.UserBean;
import com.yaoxin.appbase.utils.DataUtil;
import com.yaoxin.appbase.view.CommonGridSpacingItemDecoration;

import java.util.ArrayList;

public class BuyFeatureActivity extends BaseActivity implements View.OnClickListener {
    ActivityBuyFeatureBinding binding;

    int _type = 0;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (extras.get("type") != null) {
            _type = Integer.parseInt((String) extras.get("type"));
        }
        binding = ActivityBuyFeatureBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        binding.activityBuyFeatureNav.addCloseImageButton().setOnClickListener(this);
        binding.activityBuyFeatureShuomingTv.setOnClickListener(this);
        binding.activityBuyFeatureBuyTv.setOnClickListener(this);
        _updateUI();
    }

    void _updateUI() {

        if (_type == 0) {
            binding.activityBuyFeatureNav.getTitleView().setText("购买副号");
            binding.activityBuyFeatureMoneyTv.setText("￥68");
            binding.activityBuyFeatureDetailTv.setText("购买即得20个副号");
            binding.activityBuyFeatureShuomingTv.setVisibility(View.GONE);
            binding.activityBuyFeatureIv.setImageResource(R.mipmap.buy_feature_fuhao);
        }
        if (_type == 1) {
            binding.activityBuyFeatureBottomLl.setVisibility(View.VISIBLE);
            binding.activityBuyFeatureIvRl.setVisibility(View.VISIBLE);
            binding.activityBuyFeatureNav.getTitleView().setText("升级群组");
            binding.activityBuyFeatureMoneyTv.setText("￥88");
            binding.activityBuyFeatureDetailTv.setText("购买即升级当前群组为1000人群");
            binding.activityBuyFeatureShuomingTv.setVisibility(View.VISIBLE);
            binding.activityBuyFeatureIv.setImageResource(R.mipmap.buy_feature_group);
            binding.activityBuyFeatureUpdateInfoTv.setVisibility(View.GONE);
        }
        if (_type == 2) {
            binding.activityBuyFeatureUpdateInfoTv.setVisibility(View.VISIBLE);
            binding.activityBuyFeatureBottomLl.setVisibility(View.GONE);
            binding.activityBuyFeatureIvRl.setVisibility(View.GONE);
            binding.activityBuyFeatureNav.getTitleView().setText("升级规则");
        }
    }
    @Override
    protected void _requestData() {

    }

    @Override
    public void onClick(View v) {
        if (v == binding.activityBuyFeatureNav.addCloseImageButton()) {
            if (_type == 2) {
                _type = 1;
                _updateUI();
            } else {
                finish();
            }
        } else if (v == binding.activityBuyFeatureShuomingTv) {
            _type = 2;
            _updateUI();
        }
    }

}
