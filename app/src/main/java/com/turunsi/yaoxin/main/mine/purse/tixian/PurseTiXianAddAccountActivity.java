package com.turunsi.yaoxin.main.mine.purse.tixian;


import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.method.DigitsKeyListener;
import android.view.Gravity;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;

import com.bumptech.glide.Glide;
import com.google.gson.Gson;
import com.turunsi.yaoxin.databinding.ActivityMinePurseTixianBinding;
import com.turunsi.yaoxin.databinding.ActivityPurseTixianAddAccountBinding;
import com.yaoxin.appbase.activity.BaseActivity;
import com.yaoxin.appbase.model.NetData;
import com.yaoxin.appbase.model.RegisterBean;
import com.yaoxin.appbase.model.UserBean;
import com.yaoxin.appbase.net.CommonCallback;
import com.yaoxin.appbase.net.Constant;
import com.yaoxin.appbase.net.HttpUtil;
import com.yaoxin.appbase.pswkeyboard.OnPasswordInputFinish;
import com.yaoxin.appbase.pswkeyboard.widget.PopEnterPassword;
import com.yaoxin.appbase.utils.CommonCallBack;
import com.yaoxin.appbase.utils.DialogAlertUtil;
import com.yaoxin.appbase.utils.NumberUtil;
import com.yaoxin.appbase.utils.ToastUtils;
import com.yaoxin.appbase.utils.UploadUtil;
import com.yaoxin.appbase.view.pwdkeyboard.Keyboard;
import com.yaoxin.appbase.view.pwdkeyboard.PayEditText;

import java.util.List;

import pub.devrel.easypermissions.EasyPermissions;
import retrofit2.Call;
import retrofit2.Response;

public class PurseTiXianAddAccountActivity extends BaseActivity implements View.OnClickListener {
    ActivityPurseTixianAddAccountBinding binding;

    String qrcodeImgUrl;

    String inputMoney;
    private static final String[] KEY = new String[] {
            "1", "2", "3",
            "4", "5", "6",
            "7", "8", "9",
            "<<", "0", "完成"
    };

    private PayEditText payEditText;
    private Keyboard keyboard;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (extras.get("inputMoney") != null) {
            inputMoney = (String) extras.get("inputMoney");
        }
        binding = ActivityPurseTixianAddAccountBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        binding.activityMineAddressAddNav.addCloseImageButton().setOnClickListener(this);

        binding.activityPurseTixianAddAccountShiming.viewTitleTfWithoutBgTv.setText("支付宝实名");
        binding.activityPurseTixianAddAccountZhanghao.viewTitleTfWithoutBgTv.setText("支付宝账号");
        binding.activityPurseTixianAddAccountNicheng.viewTitleTfWithoutBgTv.setText("支付宝昵称");

        binding.activityPurseTixianAddAccountShiming.viewTitleTfWithoutBgEt.setHint("请输入支付宝实名");
        binding.activityPurseTixianAddAccountZhanghao.viewTitleTfWithoutBgEt.setHint("请输入支付宝账号");
        binding.activityPurseTixianAddAccountNicheng.viewTitleTfWithoutBgEt.setHint("请输入支付宝昵称");

        binding.activityPurseTixianAddAccountUploadLl.setOnClickListener(this);
        binding.activityPurseTixianAddAccountTixianTv.setOnClickListener(this);

        payEditText = binding.PayEditTextPay;
        keyboard = binding.KeyboardViewPay;
        keyboard.setKeyboardKeys(KEY);
        keyboard.setOnClickKeyboardListener(new Keyboard.OnClickKeyboardListener() {
            @Override
            public void onKeyClick(int position, String value) {
                if (position < 11 && position != 9) {
                    payEditText.add(value);
                } else if (position == 9) {
                    payEditText.remove();
                }else if (position == 11) {
                    binding.activityFunSendRedPacketKeybordRl.setVisibility(View.GONE);

                }
            }
        });

        /**
         * 当密码输入完成时的回调
         */
        payEditText.setOnInputFinishedListener(new PayEditText.OnInputFinishedListener() {
            @Override
            public void onInputFinished(String password) {
                tiXianClick(payEditText.getText());
                payEditText.remove();
                binding.activityFunSendRedPacketKeybordRl.setVisibility(View.GONE);
            }
        });
    }

    @Override
    public void onClick(View v) {
        if (v == binding.activityMineAddressAddNav.addCloseImageButton()) {
            finish();
        } else if (v == binding.activityPurseTixianAddAccountTixianTv) {
            String nicheng = getTextStr(binding.activityPurseTixianAddAccountNicheng.viewTitleTfWithoutBgEt);
            String shiming = getTextStr(binding.activityPurseTixianAddAccountShiming.viewTitleTfWithoutBgEt);
            String zhanghao = getTextStr(binding.activityPurseTixianAddAccountZhanghao.viewTitleTfWithoutBgEt);

//            if (nicheng.isEmpty()) {
//                ToastUtils.toastMsg("请输入昵称");
//                return;
//            }
            if (shiming.isEmpty()) {
                ToastUtils.toastMsg("请输入实名");
                return;
            }
            if (zhanghao.isEmpty()) {
                ToastUtils.toastMsg("请输入账号");
                return;
            }
            if (qrcodeImgUrl == null) {
                ToastUtils.toastMsg("请上传支付宝收款码");
                return;
            }
            bindZFB();




        } else if (v == binding.activityPurseTixianAddAccountUploadLl) {

//            Matisse.from(PurseTiXianAddAccountActivity.this)
//                    .choose(MimeType.ofImage())
//                    .capture(true)
//                    .countable(true)
//                    .maxSelectable(1)
//                    .imageEngine(new GlideEngine()) // 使用 Glide 作为图片加载引擎
//                    .forResult(10086);
            UploadUtil.openPhotoLibrary(this, Constant.REQUEST_CODE_CHOOSE);
//            ActivityCompat.requestPermissions(activity,
//                    new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE},
//                    REQUEST_CAMERA_PERMISSION);
        }
    }

    void tiXianClick(String pwd) {
        String nicheng = getTextStr(binding.activityPurseTixianAddAccountNicheng.viewTitleTfWithoutBgEt);
        String shiming = getTextStr(binding.activityPurseTixianAddAccountShiming.viewTitleTfWithoutBgEt);
        String zhanghao = getTextStr(binding.activityPurseTixianAddAccountZhanghao.viewTitleTfWithoutBgEt);

//        if (nicheng.isEmpty()) {
//            ToastUtils.toastMsg("请输入昵称");
//            return;
//        }
        if (shiming.isEmpty()) {
            ToastUtils.toastMsg("请输入实名");
            return;
        }
        if (zhanghao.isEmpty()) {
            ToastUtils.toastMsg("请输入账号");
            return;
        }
        if (qrcodeImgUrl == null) {
            ToastUtils.toastMsg("请上传支付宝收款码");
            return;
        }

        RegisterBean bean = new RegisterBean();
        bean.payPassword = pwd;
        bean.amount = NumberUtil.formartUploadMoney(inputMoney);
        bean.type = 2;
        bean.zfbNo = zhanghao;
        bean.name = shiming;
        bean.zfbUrl = qrcodeImgUrl;
        HttpUtil.apiW().withdraw_withdrawDeposit(bean)
                .enqueue(new CommonCallback<NetData>() {
                    @Override
                    public void Successful(Call<NetData> call, Response<NetData> response, NetData body) {
                        ToastUtils.toastMsg("提现成功");
                        finish();
                    }

                    @Override
                    public void Failure(Call<NetData> call, Throwable t) {
                        finish();
                    }
                });
    }

    void bindZFB() {

        String shiming = getTextStr(binding.activityPurseTixianAddAccountShiming.viewTitleTfWithoutBgEt);
        String zhanghao = getTextStr(binding.activityPurseTixianAddAccountZhanghao.viewTitleTfWithoutBgEt);

        if (shiming.isEmpty()) {
            ToastUtils.toastMsg("请输入实名");
            return;
        }
        if (zhanghao.isEmpty()) {
            ToastUtils.toastMsg("请输入账号");
            return;
        }
        if (qrcodeImgUrl == null) {
            ToastUtils.toastMsg("请上传支付宝收款码");
            return;
        }
        RegisterBean bean = new RegisterBean();
        bean.phone = zhanghao;
        bean.name = shiming;
        bean.zfb = qrcodeImgUrl;
        Activity that = this;
        HttpUtil.apiW().bindCard_createUptadeZFB(bean)
                .enqueue(new CommonCallback<NetData>() {
                    @Override
                    public void Successful(Call<NetData> call, Response<NetData> response, NetData body) {
                        ToastUtils.toastMsg("绑定成功");
//                        binding.activityFunSendRedPacketKeybordRl.setVisibility(View.VISIBLE);
                        PopEnterPassword popEnterPassword = new PopEnterPassword(that, new OnPasswordInputFinish() {
                            @Override
                            public void inputFinish(String password) {
//                        sendRedWithPwd(password);
                                tiXianClick(password);

                            }

                        },inputMoney);
                        // 显示窗口
                        popEnterPassword.showAtLocation(binding.activityPurseTixianAddAccountRootLl,
                                Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL, 0, 0); // 设置layout在PopupWindow中显示的位置
                    }

                    @Override
                    public void Failure(Call<NetData> call, Throwable t) {
                        finish();
                    }
                });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == Constant.REQUEST_CODE_CHOOSE && resultCode == RESULT_OK) {
            String imagePath = UploadUtil.resolveSelectedImagePath(this, data);
            if (TextUtils.isEmpty(imagePath)) {
                ToastUtils.toastMsg("无法读取图片，请重新选择");
                return;
            }
            UploadUtil.uploadImage(imagePath, "", new CommonCallBack() {
                @Override
                public void onCallBackUserBean(UserBean userBean) {
                    qrcodeImgUrl = userBean.url;
                    ToastUtils.toastMsg("上传成功");
                    Glide.with(PurseTiXianAddAccountActivity.this)
                            .load(imagePath)
                            .into(binding.activityPurseTixianAddAccountUploadIv);
                }
            });
        }
    }

}
