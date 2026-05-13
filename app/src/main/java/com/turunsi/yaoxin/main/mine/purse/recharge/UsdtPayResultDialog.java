package com.turunsi.yaoxin.main.mine.purse.recharge;

import android.app.Activity;
import android.app.Dialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.turunsi.yaoxin.R;
import com.yaoxin.appbase.utils.ToastUtils;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

/**
 * USDT 充值下单成功后的支付信息弹层，对应归档 {@code SWUsdtPayResultAlertView}。
 */
public final class UsdtPayResultDialog {

    private UsdtPayResultDialog() {}

    public static String
    fieldAsString(@Nullable JsonObject o, String key) {
        if (o == null || !o.has(key) || o.get(key).isJsonNull()) {
            return "--";
        }
        JsonElement el = o.get(key);
        if (el.isJsonPrimitive()) {
            return el.getAsString();
        }
        return "--";
    }

    /**
     * @return 对话框实例，供调用方在 {@link Activity#onDestroy()} 等处 dismiss。
     */
    public static Dialog show(Activity activity, JsonObject payload, Runnable onDismiss) {
        if (activity == null || activity.isFinishing()) {
            return null;
        }
        Dialog dialog = new Dialog(activity, android.R.style.Theme_Translucent_NoTitleBar_Fullscreen);
        View root = LayoutInflater.from(activity).inflate(R.layout.dialog_usdt_pay_result, null);
        dialog.setContentView(root);
        Window w = dialog.getWindow();
        if (w != null) {
            w.setLayout(android.view.ViewGroup.LayoutParams.MATCH_PARENT,
                    android.view.ViewGroup.LayoutParams.MATCH_PARENT);
        }

        String yin = fieldAsString(payload, "yin");
        String xy = fieldAsString(payload, "xy");
        String huiLv = fieldAsString(payload, "huiLv");
        String busUsdt = fieldAsString(payload, "busUsdt");
        if ("--".equals(busUsdt)) {
            busUsdt = "";
        }

        TextView yinTv = root.findViewById(R.id.usdt_pay_yin_tv);
        TextView xyTv = root.findViewById(R.id.usdt_pay_xy_tv);
        TextView huiLvTv = root.findViewById(R.id.usdt_pay_huilv_tv);
        TextView addrTv = root.findViewById(R.id.usdt_pay_address_tv);
        ImageView qrIv = root.findViewById(R.id.usdt_pay_qr_iv);
        TextView copyTv = root.findViewById(R.id.usdt_pay_copy_tv);
        TextView okTv = root.findViewById(R.id.usdt_pay_ok_tv);
        TextView closeTv = root.findViewById(R.id.usdt_pay_close_tv);
        View overlay = root.findViewById(R.id.usdt_pay_overlay);
        View panel = root.findViewById(R.id.usdt_pay_panel);

        yinTv.setText(yin);
        xyTv.setText(xy);
        huiLvTv.setText(huiLv);
        addrTv.setText(busUsdt.isEmpty() ? "--" : busUsdt);

        if (!busUsdt.isEmpty()) {
            Bitmap bmp = generateQrBitmap(busUsdt, 200);
            if (bmp != null) {
                qrIv.setImageBitmap(bmp);
                qrIv.setVisibility(View.VISIBLE);
            } else {
                qrIv.setVisibility(View.GONE);
            }
        } else {
            qrIv.setVisibility(View.GONE);
        }

        copyTv.setEnabled(!busUsdt.isEmpty());
        copyTv.setAlpha(busUsdt.isEmpty() ? 0.45f : 1f);

        String finalBusUsdt = busUsdt;
        copyTv.setOnClickListener(v -> {
            if (finalBusUsdt.isEmpty()) {
                ToastUtils.toastMsg("暂无地址可复制");
                return;
            }
            ClipboardManager cm = (ClipboardManager) activity.getSystemService(Context.CLIPBOARD_SERVICE);
            if (cm != null) {
                cm.setPrimaryClip(ClipData.newPlainText("usdt", finalBusUsdt));
            }
            ToastUtils.toastMsg("复制成功");
        });

        okTv.setOnClickListener(v -> dialog.dismiss());
        closeTv.setOnClickListener(v -> dialog.dismiss());

        overlay.setOnClickListener(v -> {
            if (v == overlay) {
                dialog.dismiss();
            }
        });
        panel.setOnClickListener(v -> { /* Consume */ });

        dialog.setOnDismissListener(d -> {
            if (onDismiss != null) {
                onDismiss.run();
            }
        });
        dialog.show();
        return dialog;
    }

    /** 与 USDT 充值页等处共用 */
    @Nullable
    public static Bitmap generateQrBitmap(String text, int size) {
        QRCodeWriter writer = new QRCodeWriter();
        try {
            BitMatrix bitMatrix = writer.encode(text, BarcodeFormat.QR_CODE, size, size);
            Bitmap bmp = Bitmap.createBitmap(size, size, Bitmap.Config.RGB_565);
            for (int x = 0; x < size; x++) {
                for (int y = 0; y < size; y++) {
                    bmp.setPixel(x, y, bitMatrix.get(x, y) ? 0xFF000000 : 0xFFFFFFFF);
                }
            }
            return bmp;
        } catch (WriterException e) {
            return null;
        }
    }
}
