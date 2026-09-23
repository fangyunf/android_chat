package com.turunsi.yaoxin.main.mine.purse.pwdmanager;

import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;

import com.turunsi.yaoxin.databinding.ActivityMinePursePwdManagerBinding;
import com.yaoxin.appbase.activity.BaseActivity;
import com.yaoxin.appbase.utils.BarUtils;
import com.yaoxin.appbase.utils.StatusBarUtils;

import java.util.HashMap;

public class PursePwdManagerActivity extends BaseActivity implements View.OnClickListener {
    ActivityMinePursePwdManagerBinding binding;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMinePursePwdManagerBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        binding.activityMinePursePwdManagerNav.addCloseImageButton().setOnClickListener(this);
        StatusBarUtils.setStatusBarLightMode(this, true, true);
        LinearLayout.LayoutParams params =
                (LinearLayout.LayoutParams) binding.activityMinePursePwdManagerNav.getLayoutParams();
        params.height = params.height + BarUtils.getStatusBarHeight();
        binding.activityMinePursePwdManagerNav.setLayoutParams(params);
        binding.activityMinePursePwdManagerNav.setPadding(0, BarUtils.getStatusBarHeight(), 0, 0);

        binding.activityMinePursePwdManagerSetPwd.setOnClickListener(this);
        binding.activityMinePursePwdManagerModifyPwd.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if (v == binding.activityMinePursePwdManagerNav.addCloseImageButton()) {
            finish();
        } else if (v == binding.activityMinePursePwdManagerSetPwd) {
            HashMap map = new HashMap();
            map.put("type", "0");
            PursePwdManagerSetActivity.start(PursePwdManagerSetActivity.class, this, map);
        } else if (v == binding.activityMinePursePwdManagerModifyPwd) {
            HashMap map = new HashMap();
            map.put("type", "1");
            PursePwdManagerSetActivity.start(PursePwdManagerSetActivity.class, this, map);
        }
    }
}
