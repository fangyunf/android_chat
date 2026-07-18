package com.turunsi.yaoxin.main.found;

import android.content.Context;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.chad.library.adapter4.BaseQuickAdapter;
import com.chad.library.adapter4.viewholder.QuickViewHolder;
import com.turunsi.yaoxin.R;

public class FoundMenuAdapter extends BaseQuickAdapter<FoundMenuItem, QuickViewHolder> {

    @Override
    protected int getItemViewType(int position, @NonNull java.util.List<? extends FoundMenuItem> list) {
        return list.get(position).type;
    }

    @Override
    protected void onBindViewHolder(@NonNull QuickViewHolder holder, int position, @Nullable FoundMenuItem item) {
        if (item == null || item.type != FoundMenuItem.TYPE_MENU) {
            return;
        }
        holder.setText(R.id.found_item_title_tv, item.title);
        ImageView iconView = holder.getView(R.id.found_item_icon_iv);
        iconView.setImageResource(item.iconRes);
    }

    @NonNull
    @Override
    protected QuickViewHolder onCreateViewHolder(@NonNull Context context, @NonNull ViewGroup parent, int viewType) {
        if (viewType == FoundMenuItem.TYPE_GAP) {
            return new QuickViewHolder(R.layout.item_found_section_gap, parent);
        }
        return new QuickViewHolder(R.layout.item_found_menu, parent);
    }
}
