package com.turunsi.yaoxin.main.mine.group;

import android.content.Context;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.chad.library.adapter4.BaseQuickAdapter;
import com.chad.library.adapter4.viewholder.QuickViewHolder;
import com.turunsi.yaoxin.R;
import com.yaoxin.appbase.model.GroupInfoBean;
import com.yaoxin.appbase.utils.NumberUtil;

/**
 * 群等级列表适配器
 */
public class GroupGradeAdapter extends BaseQuickAdapter<GroupInfoBean, QuickViewHolder> {

    @Override
    protected void onBindViewHolder(@NonNull QuickViewHolder holder, int position, @Nullable GroupInfoBean item) {
        if (item == null) return;

        // 设置背景（选中状态）
        holder.itemView.setSelected(item.isSelected);
        holder.setBackgroundResource(R.id.layoutbg, item.isSelected ? R.mipmap.icon_gour_buy2 : R.mipmap.icon_gour_buy1);
        // 显示等级名称
        holder.setText(R.id.item_group_grade_name_tv, item.gradeName != null ? item.gradeName : "VIP" + (item.grade - 1));
        // 显示价格
        holder.setText(R.id.item_group_grade_price_tv, "¥" + (item.price / 100) + "/永久");
        // 显示权益内容
        StringBuilder benefits = getStringBuilder(item);

        holder.setText(R.id.item_group_grade_benefits_tv, benefits.toString());
    }

    @NonNull
    private static StringBuilder getStringBuilder(@NonNull GroupInfoBean item) {
        StringBuilder benefits = new StringBuilder();
        benefits.append("超级群1个(上限").append(item.grade * 1000).append("人").append("\n");
        benefits.append("好友人数").append(item.grade * 1000).append("人\n");
        benefits.append("靓号优惠3").append("%\n");
        benefits.append("商城优惠3").append("%");

        return benefits;
    }

    @NonNull
    @Override
    protected QuickViewHolder onCreateViewHolder(@NonNull Context context, @NonNull ViewGroup parent, int viewType) {
        return new QuickViewHolder(R.layout.item_group_grade_cell, parent);
    }
}

