package com.netease.yunxin.kit.contactkit.ui.normal.contact.adapter;

import android.content.Context;
import android.graphics.Color;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.chad.library.adapter4.BaseQuickAdapter;
import com.chad.library.adapter4.viewholder.QuickViewHolder;
import com.netease.yunxin.kit.contactkit.ui.R;

public class ShopCategoryAdapter extends BaseQuickAdapter<String, QuickViewHolder> {

    private int selectedIndex = 0;

    public void setSelectedIndex(int index) {
        int old = selectedIndex;
        selectedIndex = index;
        if (old >= 0 && old < getItemCount()) {
            notifyItemChanged(old);
        }
        if (selectedIndex >= 0 && selectedIndex < getItemCount()) {
            notifyItemChanged(selectedIndex);
        }
    }

    public int getSelectedIndex() {
        return selectedIndex;
    }

    @Override
    protected void onBindViewHolder(
            @NonNull QuickViewHolder holder, int position, @Nullable String category) {
        TextView tv = holder.getView(R.id.shop_category_chip_tv);
        tv.setText(category == null ? "" : category);
        boolean selected = position == selectedIndex;
        tv.setSelected(selected);
        tv.setTextColor(selected ? Color.WHITE : Color.parseColor("#111827"));
    }

    @NonNull
    @Override
    protected QuickViewHolder onCreateViewHolder(
            @NonNull Context context, @NonNull ViewGroup parent, int viewType) {
        return new QuickViewHolder(R.layout.shop_category_chip_item, parent);
    }
}
