package com.turunsi.yaoxin.main.mine.fuhao;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.Nullable;

import com.turunsi.yaoxin.databinding.ActivityGroupUpgradeRuleBinding;
import com.yaoxin.appbase.activity.BaseActivity;

public class GroupUpgradeRuleActivity extends BaseActivity implements View.OnClickListener {

    private ActivityGroupUpgradeRuleBinding binding;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityGroupUpgradeRuleBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        binding.activityGroupUpgradeRuleNav.addCloseImageButton().setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if (v == binding.activityGroupUpgradeRuleNav.addCloseImageButton()) {
            finish();
        }
    }
}
