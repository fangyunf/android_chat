package com.turunsi.yaoxin.main.conversation.adapter;

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

public class XiaoZhuShouListAdapter extends BaseQuickAdapter<GroupInfoBean, QuickViewHolder> {


    @Override
    protected void onBindViewHolder(@NonNull QuickViewHolder quickViewHolder, int position, @Nullable GroupInfoBean infoBean) {


    }

    @NonNull
    @Override
    protected QuickViewHolder onCreateViewHolder(@NonNull Context context, @NonNull ViewGroup viewGroup, int i) {
        return new QuickViewHolder(R.layout.item_xiaozhushou_list, viewGroup);
    }
}

