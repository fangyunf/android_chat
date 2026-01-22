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
        TextView tv = quickViewHolder.getView(R.id.cell_fun_selected_user_list_tv_index);
        if (!TextUtils.isEmpty(infoBean.remark)) {
            quickViewHolder.setText(R.id.cell_fun_selected_user_list_name_tv, infoBean.remark);
        } else {
            quickViewHolder.setText(R.id.cell_fun_selected_user_list_name_tv, infoBean.name);
        }
        GlideUtil.yh_loadImageRoundedCorner(getContext(), iv, infoBean.avatar, 22);
//        selectIv.setVisibility(infoBean.isSelected ? View.VISIBLE:View.GONE);
        selectIv.setSelected(infoBean.isSelected);
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

