package com.yaoxin.appbase.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.yaoxin.appbase.R;

public class IconTitleArrowTemplate_1 extends LinearLayout {
    public TextView rightTv;
    public ImageView rightIv;

    public IconTitleArrowTemplate_1(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    private void init(Context context, @Nullable AttributeSet attrs) {
        // Inflate the XML layout
        LayoutInflater.from(context).inflate(R.layout.icon_title_arrow_template_1, this, true);
        TextView titleTv = findViewById(R.id.icon_title_arrow_template_title_tv);
        rightTv = findViewById(R.id.icon_title_arrow_template_right_tv);
        rightIv = findViewById(R.id.icon_title_arrow_template_right_iv);
        ImageView imgIv = findViewById(R.id.icon_title_arrow_template_left_iv);
//        LinearLayout bgLL = findViewById(R.id.icon_title_arrow_template_ll);

        // Initialize any custom views or attributes here
        if (attrs != null) {
            TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.IconTitleArrowTemplate);
            String titleTvText = a.getString(R.styleable.IconTitleArrowTemplate_titleText);
            boolean hiddenIv = a.getBoolean(R.styleable.IconTitleArrowTemplate_hideImg, false);

            Drawable customImage = a.getDrawable(R.styleable.IconTitleArrowTemplate_leftImg);
//            int bgColor = a
//                    .getColor(
//                            R.styleable.IconTitleArrowTemplate_bgColor,
//                            Color.WHITE);
            int textColor = a.getColor(R.styleable.IconTitleArrowTemplate_textColor, Color.BLACK);
            a.recycle();

            // Apply custom attributes to views
            if (titleTvText != null) {
                titleTv.setText(titleTvText);
            }
            // Apply custom attributes to views
            titleTv.setTextColor(textColor);

            if (customImage != null) {
                imgIv.setImageDrawable(customImage);
            }
            if (hiddenIv) {
                imgIv.setVisibility(GONE);
            }
//            bgLL.setBackgroundColor(bgColor);
        }
    }
}
