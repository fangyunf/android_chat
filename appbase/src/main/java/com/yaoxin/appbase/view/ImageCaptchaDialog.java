package com.yaoxin.appbase.view;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentManager;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.signature.ObjectKey;
import com.google.android.material.button.MaterialButton;
import com.yaoxin.appbase.R;
import com.yaoxin.appbase.utils.CommonNetUtil;
import com.yaoxin.appbase.utils.ToastUtils;
import com.yaoxin.appbase.view.loginlib.view.CountDownView;

import java.util.Objects;

/**
 * 短信接口要求图形验证（type=1）时展示；填写 validate 后再次请求 /customer/smsCode。
 * 服务端返回 704 时 data 为新图地址，仅刷新图片。
 */
public class ImageCaptchaDialog extends DialogFragment {

    private static final String ARG_PHONE = "phone";
    private static final String ARG_IMAGE = "imageUrl";

    private CountDownView countDownView;
    private String phone;
    private String imageUrl;
    private boolean finishedOk;
    private ImageView imageView;
    private EditText editText;

    public static void show(
            FragmentManager fm, String phone, String imageUrl, CountDownView countDownView) {
        ImageCaptchaDialog dialog = new ImageCaptchaDialog();
        dialog.countDownView = countDownView;
        Bundle b = new Bundle();
        b.putString(ARG_PHONE, phone);
        b.putString(ARG_IMAGE, imageUrl);
        dialog.setArguments(b);
        dialog.show(fm, "ImageCaptchaDialog");
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NO_FRAME, R.style.custom_dlg);
    }

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.dialog_image_captcha, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Bundle args = getArguments();
        if (args != null) {
            phone = args.getString(ARG_PHONE);
            imageUrl = args.getString(ARG_IMAGE);
        }
        Objects.requireNonNull(getDialog()).setCanceledOnTouchOutside(true);

        imageView = view.findViewById(R.id.dialog_image_captcha_iv);
        editText = view.findViewById(R.id.dialog_image_captcha_et);
        MaterialButton ok = view.findViewById(R.id.dialog_image_captcha_ok);
        TextView cancel = view.findViewById(R.id.dialog_image_captcha_cancel);

        loadImage(CommonNetUtil.unwrapGraphicCaptchaUrl(imageUrl));

        cancel.setOnClickListener(v -> dismissAllowingStateLoss());
        ok.setOnClickListener(v -> submit());
    }

    private void loadImage(String url) {
        if (imageView == null || TextUtils.isEmpty(url)) {
            return;
        }
        float cornerDp = 14f;
        int cornersPx =
                (int)
                        TypedValue.applyDimension(
                                TypedValue.COMPLEX_UNIT_DIP,
                                cornerDp,
                                getResources().getDisplayMetrics());
        RequestOptions opts =
                RequestOptions.bitmapTransform(new RoundedCorners(cornersPx))
                        .skipMemoryCache(true)
                        .diskCacheStrategy(DiskCacheStrategy.NONE)
                        // 服务端常固定同一路径换图：必须断开 Glide 缓存，否则会一直显示旧图
                        .signature(new ObjectKey(url + '|' + System.nanoTime()));
        Glide.with(this).load(url).apply(opts).into(imageView);
    }

    private void submit() {
        if (getActivity() == null) {
            return;
        }
        String code = editText.getText().toString().trim();
        if (code.isEmpty()) {
            ToastUtils.toastMsg("请输入图形验证码");
            return;
        }
        CommonNetUtil.submitSmsGraphicCaptcha(
                getActivity(),
                phone,
                code,
                countDownView,
                new CommonNetUtil.GraphicCaptchaContinuation() {
                    @Override
                    public void onSmsOk() {
                        finishedOk = true;
                        dismissAllowingStateLoss();
                    }

                    @Override
                    public void onReplaceCaptchaImage(@Nullable String newImageUrl) {
                        String resolved = CommonNetUtil.unwrapGraphicCaptchaUrl(newImageUrl);
                        if (!TextUtils.isEmpty(resolved)) {
                            imageUrl = resolved;
                            loadImage(resolved);
                        } else if (imageView != null && ImageCaptchaDialog.this.isAdded()) {
                            Glide.with(ImageCaptchaDialog.this).clear(imageView);
                        }
                        if (editText != null) {
                            editText.setText("");
                        }
                    }
                });
    }

    @Override
    public void onDestroyView() {
        imageView = null;
        editText = null;
        super.onDestroyView();
    }

    @Override
    public void onDismiss(@NonNull android.content.DialogInterface dialog) {
        super.onDismiss(dialog);
        if (!finishedOk && countDownView != null) {
            countDownView.notifySmsRequestFailed();
        }
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Window window = Objects.requireNonNull(getDialog()).getWindow();
        if (window != null) {
            window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            WindowManager.LayoutParams lp = window.getAttributes();
            lp.width = (int) (getResources().getDisplayMetrics().widthPixels * 0.86f);
            lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
            lp.dimAmount = 0.45f;
            lp.gravity = Gravity.CENTER;
            window.setAttributes(lp);
        }
    }
}
