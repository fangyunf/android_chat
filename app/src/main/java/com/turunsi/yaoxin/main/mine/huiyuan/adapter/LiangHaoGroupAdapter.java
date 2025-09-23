package com.turunsi.yaoxin.main.mine.huiyuan.adapter;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.chad.library.adapter4.BaseQuickAdapter;
import com.chad.library.adapter4.viewholder.QuickViewHolder;
import com.turunsi.yaoxin.R;
import com.turunsi.yaoxin.main.mine.huiyuan.LiangHaoZoneMoreActivity;
import com.yaoxin.appbase.model.HuiYuanBean;

public class LiangHaoGroupAdapter extends BaseQuickAdapter<HuiYuanBean, QuickViewHolder> {


    @Override
    protected void onBindViewHolder(@NonNull QuickViewHolder holder, int position, HuiYuanBean group) {
        ImageView ivIcon = holder.getView(R.id.ivGroupIcon);
        TextView tvMore = holder.getView(R.id.tvMore);
        RecyclerView rvNumbers = holder.getView(R.id.rvNumbers);
        int memberLevel = Integer.parseInt(group.memberConfig.memberLevel);
        String imageName = "mine_grade_level_" + memberLevel;
        int resId = getContext().getResources().getIdentifier(imageName, "mipmap", getContext().getPackageName());
        // 如果找到了资源，则可以使用这个ID获取Drawable
        Drawable drawable = null;
        if (resId > 0) {
            drawable = ContextCompat.getDrawable(getContext(), resId);
        }
        ivIcon.setImageDrawable(drawable);
        tvMore.setText("查看更多");
        rvNumbers.setLayoutManager(new GridLayoutManager(holder.itemView.getContext(), 3));
        LiangHaoNumberAdapter numberAdapter = new LiangHaoNumberAdapter(memberLevel);
        rvNumbers.setAdapter(numberAdapter);
        numberAdapter.setItems(group.memberCode);
        // 可加tvMore点击事件

        tvMore.setOnClickListener(view -> {
            Intent intent = new Intent(getContext(), LiangHaoZoneMoreActivity.class);
            intent.putExtra("huiyuan", group);
            getContext().startActivity(intent);
        });

        numberAdapter.setOnItemClickListener((baseQuickAdapter, view, i) -> {
//            Intent intent = new Intent(getContext(), LiangHaoBuyActivity.class);
//            intent.putExtra("huiyuan", group);
//            intent.putExtra("index", i);
//            getContext().startActivity(intent);
        });
    }

    @NonNull
    @Override
    protected QuickViewHolder onCreateViewHolder(@NonNull Context context, @NonNull ViewGroup parent, int viewType) {
        return new QuickViewHolder(R.layout.item_lianghao_group, parent);
    }
} 