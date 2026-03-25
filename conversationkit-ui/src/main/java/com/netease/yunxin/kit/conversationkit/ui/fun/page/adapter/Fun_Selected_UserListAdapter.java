package com.netease.yunxin.kit.conversationkit.ui.fun.page.adapter;

import android.content.Context;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.chad.library.adapter4.BaseQuickAdapter;
import com.chad.library.adapter4.viewholder.QuickViewHolder;
import com.netease.yunxin.kit.conversationkit.ui.R;
import com.yaoxin.appbase.model.GroupInfoBean;
import com.yaoxin.appbase.utils.GlideUtil;

import java.util.List;

public class Fun_Selected_UserListAdapter extends BaseQuickAdapter<GroupInfoBean, QuickViewHolder> {

    public int opt_type = 0;
    public List<GroupInfoBean> contacts;

    @Override
    protected void onBindViewHolder(@NonNull QuickViewHolder quickViewHolder, int position, @Nullable GroupInfoBean infoBean) {

        ImageView iv = quickViewHolder.getView(R.id.cell_fun_selected_user_list_head_iv);
        ImageView selectIv = quickViewHolder.getView(R.id.cell_fun_selected_user_list_selected_iv);
        TextView inGroupTv = quickViewHolder.getView(R.id.cell_fun_selected_user_list_in_group_tv);
        TextView tv = quickViewHolder.getView(R.id.cell_fun_selected_user_list_tv_index);
        if (!TextUtils.isEmpty(infoBean.remark)) {
            quickViewHolder.setText(R.id.cell_fun_selected_user_list_name_tv, infoBean.remark);
        } else {
            quickViewHolder.setText(R.id.cell_fun_selected_user_list_name_tv, infoBean.name);
        }
        GlideUtil.yh_loadImageRoundedCorner(getContext(), iv, infoBean.avatar, 22);
        if (infoBean.alreadyInGroup) {
            inGroupTv.setVisibility(View.VISIBLE);
            selectIv.setSelected(true);
            selectIv.setEnabled(false);
            selectIv.setAlpha(0.45f);
        } else {
            inGroupTv.setVisibility(View.GONE);
            selectIv.setSelected(infoBean.isSelected);
            selectIv.setEnabled(true);
            selectIv.setAlpha(1f);
        }
        if (position == 0 || !contacts.get(position - 1).getIndex().equals(infoBean.getIndex())) {
            tv.setVisibility(View.VISIBLE);
            tv.setText(infoBean.getIndex());
        } else {
            tv.setVisibility(View.GONE);
        }
    }

    @NonNull
    @Override
    protected QuickViewHolder onCreateViewHolder(@NonNull Context context, @NonNull ViewGroup viewGroup, int i) {
        return new QuickViewHolder(R.layout.cell_fun_selected_user_list, viewGroup);
    }
}

