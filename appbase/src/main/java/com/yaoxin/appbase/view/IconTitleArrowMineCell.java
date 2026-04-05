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

/**
 * 「我的」等功能列表行：相对 {@link IconTitleArrowTemplate} 去掉行内左右留白（由外层容器统一 padding），
 * 左侧图标略小，标题字号略调，左右贴齐容器内容区。
 */
public class IconTitleArrowMineCell extends LinearLayout {

  public IconTitleArrowMineCell(Context context) {
    this(context, null);
  }

  public IconTitleArrowMineCell(Context context, @Nullable AttributeSet attrs) {
    super(context, attrs);
    init(context, attrs);
  }

  private void init(Context context, @Nullable AttributeSet attrs) {
    LayoutInflater.from(context).inflate(R.layout.icon_title_arrow_mine_cell, this, true);
    TextView titleTv = findViewById(R.id.icon_title_arrow_mine_cell_title_tv);
    ImageView imgIv = findViewById(R.id.icon_title_arrow_mine_cell_left_iv);

    if (attrs != null) {
      TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.IconTitleArrowMineCell);
      String titleText = a.getString(R.styleable.IconTitleArrowMineCell_mineCellTitle);
      Drawable left = a.getDrawable(R.styleable.IconTitleArrowMineCell_mineCellLeftImg);
      int textColor =
          a.getColor(R.styleable.IconTitleArrowMineCell_mineCellTextColor, Color.parseColor("#333333"));
      a.recycle();

      if (titleText != null) {
        titleTv.setText(titleText);
      }
      titleTv.setTextColor(textColor);
      if (left != null) {
        imgIv.setVisibility(VISIBLE);
        imgIv.setImageDrawable(left);
      } else {
        imgIv.setVisibility(GONE);
      }
    }
  }
}
