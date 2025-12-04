package com.yaoxin.appbase.view;

import android.graphics.Rect;
import android.view.View;

import androidx.recyclerview.widget.RecyclerView;

/**
 * 网格布局间距装饰器
 * 支持自定义列数、横向间隙、纵向间隙
 */
public class GridSpacingItemDecoration extends RecyclerView.ItemDecoration {

    private int spanCount;        // 列数
    private int horizontalSpacing; // 横向间隙（列间距）
    private int verticalSpacing;   // 纵向间隙（行间距）
    private boolean includeEdge;   // 是否包含边缘

    /**
     * @param spanCount 列数
     * @param horizontalSpacing 横向间隙（列间距）
     * @param verticalSpacing 纵向间隙（行间距）
     * @param includeEdge 是否包含边缘
     */
    public GridSpacingItemDecoration(int spanCount, int horizontalSpacing, int verticalSpacing, boolean includeEdge) {
        this.spanCount = spanCount;
        this.horizontalSpacing = horizontalSpacing;
        this.verticalSpacing = verticalSpacing;
        this.includeEdge = includeEdge;
    }

    /**
     * @param spanCount 列数
     * @param spacing 横向和纵向间隙相同
     * @param includeEdge 是否包含边缘
     */
    public GridSpacingItemDecoration(int spanCount, int spacing, boolean includeEdge) {
        this(spanCount, spacing, spacing, includeEdge);
    }

    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
        int position = parent.getChildAdapterPosition(view); // item position
        if (position < 0) {
            return;
        }

        int column = position % spanCount; // item column

        if (includeEdge) {
            // 包含边缘的情况
            outRect.left = horizontalSpacing - column * horizontalSpacing / spanCount;
            outRect.right = (column + 1) * horizontalSpacing / spanCount;

            if (position < spanCount) { // 第一行
                outRect.top = verticalSpacing;
            }
            outRect.bottom = verticalSpacing; // 每个item底部都有间隙
        } else {
            // 不包含边缘的情况
            outRect.left = column * horizontalSpacing / spanCount;
            outRect.right = horizontalSpacing - (column + 1) * horizontalSpacing / spanCount;

            if (position >= spanCount) { // 不是第一行
                outRect.top = verticalSpacing;
            }
        }
    }
}

