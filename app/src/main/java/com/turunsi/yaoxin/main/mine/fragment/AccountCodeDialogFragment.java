package com.turunsi.yaoxin.main.mine.fragment;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentManager;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.turunsi.yaoxin.databinding.DialogAccountCodeBinding;
import com.turunsi.yaoxin.databinding.DialogBugEggSucessBinding;
import com.yaoxin.appbase.fragment.BaseDialogFragment;
import com.yaoxin.appbase.utils.AppProxy;
import com.yaoxin.appbase.utils.DataUtil;
import com.yaoxin.appbase.utils.GlideUtil;
import com.yaoxin.appbase.utils.ImageUtil;

public class AccountCodeDialogFragment extends BaseDialogFragment implements View.OnClickListener {
    DialogAccountCodeBinding binding;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = DialogAccountCodeBinding.inflate(inflater, container, false);
        binding.dialogAccountCodeCloseIv.setOnClickListener(this);
        binding.dialogAccountCodeSavePhoto.setOnClickListener(this);
        binding.dialogAccountCodeNameTv.setText(DataUtil.getUserInfo().userName);

        GlideUtil.yh_loadImageRoundedCorner(getContext(),binding.dialogAccountCodeHeadIv,DataUtil.getUserInfo().avatar,2);
        Bitmap bitmap = generateQRCode(DataUtil.getUserInfo().memberCode);
        if (bitmap != null) {
            binding.dialogAccountCodeCodeIv.setImageBitmap(bitmap);
        }
        return binding.getRoot();
    }
    private Bitmap generateQRCode(String text) {
        QRCodeWriter writer = new QRCodeWriter();
        try {
            int width = 512;
            int height = 512;
            BitMatrix bitMatrix = writer.encode(text, BarcodeFormat.QR_CODE, width, height);
            Bitmap bmp = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
            for (int x = 0; x < width; x++) {
                for (int y = 0; y < height; y++) {
                    bmp.setPixel(x, y, bitMatrix.get(x, y) ? 0xFF000000 : 0xFFFFFFFF);
                }
            }
            return bmp;
        } catch (WriterException e) {
            e.printStackTrace();
        }
        return null;
    }
    public static void showV(FragmentManager fragmentManager) {
        AccountCodeDialogFragment fragment = new AccountCodeDialogFragment();
        fragment.showNow(fragmentManager,"AccountCodeDialogFragment");
    }
    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onClick(View v) {
         if (v == binding.dialogAccountCodeCloseIv) {
            dismiss();
        } else if (binding.dialogAccountCodeSavePhoto == v) {
             ImageUtil.saveImageViewToGallery(getContext(), binding.dialogAccountCodeCodeIv);
         }
    }
}
