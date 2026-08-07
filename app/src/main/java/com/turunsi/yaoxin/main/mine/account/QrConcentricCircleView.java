package com.turunsi.yaoxin.main.mine.account;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;

/**
 * 同心正圆背景：按短边取半径，保证是圆不是椭圆。
 */
public class QrConcentricCircleView extends View {

    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);

    // 由外到内填充，中心白圆需覆盖二维码
    private final int[] colors = {
            0xFFD9ECF8,
            0xFFE4F2FA,
            0xFFEEF7FC,
            0xFFF7FBFE,
            0xFFFFFFFF
    };
    private final float[] ratios = {1.0f, 0.86f, 0.72f, 0.60f, 0.52f};

    public QrConcentricCircleView(Context context) {
        super(context);
    }

    public QrConcentricCircleView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public QrConcentricCircleView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        float cx = getWidth() / 2f;
        float cy = getHeight() / 2f;
        // 关键边决定半径，保证正圆
        float maxRadius = Math.min(getWidth(), getHeight()) / 2f;
        paint.setStyle(Paint.Style.FILL);
        for (int i = 0; i < colors.length; i++) {
            paint.setColor(colors[i]);
            canvas.drawCircle(cx, cy, maxRadius * ratios[i], paint);
        }
    }
}
