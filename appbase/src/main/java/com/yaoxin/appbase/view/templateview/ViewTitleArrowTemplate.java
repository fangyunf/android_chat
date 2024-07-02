package com.yaoxin.appbase.view.templateview;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.yaoxin.appbase.R;

public class ViewTitleArrowTemplate extends LinearLayout {

    public ViewTitleArrowTemplate(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);

    }
    private void init(Context context, AttributeSet attrs) {
        LayoutInflater.from(context).inflate(R.layout.view_title_arrow_template, this, true);

        TypedArray a = context.getTheme().obtainStyledAttributes(
                attrs,
                R.styleable.ViewTitleArrowTemplate,
                0, 0);

        try {
            String customText = a.getString(R.styleable.ViewTitleArrowTemplate_leftTitleText);
            TextView textView = findViewById(R.id.view_title_arrow_tv);
            textView.setText(customText);

//            int customImageResId = a.getResourceId(R.styleable.ViewTitleArrowTemplate_leftImg, -1);
            ImageView imageView = findViewById(R.id.view_title_arrow_template_left_iv);
//            if (customImageResId != -1) {
//                imageView.setImageResource(customImageResId);
//            }
        } finally {
            a.recycle();
        }
    }
}
