package com.turunsi.yaoxin.main.mine.purse.pwdmanager;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.Nullable;

import com.yaoxin.appbase.activity.BaseActivity;
import com.turunsi.yaoxin.databinding.ActivityMinePursePwdManagerBinding;

import java.util.HashMap;

public class PursePwdManagerActivity extends BaseActivity implements View.OnClickListener {
    ActivityMinePursePwdManagerBinding binding;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMinePursePwdManagerBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        binding.activityMinePursePwdManagerNav.addCloseImageButton().setOnClickListener(this);

        _initCell();
    }
    private void _initCell() {
        binding.activityMinePursePwdManagerSetPwd.viewTitleArrowTv.setText("设置支付密码");
        binding.activityMinePursePwdManagerModifyPwd.viewTitleArrowTv.setText("修改支付密码");
        binding.activityMinePursePwdManagerSetPwd.viewTitleArrowLl.setOnClickListener(this);
        binding.activityMinePursePwdManagerModifyPwd.viewTitleArrowLl.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if (v == binding.activityMinePursePwdManagerNav.addCloseImageButton()) {
            finish();
        } else if (v == binding.activityMinePursePwdManagerSetPwd.viewTitleArrowLl) {
            HashMap map = new HashMap();
            map.put("type","0");
            PursePwdManagerSetActivity.start(PursePwdManagerSetActivity.class,this,map);
        } else if (v == binding.activityMinePursePwdManagerModifyPwd.viewTitleArrowLl) {
            HashMap map = new HashMap();
            map.put("type","1");
            PursePwdManagerSetActivity.start(PursePwdManagerSetActivity.class,this,map);

        }
    }

}
