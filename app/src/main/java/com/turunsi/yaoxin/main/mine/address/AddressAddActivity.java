package com.turunsi.yaoxin.main.mine.address;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.Nullable;

import com.turunsi.yaoxin.R;
import com.yaoxin.appbase.activity.BaseActivity;
import com.turunsi.yaoxin.databinding.ActivityMineAddressAddBinding;

public class AddressAddActivity extends BaseActivity implements View.OnClickListener {
    ActivityMineAddressAddBinding binding;

    private boolean isDefault = true;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMineAddressAddBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        binding.activityMineAddressAddNav.addCloseImageButton().setOnClickListener(this);
        binding.activityMineAddressAddIsDefalutIv.setOnClickListener(this);

    }

    @Override
    public void onClick(View v) {
        if (v == binding.activityMineAddressAddNav.addCloseImageButton()) {
            finish();
        } else if (binding.activityMineAddressAddIsDefalutIv == v) {
            binding.activityMineAddressAddIsDefalutIv.setImageResource(isDefault ? com.yaoxin.appbase.R.mipmap.common_switch_state_default: com.yaoxin.appbase.R.mipmap.common_switch_state_seleted);
            isDefault = !isDefault;
        }
    }

}
