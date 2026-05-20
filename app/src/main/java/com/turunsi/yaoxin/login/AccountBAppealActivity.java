package com.turunsi.yaoxin.login;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;
import com.netease.yunxin.kit.common.ui.photo.BasePhotoChoiceDialog;
import com.netease.yunxin.kit.teamkit.ui.fun.dialog.FunPhotoChoiceDialog;
import com.turunsi.yaoxin.R;
import com.turunsi.yaoxin.databinding.ActivityAccountBAppealBinding;
import com.yaoxin.appbase.activity.BaseActivity;
import com.yaoxin.appbase.utils.StatusBarUtils;

import java.io.File;

public class AccountBAppealActivity extends BaseActivity {
    ActivityAccountBAppealBinding binding;
    // 用于存储图片路径或Uri
    private String idFrontPath = null;
    private String idBackPath = null;
    private String idHoldFrontPath = null;
    private String idHoldBackPath = null;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAccountBAppealBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        binding.layoutTitleBar.addCloseImageButton().setOnClickListener(view -> finish());
        StatusBarUtils.transtStatusBar(this, binding.layoutTitleBar);
        // 图片上传点击事件
        binding.ivIdFront.setOnClickListener(v -> selectImage(0));
        binding.ivIdBack.setOnClickListener(v -> selectImage(1));
        binding.ivIdHoldFront.setOnClickListener(v -> selectImage(2));
        binding.ivIdHoldBack.setOnClickListener(v -> selectImage(3));
        // 提交申诉按钮
        binding.btnSubmit.setOnClickListener(v -> submitAppeal());
        // 申诉进度点击（可选）
        binding.tvProgress.setOnClickListener(v -> showAppealProgress());
    }

    // 选择图片（0:正面 1:反面 2:手持正面 3:手持反面）
    private void selectImage(int type) {
        BasePhotoChoiceDialog choiceDialog;
        choiceDialog = new FunPhotoChoiceDialog(this);
        choiceDialog.show(new com.netease.yunxin.kit.common.ui.utils.CommonCallback<File>() {
            @Override
            public void onSuccess(@Nullable File param) {
                AccountBAppealActivity.this.onImageSelected(type, param.getAbsolutePath());
            }

            @Override
            public void onFailed(int code) {
                Toast.makeText(getApplicationContext(), getString(R.string.request_fail), Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onException(@Nullable Throwable exception) {
                Toast.makeText(getApplicationContext(), getString(R.string.request_fail), Toast.LENGTH_SHORT).show();
            }
        });
    }

    // 图片选择回调（实际开发中应由图片选择库回调）
    private void onImageSelected(int type, String path) {
        switch (type) {
            case 0:
                idFrontPath = path;
                setImage(binding.ivIdFront, path);
                break;
            case 1:
                idBackPath = path;
                setImage(binding.ivIdBack, path);
                break;
            case 2:
                idHoldFrontPath = path;
                setImage(binding.ivIdHoldFront, path);
                break;
            case 3:
                idHoldBackPath = path;
                setImage(binding.ivIdHoldBack, path);
                break;
        }
    }

    private void setImage(ImageView imageView, String path) {
        Glide.with(this).load(new File(path)).into(imageView);
    }

    // 提交申诉
    private void submitAppeal() {
        String phone = binding.etPhone.getText().toString().trim();
        String name = binding.etRealName.getText().toString().trim();
        String idCard = binding.etIdCard.getText().toString().trim();

        if (TextUtils.isEmpty(phone)) {
            showToast("请输入申诉手机号");
            return;
        }
        if (!Patterns.PHONE.matcher(phone).matches() || phone.length() < 6) {
            showToast("请输入正确的手机号");
            return;
        }
        if (TextUtils.isEmpty(name)) {
            showToast("请输入真实姓名");
            return;
        }
        if (TextUtils.isEmpty(idCard) || idCard.length() < 15) {
            showToast("请输入正确的身份证号码");
            return;
        }
        if (idFrontPath == null) {
            showToast("请上传身份证正面");
            return;
        }
        if (idBackPath == null) {
            showToast("请上传身份证反面");
            return;
        }
        if (idHoldFrontPath == null) {
            showToast("请上传手持身份证正面");
            return;
        }
        if (idHoldBackPath == null) {
            showToast("请上传手持身份证反面");
            return;
        }
        // TODO: 上传数据到服务器
        showToast("申诉提交中...");
        // 示例：uploadAppeal(phone, name, idCard, idFrontPath, idBackPath, idHoldFrontPath, idHoldBackPath);
    }

    // 申诉进度点击（可选实现）
    private void showAppealProgress() {
        // TODO: 跳转进度页面或弹窗
        showToast("申诉进度功能待实现");
    }

    private void showToast(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }
}
