package com.turunsi.yaoxin.main.mine.fragment;

import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.TextPaint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentManager;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.Target;

import androidx.annotation.Nullable;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.turunsi.yaoxin.databinding.DialogAccountCodeBinding;
import com.turunsi.yaoxin.databinding.DialogBugEggSucessBinding;
import com.yaoxin.appbase.fragment.BaseDialogFragment;
import com.yaoxin.appbase.model.UserBean;
import com.yaoxin.appbase.utils.AppProxy;
import com.yaoxin.appbase.utils.DataUtil;
import com.yaoxin.appbase.utils.GlideUtil;
import com.yaoxin.appbase.utils.ImageUtil;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class AccountCodeDialogFragment extends BaseDialogFragment implements View.OnClickListener {
    DialogAccountCodeBinding binding;
    private ExecutorService executorService = Executors.newSingleThreadExecutor();
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = DialogAccountCodeBinding.inflate(inflater, container, false);
        binding.dialogAccountCodeCloseIv.setOnClickListener(this);
        binding.dialogAccountCodeSavePhoto.setOnClickListener(this);
        binding.dialogAccountCodeNameTv.setText(DataUtil.getUserInfo().username);
        binding.dialogAccountCodeIdTv.setText( "ID:" +DataUtil.getUserInfo().memberCode);

        GlideUtil.yh_loadImageRoundedCorner(getContext(),binding.dialogAccountCodeHeadIv,DataUtil.getUserInfo().avatar,2);
        Bitmap bitmap = generateQRCode(DataUtil.getUserInfo().memberCode);
        if (bitmap != null) {
            binding.dialogAccountCodeCodeIv.setImageBitmap(bitmap);
        }
        return binding.getRoot();
    }
    private Bitmap generateQRCode(String text) {
        String resultStr = text;
        QRCodeWriter writer = new QRCodeWriter();
        try {
            int width = 512;
            int height = 512;
            BitMatrix bitMatrix = writer.encode(resultStr, BarcodeFormat.QR_CODE, width, height);
            // 生成黑白二维码，使用ARGB_8888确保颜色精度
            Bitmap bmp = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
            for (int x = 0; x < width; x++) {
                for (int y = 0; y < height; y++) {
                    // 使用黑色和白色
                    bmp.setPixel(x, y, bitMatrix.get(x, y) ? 0xFF000000 : 0xFFFFFFFF);
                }
            }
            return bmp;
        } catch (WriterException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 使用Glide异步加载头像，加载成功后在回调中绘制完整图片
     */
    private void loadAvatarAndGenerateImage(String avatarUrl, int avatarSize) {
        // 使用ApplicationContext，避免Fragment生命周期问题
        android.content.Context context = getContext();
        if (context == null) {
            context = getActivity();
        }
        if (context == null) {
            return;
        }
        
        // 如果没有头像URL，使用默认头像
        if (avatarUrl == null || avatarUrl.isEmpty()) {
            Bitmap defaultAvatar = getDefaultAvatarBitmap(context, avatarSize);
            generateCompleteQRCodeImage(defaultAvatar);
            return;
        }
        
        // 使用异步方式加载头像，在加载成功后再绘制完整图片
        Glide.with(context.getApplicationContext())
                .asBitmap()
                .load(avatarUrl)
                .apply(new RequestOptions().centerCrop().override(avatarSize, avatarSize))
                .addListener(new RequestListener<Bitmap>() {
                    @Override
                    public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Bitmap> target, boolean isFirstResource) {
                        // 加载失败，使用默认头像
                        if (e != null) {
                            e.printStackTrace();
                        }
                        android.content.Context ctx = getContext();
                        if (ctx == null) {
                            ctx = getActivity();
                        }
                        if (ctx != null) {
                            Bitmap defaultAvatar = getDefaultAvatarBitmap(ctx, avatarSize);
                            generateCompleteQRCodeImage(defaultAvatar);
                        } else {
                            generateCompleteQRCodeImage(null);
                        }
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(Bitmap resource, Object model, Target<Bitmap> target, DataSource dataSource, boolean isFirstResource) {
                        // 加载成功，开始绘制完整图片
                        generateCompleteQRCodeImage(resource);
                        return false;
                    }
                })
                .submit();
    }

    /**
     * 获取默认头像Bitmap
     */
    private Bitmap getDefaultAvatarBitmap(android.content.Context context, int size) {
        try {
            // 使用Glide加载默认头像资源
            Bitmap defaultBitmap = Glide.with(context.getApplicationContext())
                    .asBitmap()
                    .load(com.yaoxin.appbase.R.mipmap.app_default_base_icon_geren)
                    .apply(new RequestOptions().centerCrop().override(size, size))
                    .submit()
                    .get();
            return defaultBitmap;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 从Drawable获取头像Bitmap，确保内容居中（centerCrop效果）
     * 注意：此方法会复制drawable，不会影响原始drawable
     */
    private Bitmap getAvatarBitmapFromDrawable(android.graphics.drawable.Drawable drawable, int size) {
        try {
            if (drawable != null) {
                // 保存原始bounds，避免影响原始drawable
                android.graphics.Rect originalBounds = drawable.getBounds();
                
                // 获取drawable的原始尺寸
                int drawableWidth = drawable.getIntrinsicWidth();
                int drawableHeight = drawable.getIntrinsicHeight();
                
                // 如果drawable没有固有尺寸，尝试从bounds获取
                if (drawableWidth <= 0 || drawableHeight <= 0) {
                    if (!originalBounds.isEmpty()) {
                        drawableWidth = originalBounds.width();
                        drawableHeight = originalBounds.height();
                    }
                }
                
                // 如果还是没有尺寸，使用目标尺寸
                if (drawableWidth <= 0) drawableWidth = size;
                if (drawableHeight <= 0) drawableHeight = size;
                
                // 创建bitmap
                Bitmap bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888);
                Canvas canvas = new Canvas(bitmap);
                
                // 计算缩放比例，确保图片填满整个区域（centerCrop效果）
                // 使用较大的缩放比例，确保图片能填满整个区域
                float scaleX = (float) size / drawableWidth;
                float scaleY = (float) size / drawableHeight;
                float scale = Math.max(scaleX, scaleY);
                
                // 计算缩放后的尺寸
                float scaledWidth = drawableWidth * scale;
                float scaledHeight = drawableHeight * scale;
                
                // 计算居中位置
                float left = (size - scaledWidth) / 2f;
                float top = (size - scaledHeight) / 2f;
                
                // 创建drawable的副本，避免影响原始drawable
                android.graphics.drawable.Drawable drawableCopy = drawable.getConstantState().newDrawable().mutate();
                
                // 设置drawable副本的bounds并绘制
                drawableCopy.setBounds((int) left, (int) top, (int) (left + scaledWidth), (int) (top + scaledHeight));
                drawableCopy.draw(canvas);
                
                // 恢复原始bounds（虽然使用了副本，但为了安全还是恢复）
                drawable.setBounds(originalBounds);
                
                return bitmap;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 生成完整的二维码图片（包含头像、用户名、ID、二维码）
     * 与设计图一致：白色背景，顶部头像+用户名+ID，中间黑白二维码
     * 注意：此方法应在后台线程调用，完全不影响界面显示
     * @param avatarBitmap 已加载好的头像Bitmap，如果为null则不绘制头像
     */
    private void generateCompleteQRCodeImage(@Nullable Bitmap avatarBitmap) {
        // 图片尺寸（根据设计图比例，使用1080x1080正方形）
        int imageWidth = 1080;
        int imageHeight = 1080;
        
        // 创建白色背景
        Bitmap resultBitmap = Bitmap.createBitmap(imageWidth, imageHeight, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(resultBitmap);
        canvas.drawColor(Color.WHITE);
        
        // 头像尺寸（根据设计图，头像约60-70像素，使用3倍图约180-210px）
        int avatarSize = 180; // 约60dp * 3
        
        // 绘制头像（左侧，距离左边和顶部各60px）
        int marginLeft = 60;
        int marginTop = 60;
        if (avatarBitmap != null && !avatarBitmap.isRecycled()) {
            // 绘制圆角头像
            Bitmap roundedAvatar = getRoundedBitmap(avatarBitmap, avatarSize);
            if (roundedAvatar != null && !roundedAvatar.isRecycled()) {
                canvas.drawBitmap(roundedAvatar, marginLeft, marginTop, null);
            }
        }
        
        // 绘制用户名和ID（头像右侧，垂直居中）
        int textStartX = marginLeft + avatarSize + 36; // 12dp * 3 = 36px
        int avatarCenterY = marginTop + avatarSize / 2;
        
        TextPaint namePaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
        namePaint.setColor(Color.BLACK);
        namePaint.setTextSize(48); // 18sp * 3 = 54px
        namePaint.setTypeface(Typeface.DEFAULT_BOLD);
        
        TextPaint idPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
        idPaint.setColor(Color.BLACK);
        idPaint.setTextSize(40); // 16sp * 3 = 48px（ID稍小一点）
        
        String userName = DataUtil.getUserInfo().username;
        String userId = "ID: " + DataUtil.getUserInfo().memberCode;
        
        // 计算文本基线位置，使文本垂直居中
        Paint.FontMetrics nameMetrics = namePaint.getFontMetrics();
        Paint.FontMetrics idMetrics = idPaint.getFontMetrics();
        float nameHeight = nameMetrics.bottom - nameMetrics.top;
        float idHeight = idMetrics.bottom - idMetrics.top;
        float totalTextHeight = nameHeight + idHeight + 30; // 30px间距
        
        float nameY = avatarCenterY - totalTextHeight / 2 + nameHeight - nameMetrics.bottom;
        float idY = nameY + nameHeight + 20; // 30px间距
        
        canvas.drawText(userName, textStartX, nameY, namePaint);
        canvas.drawText(userId, textStartX, idY, idPaint);
        
        // 绘制二维码（居中，距离用户信息区域下方有足够间距）
        Bitmap qrCodeBitmap = generateQRCode(DataUtil.getUserInfo().memberCode);
        if (qrCodeBitmap != null) {
            // 二维码应该占据大部分空间，根据设计图约占据下半部分
            int qrCodeSize = 750; // 250dp * 3 = 750px
            Bitmap scaledQRCode = Bitmap.createScaledBitmap(qrCodeBitmap, qrCodeSize, qrCodeSize, true);
            int qrCodeX = (imageWidth - qrCodeSize) / 2; // 水平居中
            int qrCodeY = marginTop + avatarSize + 80; // 距离头像区域80px
            canvas.drawBitmap(scaledQRCode, qrCodeX, qrCodeY, null);
        }
        
        // 保存图片到相册
        if (getActivity() != null) {
            getActivity().runOnUiThread(() -> {
                ImageUtil.saveBitmapToGallery(getContext(), resultBitmap);
            });
        }
    }

    /**
     * 将Bitmap裁剪为6dp圆角，确保头像内容居中
     */
    private Bitmap getRoundedBitmap(Bitmap bitmap, int size) {
        if (bitmap == null || bitmap.isRecycled()) {
            return null;
        }
        
        Bitmap roundedBitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(roundedBitmap);
        
        // 计算6dp对应的像素值（使用3倍图，所以是6dp * 3 = 18px）
        float cornerRadius = 18f; // 6dp * 3
        
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        BitmapShader shader = new BitmapShader(bitmap, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP);
        
        // 使用Matrix确保图片居中并填满（centerCrop效果）
        Matrix matrix = new Matrix();
        
        // 计算缩放比例，确保图片填满整个区域
        float scaleX = (float) size / bitmap.getWidth();
        float scaleY = (float) size / bitmap.getHeight();
        float scale = Math.max(scaleX, scaleY); // 使用较大的缩放比例，确保填满
        
        // 先缩放
        matrix.setScale(scale, scale);
        
        // 计算居中偏移量
        float scaledWidth = bitmap.getWidth() * scale;
        float scaledHeight = bitmap.getHeight() * scale;
        float dx = (size - scaledWidth) / 2f;
        float dy = (size - scaledHeight) / 2f;
        
        // 应用偏移，使图片居中
        matrix.postTranslate(dx, dy);
        
        shader.setLocalMatrix(matrix);
        paint.setShader(shader);
        
        // 绘制圆角矩形
        RectF rect = new RectF(0, 0, size, size);
        canvas.drawRoundRect(rect, cornerRadius, cornerRadius, paint);
        
        return roundedBitmap;
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
             // 获取头像URL，完全不影响ImageView
             String avatarUrl = DataUtil.getUserInfo() != null ? DataUtil.getUserInfo().avatar : null;
             int avatarSize = 180; // 约60dp * 3
             
             // 在后台线程加载头像，加载成功后再绘制完整图片
             executorService.execute(() -> {
                 loadAvatarAndGenerateImage(avatarUrl, avatarSize);
             });
         }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (executorService != null && !executorService.isShutdown()) {
            executorService.shutdown();
        }
    }
}
