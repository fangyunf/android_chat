package com.turunsi.yaoxin.main.mine.purse.recharge;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.InputType;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.method.DigitsKeyListener;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;

import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.turunsi.yaoxin.R;
import com.turunsi.yaoxin.databinding.ActivityUsdtRechargeBinding;
import com.yaoxin.appbase.activity.BaseActivity;
import com.yaoxin.appbase.model.NetData;
import com.yaoxin.appbase.model.RequestParamsBean;
import com.yaoxin.appbase.model.UserBean;
import com.yaoxin.appbase.net.CommonCallback;
import com.yaoxin.appbase.net.Constant;
import com.yaoxin.appbase.net.HttpUtil;
import com.yaoxin.appbase.utils.CommonCallBack;
import com.yaoxin.appbase.utils.GlideUtil;
import com.yaoxin.appbase.utils.NumberUtil;
import com.yaoxin.appbase.utils.StatusBarUtils;
import com.yaoxin.appbase.utils.ToastUtils;
import com.yaoxin.appbase.utils.UploadUtil;
import com.zhihu.matisse.Matisse;

import java.io.OutputStream;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import retrofit2.Call;
import retrofit2.Response;

/**
 * USDT 充值页，布局与交互对齐 iOS：充币网络 / 今日汇率 / 收款二维码与地址、
 * 充值金额与 USDT 数量联动、区块链交易 ID（粘贴）、凭证上传、提交。
 * 收款地址等来自 {@code /pay/qwe}；提交订单走 {@code /pay/usdtPay}。接口 {@code id} 取用户输入的「区块链交易 ID」；汇率固定 1 USDT = 7 元人民币（与产品约定，不参与接口换算）。
 */
public class UsdtRechargeActivity extends BaseActivity implements View.OnClickListener {

    /**
     * 从 {@link PurseRechargeActivity} 档位传入的人民币金额（元），如 "100"
     */
    public static final String EXTRA_SELECTED_CNY_YUAN = "selected_cny_yuan";

    /**
     * 固定汇率：1 USDT 折合多少元人民币（充值金额换算用）
     */
    private static final double FIXED_RATE_CNY_PER_USDT = 7.0;
    private static final String FIXED_RATE_LABEL = "1USDT ≈ 7.00 元";

    private ActivityUsdtRechargeBinding binding;
    private boolean suppressAmountSync;
    private String voucherUrl = "";
    /**
     * {@code /pay/qwe} 返回的 {@code busUsdtImg}，用于展示与长按保存
     */
    private String previewQrImageUrl = "";
    private int resumeCount;
    private ImageButton navCloseButton;

    private final Handler previewHandler = new Handler(Looper.getMainLooper());
    private final Runnable previewReloadRunnable = new Runnable() {
        @Override
        public void run() {
            requestUsdtPayPreview(readPayIdFromInput());
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityUsdtRechargeBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        StatusBarUtils.transtStatusBar(this, binding.activityUsdtRechargeNav);
        navCloseButton = binding.activityUsdtRechargeNav.addCloseImageButton();
        navCloseButton.setOnClickListener(this);
        binding.activityUsdtRechargeSubmitTv.setOnClickListener(this);
        binding.activityUsdtRechargeCopyAddressTv.setOnClickListener(this);
        binding.activityUsdtRechargePasteTv.setOnClickListener(this);
        binding.activityUsdtRechargeUploadFl.setOnClickListener(this);

        binding.activityUsdtRechargeRatePillTv.setText(FIXED_RATE_LABEL);

        setupDecimalInput(binding.activityUsdtRechargeCnyEt);
        setupDecimalInput(binding.activityUsdtRechargeUsdtEt);

        binding.activityUsdtRechargeCnyEt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (suppressAmountSync) {
                    return;
                }
                syncCnyToUsdt();
                schedulePayPreviewReload();
            }
        });
        binding.activityUsdtRechargeUsdtEt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (suppressAmountSync) {
                    return;
                }
                syncUsdtToCny();
            }
        });

        String initialCnyYuan = "100";
        if (extras != null) {
            String fromRecharge = extras.getString(EXTRA_SELECTED_CNY_YUAN);
            if (!TextUtils.isEmpty(fromRecharge) && parsePositiveDouble(fromRecharge.trim()) > 0) {
                initialCnyYuan = fromRecharge.trim();
            }
        }
        binding.activityUsdtRechargeCnyEt.setText(initialCnyYuan);
        binding.activityUsdtRechargeCnyEt.setSelection(binding.activityUsdtRechargeCnyEt.getText().length());

        binding.activityUsdtRechargeTxidEt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                schedulePayPreviewReload();
            }
        });

        binding.activityUsdtRechargeQrIv.setOnLongClickListener(v -> {
            saveQrToGallery();
            return true;
        });

        loadPayPreview();
    }

    @Override
    protected void onResume() {
        super.onResume();
        resumeCount++;
        if (resumeCount > 1) {
            loadPayPreview();
        }
    }

    @Override
    protected void onDestroy() {
        previewHandler.removeCallbacks(previewReloadRunnable);
        super.onDestroy();
    }

    private void setupDecimalInput(EditText et) {
        et.setInputType(InputType.TYPE_NUMBER_FLAG_DECIMAL);
        et.setKeyListener(DigitsKeyListener.getInstance("0123456789."));
        et.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                String input = s.toString();
                int dotCount = input.length() - input.replace(".", "").length();
                if (dotCount > 1) {
                    int firstDotIndex = input.indexOf(".");
                    StringBuilder cleanedInput = new StringBuilder(input.substring(0, firstDotIndex + 1));
                    for (int i = firstDotIndex + 1; i < input.length(); i++) {
                        if (input.charAt(i) != '.') {
                            cleanedInput.append(input.charAt(i));
                        }
                    }
                    et.setText(cleanedInput.toString());
                    et.setSelection(cleanedInput.length());
                }
            }
        });
    }

    /** {@code /pay/qwe}、{@code /pay/usdtPay} 的 id 均取自用户输入（区块链交易 ID 框）。 */
    private String readPayIdFromInput() {
        return getTextStr(binding.activityUsdtRechargeTxidEt).trim();
    }

    private void schedulePayPreviewReload() {
        previewHandler.removeCallbacks(previewReloadRunnable);
        previewHandler.postDelayed(previewReloadRunnable, 450);
    }

    /**
     * 拉取页面展示用收款信息 {@code /pay/qwe}：amount（分）、id（输入框）。
     */
    private void loadPayPreview() {
        previewHandler.removeCallbacks(previewReloadRunnable);
        requestUsdtPayPreview(readPayIdFromInput());
    }

    private void requestUsdtPayPreview(String payId) {
        String cny = getTextStr(binding.activityUsdtRechargeCnyEt).trim();
        if (cny.isEmpty()) {
            cny = "100";
        }
        RequestParamsBean payBean = new RequestParamsBean();
        payBean.amount = NumberUtil.formartUploadMoney(cny);
        payBean.id = payId;
        HttpUtil.apiW().pay_qwe(payBean)
                .enqueue(new CommonCallback<NetData>() {
                    @Override
                    public void Successful(Call<NetData> call, Response<NetData> response, NetData body1) {
                        if (body1 == null || body1.data == null) {
                            return;
                        }
                        JsonObject payload = new Gson().fromJson(body1.data.toString(), JsonObject.class);
                        if (payload != null) {
                            applyPayPayload(payload);
                        }
                    }

                    @Override
                    public void Failure(Call<NetData> call, Throwable t) {
                    }
                });
    }

    private void applyPayPayload(JsonObject payload) {
        String busUsdt = UsdtPayResultDialog.fieldAsString(payload, "busUsdt");
        String busUsdtImg = UsdtPayResultDialog.fieldAsString(payload, "busUsdtImg");
        if ("--".equals(busUsdt)) {
            busUsdt = "";
        }
        if ("--".equals(busUsdtImg)) {
            busUsdtImg = "";
        }
        binding.activityUsdtRechargeRatePillTv.setText(FIXED_RATE_LABEL);
        binding.activityUsdtRechargeAddressTv.setText(busUsdt.isEmpty() ? "--" : busUsdt);
        boolean hasAddr = !busUsdt.isEmpty();
        binding.activityUsdtRechargeCopyAddressTv.setEnabled(hasAddr);
        binding.activityUsdtRechargeCopyAddressTv.setAlpha(hasAddr ? 1f : 0.45f);
        previewQrImageUrl = "";
        if (!TextUtils.isEmpty(busUsdtImg)) {
            previewQrImageUrl = busUsdtImg;
            GlideUtil.loadImage(this, binding.activityUsdtRechargeQrIv, busUsdtImg, 0, 0);
        } else {
            binding.activityUsdtRechargeQrIv.setImageDrawable(null);
        }

        suppressAmountSync = true;
        syncCnyToUsdt();
        suppressAmountSync = false;
    }

    private static double parsePositiveDouble(String s) {
        if (s == null || s.isEmpty()) {
            return 0;
        }
        try {
            return Double.parseDouble(s.trim());
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private void syncCnyToUsdt() {
        String cnyStr = getTextStr(binding.activityUsdtRechargeCnyEt).trim();
        double cny = parsePositiveDouble(cnyStr);
        double usdt = cny / FIXED_RATE_CNY_PER_USDT;
        suppressAmountSync = true;
        binding.activityUsdtRechargeUsdtEt.setText(formatMoney(usdt));
        binding.activityUsdtRechargeUsdtEt.setSelection(binding.activityUsdtRechargeUsdtEt.getText().length());
        suppressAmountSync = false;
    }

    private void syncUsdtToCny() {
        String usdtStr = getTextStr(binding.activityUsdtRechargeUsdtEt).trim();
        double usdt = parsePositiveDouble(usdtStr);
        double cny = usdt * FIXED_RATE_CNY_PER_USDT;
        suppressAmountSync = true;
        binding.activityUsdtRechargeCnyEt.setText(formatMoney(cny));
        binding.activityUsdtRechargeCnyEt.setSelection(binding.activityUsdtRechargeCnyEt.getText().length());
        suppressAmountSync = false;
    }

    private static String formatMoney(double v) {
        return String.format(Locale.US, "%.2f", v);
    }

    private void saveQrToGallery() {
        if (TextUtils.isEmpty(previewQrImageUrl)) {
            ToastUtils.toastMsg("暂无二维码");
            return;
        }
        final String url = previewQrImageUrl;
        new Thread(() -> {
            Bitmap bmp = null;
            try {
                bmp = Glide.with(getApplicationContext())
                        .asBitmap()
                        .load(url)
                        .submit()
                        .get(30, TimeUnit.SECONDS);
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (bmp == null || bmp.isRecycled()) {
                runOnUiThread(() -> ToastUtils.toastMsg("保存失败"));
                return;
            }
            final Bitmap toSave = bmp;
            String fileName = "usdt_charge_" + System.currentTimeMillis() + ".png";
            ContentValues values = new ContentValues();
            values.put(MediaStore.Images.Media.DISPLAY_NAME, fileName);
            values.put(MediaStore.Images.Media.MIME_TYPE, "image/png");
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                values.put(MediaStore.Images.Media.RELATIVE_PATH, android.os.Environment.DIRECTORY_PICTURES);
            }
            Uri uri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
            if (uri == null) {
                runOnUiThread(() -> ToastUtils.toastMsg("保存失败"));
                return;
            }
            try (OutputStream out = getContentResolver().openOutputStream(uri)) {
                if (out == null) {
                    runOnUiThread(() -> ToastUtils.toastMsg("保存失败"));
                    return;
                }
                toSave.compress(Bitmap.CompressFormat.PNG, 100, out);
                runOnUiThread(() -> ToastUtils.toastMsg("已保存到相册"));
            } catch (Exception e) {
                runOnUiThread(() -> ToastUtils.toastMsg("保存失败"));
            }
        }).start();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == Constant.REQUEST_CODE_CHOOSE && resultCode == RESULT_OK && data != null) {
            List<String> strings = Matisse.obtainPathResult(data);
            if (strings == null || strings.isEmpty()) {
                return;
            }
            UploadUtil.uploadImage(strings.get(0), "", new CommonCallBack() {
                @Override
                public void onCallBackUserBean(UserBean userBean) {
                    if (userBean == null || TextUtils.isEmpty(userBean.url)) {
                        ToastUtils.toastMsg("上传失败");
                        return;
                    }
                    voucherUrl = userBean.url;
                    UsdtRechargeActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            ToastUtils.toastMsg("上传成功");
                            binding.activityUsdtRechargeVoucherIv.setVisibility(View.VISIBLE);
                            binding.activityUsdtRechargeUploadPlaceholderLl.setVisibility(View.GONE);
                            GlideUtil.loadImage(UsdtRechargeActivity.this,
                                    binding.activityUsdtRechargeVoucherIv, voucherUrl, 0, 0);
                        }
                    });
                }
            });
        }
    }

    @Override
    public void onClick(View v) {
        if (v == navCloseButton) {
            finish();
        } else if (v == binding.activityUsdtRechargeSubmitTv) {
            submitRecharge();
        } else if (v == binding.activityUsdtRechargeCopyAddressTv) {
            copyDepositAddress();
        } else if (v == binding.activityUsdtRechargePasteTv) {
            pasteTxId();
        } else if (v == binding.activityUsdtRechargeUploadFl) {
            UploadUtil.openPhotoLibrary(this, Constant.REQUEST_CODE_CHOOSE);
        }
    }

    private void copyDepositAddress() {
        String addr = getTextStr(binding.activityUsdtRechargeAddressTv);
        if (TextUtils.isEmpty(addr) || "--".equals(addr)) {
            ToastUtils.toastMsg("暂无地址可复制");
            return;
        }
        ClipboardManager cm = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        if (cm != null) {
            cm.setPrimaryClip(ClipData.newPlainText("usdt", addr));
        }
        ToastUtils.toastMsg("复制成功");
    }

    private void pasteTxId() {
        ClipboardManager cm = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        if (cm == null || !cm.hasPrimaryClip()) {
            ToastUtils.toastMsg("剪贴板为空");
            return;
        }
        ClipData clip = cm.getPrimaryClip();
        if (clip == null || clip.getItemCount() == 0) {
            ToastUtils.toastMsg("剪贴板为空");
            return;
        }
        CharSequence t = clip.getItemAt(0).coerceToText(this);
        if (t == null || t.length() == 0) {
            ToastUtils.toastMsg("剪贴板为空");
            return;
        }
        binding.activityUsdtRechargeTxidEt.setText(t.toString().trim());
        binding.activityUsdtRechargeTxidEt.setSelection(binding.activityUsdtRechargeTxidEt.getText().length());
        ToastUtils.toastMsg("已粘贴");
        schedulePayPreviewReload();
    }

    private void submitRecharge() {
        String cny = getTextStr(binding.activityUsdtRechargeCnyEt).trim();
        if (cny.isEmpty() || parsePositiveDouble(cny) <= 0) {
            ToastUtils.toastMsg("请输入充值金额");
            return;
        }
        String payId = readPayIdFromInput();
        if (payId.isEmpty()) {
            ToastUtils.toastMsg("请输入区块链交易 ID");
            return;
        }
        if (TextUtils.isEmpty(voucherUrl)) {
            ToastUtils.toastMsg("请上传充值凭证");
            return;
        }
        RequestParamsBean payBean = new RequestParamsBean();
        payBean.amount = NumberUtil.formartUploadMoney(cny);
        payBean.id = payId;
        payBean.zfpz = voucherUrl;
        HttpUtil.apiW().pay_usdtPay(payBean)
                .enqueue(new CommonCallback<NetData>() {
                    @Override
                    public void Successful(Call<NetData> call, Response<NetData> response, NetData body) {
                        ToastUtils.toastMsg(
                                body != null && !TextUtils.isEmpty(body.msg) ? body.msg : "提交成功");
                        finish();
                    }

                    @Override
                    public void Failure(Call<NetData> call, Throwable t) {
                    }
                });
    }
}
