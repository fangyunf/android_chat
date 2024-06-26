package com.yaoxin.appbase.view;

import android.graphics.Rect;
import android.view.View;

import androidx.recyclerview.widget.RecyclerView;

public class CommonGridSpacingItemDecoration extends RecyclerView.ItemDecoration{

    private int spanCount; //列数
    private int spacing; //间隔
    private boolean includeEdge; //是否包含边缘

    public CommonGridSpacingItemDecoration(int spanCount, int spacing, boolean includeEdge) {
        this.spanCount = spanCount;
        this.spacing = spacing;
        this.includeEdge = includeEdge;
    }

    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent,
                               RecyclerView.State state) {
        outRect.bottom = spacing;
        outRect.left = spacing;
    }
}
