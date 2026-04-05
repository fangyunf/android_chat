package com.turunsi.yaoxin.main.mine;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.RadioGroup;

import androidx.annotation.Nullable;

import com.turunsi.yaoxin.databinding.ActivityComplaintReportBinding;
import com.yaoxin.appbase.activity.BaseActivity;
import com.yaoxin.appbase.utils.ToastUtils;

/**
 * 投诉举报 — 填写信息表单（演示：校验后仅 Toast，未接真实接口）。
 */
public class ComplaintReportActivity extends BaseActivity {

  private ActivityComplaintReportBinding binding;

  @Override
  protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    binding = ActivityComplaintReportBinding.inflate(getLayoutInflater());
    setContentView(binding.getRoot());

    binding.activityComplaintReportNav.addCloseImageButton().setOnClickListener(v -> finish());
    binding.activityComplaintReportSubmitTv.setOnClickListener(v -> trySubmit());
  }

  private void trySubmit() {
    RadioGroup rg = binding.activityComplaintReportTypeRg;
    if (rg.getCheckedRadioButtonId() == -1) {
      ToastUtils.toastMsg("请选择问题类型");
      return;
    }

    String desc = binding.activityComplaintReportDescEt.getText().toString().trim();
    if (desc.length() < 10) {
      ToastUtils.toastMsg("问题描述请至少填写 10 个字");
      return;
    }

    String phone = binding.activityComplaintReportPhoneEt.getText().toString().trim();
    if (!TextUtils.isEmpty(phone) && phone.length() < 11) {
      ToastUtils.toastMsg("请输入正确的手机号码");
      return;
    }

    hideKeyboard(binding.activityComplaintReportDescEt);
    ToastUtils.toastMsg("我们已收到您的反馈，将尽快处理（演示，未实际上传）");
    finish();
  }
}
