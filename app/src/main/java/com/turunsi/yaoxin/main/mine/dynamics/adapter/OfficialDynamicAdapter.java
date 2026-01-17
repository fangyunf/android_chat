package com.turunsi.yaoxin.main.mine.dynamics.adapter;

import android.content.Context;
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
import com.yaoxin.appbase.utils.TimeUtil;

public class OfficialDynamicAdapter extends BaseQuickAdapter<GroupInfoBean, QuickViewHolder> {

    @Override
    protected void onBindViewHolder(@NonNull QuickViewHolder holder, int position, @Nullable GroupInfoBean item) {
        if (item == null) {
            return;
        }
        
        TextView titleTv = holder.getView(R.id.item_official_dynamic_title_tv);
        TextView contentTv = holder.getView(R.id.item_official_dynamic_content_tv);
        TextView tagTv = holder.getView(R.id.item_official_dynamic_tag_tv);
        TextView timeTv = holder.getView(R.id.item_official_dynamic_time_tv);
        ImageView logoIv = holder.getView(R.id.item_official_dynamic_logo_iv);
        
        titleTv.setText("三样官方动态");
        contentTv.setText(item.content != null ? item.content : item.name);
        tagTv.setText("三样官方动态");
        timeTv.setText(TimeUtil.stampToDate(item.createTime));
        
        // 加载 logo（如果有 avatar 字段）
        if (item.avatar != null && !item.avatar.isEmpty()) {
            GlideUtil.yh_loadImageRoundedCorner(getContext(), logoIv, item.avatar, 4);
        }
    }

    @NonNull
    @Override
    protected QuickViewHolder onCreateViewHolder(@NonNull Context context, @NonNull ViewGroup parent, int viewType) {
        return new QuickViewHolder(R.layout.item_official_dynamic, parent);
    }
}

