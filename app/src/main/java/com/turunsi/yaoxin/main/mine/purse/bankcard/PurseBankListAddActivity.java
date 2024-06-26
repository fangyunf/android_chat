package com.turunsi.yaoxin.main.mine.purse.bankcard;

import android.os.Bundle;
import android.text.InputType;
import android.view.View;

import androidx.annotation.Nullable;

import com.turunsi.yaoxin.R;
import com.yaoxin.appbase.activity.BaseActivity;
import com.turunsi.yaoxin.databinding.ActivityMinePurseBankListAddBinding;

public class PurseBankListAddActivity extends BaseActivity implements View.OnClickListener {
    ActivityMinePurseBankListAddBinding binding;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMinePurseBankListAddBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        binding.activityMinePurseBankCardListAddNav.addCloseImageButton().setOnClickListener(this);
        binding.activityMinePurseBankCardListAddNextRl.setOnClickListener(this);

        _initSetCell();
    }
    private void _initSetCell() {
        binding.activityMinePurseBankCardListAddCardHolder.viewTitleTfWithoutBgTv.setText("持卡人");
        binding.activityMinePurseBankCardListAddIdentityId.viewTitleTfWithoutBgTv.setText("身份证");
        binding.activityMinePurseBankCardListAddBankCardNumber.viewTitleTfWithoutBgTv.setText("银行卡号");
        binding.activityMinePurseBankCardListAddReservePhoneNumber.viewTitleTfWithoutBgTv.setText("预留手机号");
        binding.activityMinePurseBankCardListAddBankCity.viewTitleTfWithoutBgTv.setText("银行卡省市");
        binding.activityMinePurseBankCardListAddVerifyCode.viewTitleTfWithoutBgTv.setText("验证码");

        binding.activityMinePurseBankCardListAddCardHolder.viewTitleTfWithoutBgEt.setHint("请输入持卡人");
        binding.activityMinePurseBankCardListAddIdentityId.viewTitleTfWithoutBgEt.setHint("请输入身份证");
        binding.activityMinePurseBankCardListAddBankCardNumber.viewTitleTfWithoutBgEt.setHint("请输入银行卡号");
        binding.activityMinePurseBankCardListAddReservePhoneNumber.viewTitleTfWithoutBgEt.setHint("请输入预留手机号");
        binding.activityMinePurseBankCardListAddBankCity.viewTitleTfWithoutBgEt.setHint("请选择银行卡省市");
        binding.activityMinePurseBankCardListAddVerifyCode.viewTitleTfWithoutBgEt.setHint("请输入验证码");

        binding.activityMinePurseBankCardListAddBankCity.viewTitleTfWithoutBgEt.setEnabled(false);

        binding.activityMinePurseBankCardListAddIdentityId.viewTitleTfWithoutBgEt.setInputType(InputType.TYPE_CLASS_NUMBER);
        binding.activityMinePurseBankCardListAddBankCardNumber.viewTitleTfWithoutBgEt.setInputType(InputType.TYPE_CLASS_NUMBER);
        binding.activityMinePurseBankCardListAddReservePhoneNumber.viewTitleTfWithoutBgEt.setInputType(InputType.TYPE_CLASS_NUMBER);
        binding.activityMinePurseBankCardListAddVerifyCode.viewTitleTfWithoutBgEt.setInputType(InputType.TYPE_CLASS_NUMBER);

        binding.activityMinePurseBankCardListAddCardHolder.viewTitleTfWithoutBgEt.setBackgroundColor(getResources().getColor(R.color.color_white));
        binding.activityMinePurseBankCardListAddCardHolder.viewTitleTfWithoutBgLl.setBackgroundColor(getResources().getColor(R.color.color_white));

        binding.activityMinePurseBankCardListAddIdentityId.viewTitleTfWithoutBgEt.setBackgroundColor(getResources().getColor(R.color.color_white));
        binding.activityMinePurseBankCardListAddIdentityId.viewTitleTfWithoutBgLl.setBackgroundColor(getResources().getColor(R.color.color_white));

        binding.activityMinePurseBankCardListAddBankCardNumber.viewTitleTfWithoutBgEt.setBackgroundColor(getResources().getColor(R.color.color_white));
        binding.activityMinePurseBankCardListAddBankCardNumber.viewTitleTfWithoutBgLl.setBackgroundColor(getResources().getColor(R.color.color_white));

        binding.activityMinePurseBankCardListAddReservePhoneNumber.viewTitleTfWithoutBgEt.setBackgroundColor(getResources().getColor(R.color.color_white));
        binding.activityMinePurseBankCardListAddReservePhoneNumber.viewTitleTfWithoutBgLl.setBackgroundColor(getResources().getColor(R.color.color_white));

        binding.activityMinePurseBankCardListAddBankCity.viewTitleTfWithoutBgEt.setBackgroundColor(getResources().getColor(R.color.color_white));
        binding.activityMinePurseBankCardListAddBankCity.viewTitleTfWithoutBgLl.setBackgroundColor(getResources().getColor(R.color.color_white));

        binding.activityMinePurseBankCardListAddVerifyCode.viewTitleTfWithoutBgEt.setBackgroundColor(getResources().getColor(R.color.color_white));
        binding.activityMinePurseBankCardListAddVerifyCode.viewTitleTfWithoutBgLl.setBackgroundColor(getResources().getColor(R.color.color_white));
    }
    @Override
    public void onClick(View v) {
        if (v == binding.activityMinePurseBankCardListAddNav.addCloseImageButton()) {
            finish();
        }
    }

}
