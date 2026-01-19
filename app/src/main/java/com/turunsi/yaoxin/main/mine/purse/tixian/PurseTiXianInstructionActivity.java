package com.turunsi.yaoxin.main.mine.purse.tixian;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.Nullable;

import com.turunsi.yaoxin.databinding.ActivityMinePurseTixianInstructionBinding;
import com.yaoxin.appbase.activity.BaseActivity;
import com.yaoxin.appbase.utils.StatusBarUtils;

public class PurseTiXianInstructionActivity extends BaseActivity implements View.OnClickListener {

    private ActivityMinePurseTixianInstructionBinding binding;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMinePurseTixianInstructionBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        StatusBarUtils.transtStatusBar(this, binding.activityMinePurseTixianInstructionNav);
        binding.activityMinePurseTixianInstructionNav.addCloseImageButton().setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if (v == binding.activityMinePurseTixianInstructionNav.addCloseImageButton()) {
            finish();
        }
    }
}
