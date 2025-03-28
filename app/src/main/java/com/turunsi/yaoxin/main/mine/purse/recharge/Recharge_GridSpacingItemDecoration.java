package com.turunsi.yaoxin.main.mine.purse.recharge;

import android.graphics.Rect;
import android.view.View;

import androidx.recyclerview.widget.RecyclerView;

public class Recharge_GridSpacingItemDecoration extends RecyclerView.ItemDecoration {

  private int spanCount; //列数
  private int spacing; //间隔
  private boolean includeEdge; //是否包含边缘
  public int leftSpace;

  public Recharge_GridSpacingItemDecoration(int spanCount, int spacing, boolean includeEdge) {
    this.spanCount = spanCount;
    this.spacing = spacing;
    this.includeEdge = includeEdge;
  }

  @Override
  public void getItemOffsets(Rect outRect, View view, RecyclerView parent,
      RecyclerView.State state) {
    //不是第一个的格子都设一个左边和底部的间距
    outRect.left = leftSpace;
    outRect.bottom = spacing;
    //由于每行都只有3个，所以第一个都是3的倍数，把左边距设为0
    /*if (parent.getChildLayoutPosition(view) % spanCount == 0) {
      outRect.left = 0;
    }*/
  }
}