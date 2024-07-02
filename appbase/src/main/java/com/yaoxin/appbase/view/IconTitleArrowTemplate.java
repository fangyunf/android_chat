package com.yaoxin.appbase.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.yaoxin.appbase.R;

public class IconTitleArrowTemplate extends LinearLayout {
    public IconTitleArrowTemplate(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context,attrs);
    }
    private void init(Context context,@Nullable AttributeSet attrs) {
        // Inflate the XML layout
        LayoutInflater.from(context).inflate(R.layout.icon_title_arrow_template, this, true);
        TextView titleTv = findViewById(R.id.icon_title_arrow_template_title_tv);
        ImageView imgIv = findViewById(R.id.icon_title_arrow_template_left_iv);

        // Initialize any custom views or attributes here
        if (attrs != null) {
            TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.IconTitleArrowTemplate);
            String titleTvText = a.getString(R.styleable.IconTitleArrowTemplate_titleText);

            Drawable customImage = a.getDrawable(R.styleable.IconTitleArrowTemplate_leftImg);
            a.recycle();

            // Apply custom attributes to views
            if (titleTvText != null) {
                titleTv.setText(titleTvText);
            }

            if (customImage != null) {
                imgIv.setImageDrawable(customImage);
            }
        }
    }
}
