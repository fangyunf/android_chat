package com.turunsi.yaoxin.main.mine.group;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.chad.library.adapter4.BaseQuickAdapter;
import com.chad.library.adapter4.viewholder.QuickViewHolder;
import com.turunsi.yaoxin.R;
import com.yaoxin.appbase.model.GroupInfoBean;
import com.yaoxin.appbase.utils.GlideUtil;

import java.util.List;

/**
 * 选择群列表适配器
 */
public class SelectGroupAdapter extends BaseQuickAdapter<GroupInfoBean, QuickViewHolder> {

    public List<GroupInfoBean> groups; // 用于字母索引判断

    @Override
    protected void onBindViewHolder(@NonNull QuickViewHolder holder, int position, @Nullable GroupInfoBean item) {
        if (item == null) return;
        // 群头像
        ImageView avatarIv = holder.getView(R.id.item_select_group_avatar_iv);
        GlideUtil.yh_loadImageRoundedCorner(getContext(), avatarIv, item.head, 3);
        // 群名称
        holder.setText(R.id.item_select_group_name_tv, item.name);
        // 选中状态
        ImageView selIv = holder.getView(R.id.item_select_group_sel_iv);
        selIv.setSelected(item.isSelected);

        // 字母索引显示逻辑
        TextView indexTv = holder.getView(R.id.item_select_group_tv_index);
        if (groups == null || groups.isEmpty()) {
            indexTv.setVisibility(View.GONE);
            return;
        }
        String currentIndex = item.getIndex();
        // 判断是否显示字母索引：如果是第一个或者和前一个的首字母不同
        if (position == 0 || !groups.get(position - 1).getIndex().equals(currentIndex)) {
            indexTv.setVisibility(View.VISIBLE);
            indexTv.setText(currentIndex);
        } else {
            indexTv.setVisibility(View.GONE);
        }
    }

    @NonNull
    @Override
    protected QuickViewHolder onCreateViewHolder(@NonNull Context context, @NonNull ViewGroup parent, int viewType) {
        return new QuickViewHolder(R.layout.item_select_group_cell, parent);
    }
}

