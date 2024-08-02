package com.turunsi.yaoxin.main.mine.purse.recharge;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.text.method.DigitsKeyListener;
import android.view.Gravity;
import android.view.View;

import androidx.annotation.Nullable;

import com.google.gson.Gson;
import com.turunsi.yaoxin.R;
import com.turunsi.yaoxin.databinding.ActivityMinePurseRechargeBinding;
import com.turunsi.yaoxin.databinding.ActivityMinePurseUsdtRechargeBinding;
import com.yaoxin.appbase.activity.BaseActivity;
import com.yaoxin.appbase.model.NetData;
import com.yaoxin.appbase.model.RequestParamsBean;
import com.yaoxin.appbase.model.UserBean;
import com.yaoxin.appbase.net.CommonCallback;
import com.yaoxin.appbase.net.HttpUtil;
import com.yaoxin.appbase.utils.DialogAlertUtil;
import com.yaoxin.appbase.utils.NumberUtil;
import com.yaoxin.appbase.utils.ToastUtils;
import com.yaoxin.appbase.view.LoadingDialog;

import retrofit2.Call;
import retrofit2.Response;

public class PurseUSDTRechargeActivity extends BaseActivity implements View.OnClickListener {
    ActivityMinePurseUsdtRechargeBinding binding;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMinePurseUsdtRechargeBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        binding.activityMinePurseUsdtRechargeNav.addCloseImageButton().setOnClickListener(this);
        binding.activityMinePurseUsdtRechargeCopyTv.setOnClickListener(this);

        _initCell();
    }
    private void _initCell() {

    }
    @Override
    public void onClick(View v) {
        if (v == binding.activityMinePurseUsdtRechargeNav.addCloseImageButton()) {
            finish();
        } else if (v == binding.activityMinePurseUsdtRechargeCopyTv) {
            // 获取文本内容
            String textToCopy = binding.activityMinePurseUsdtRechargeCopyContentTv.getText().toString();

            // 获取剪切板管理器
            ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);

            // 创建ClipData对象并将文本复制到剪切板
            ClipData clip = ClipData.newPlainText("label", textToCopy);
            clipboard.setPrimaryClip(clip);

            // 提示用户内容已复制
            ToastUtils.toastMsg("已复制到剪切板");
        }

    }

}
