package com.turunsi.yaoxin.main.mine.purse.recharge;

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
import com.turunsi.yaoxin.databinding.FragmentChargeScanBinding;
import com.yaoxin.appbase.fragment.BaseDialogFragment;
import com.yaoxin.appbase.utils.DataUtil;
import com.yaoxin.appbase.utils.GlideUtil;

public class RechargeScanFragment extends BaseDialogFragment implements View.OnClickListener {
    FragmentChargeScanBinding binding;
    String _title;
    String _imgUrl;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentChargeScanBinding.inflate(inflater, container, false);

        binding.fragmentChargeScanTv.setText(_title);
//        GlideUtil.yh_loadImageRoundedCorner(getContext(),binding.fragmentChargeScanIv,_imgUrl,100);
        Bitmap bitmap = generateQRCode(_imgUrl);
        if (bitmap != null) {
            binding.fragmentChargeScanIv.setImageBitmap(bitmap);
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


    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onClick(View v) {

    }
    public static void showV(FragmentManager fragmentManager, String title, String imgUrl) {
        RechargeScanFragment fragment = new  RechargeScanFragment();
        fragment._title = title;
        fragment._imgUrl = imgUrl;
        fragment.showNow(fragmentManager,"RechargeScanFragment");
    }
}
