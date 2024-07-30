package com.netease.yunxin.kit.contactkit.ui.normal.contact.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.chad.library.adapter4.BaseQuickAdapter;
import com.chad.library.adapter4.viewholder.QuickViewHolder;
import com.netease.yunxin.kit.contactkit.ui.R;
import com.yaoxin.appbase.model.GroupInfoBean;
import com.yaoxin.appbase.net.Constant;
import com.yaoxin.appbase.utils.GlideUtil;

import java.util.List;

public class ContactUserListAdapter extends BaseQuickAdapter<GroupInfoBean, QuickViewHolder> {

    public int opt_type = 0;
    public List<GroupInfoBean> contacts;

    public int friendApplyNum = 0;
    public int groupApplyNum = 0;

    public boolean _isSearch = false;

    @Override
    protected void onBindViewHolder(@NonNull QuickViewHolder quickViewHolder, int position, @Nullable GroupInfoBean infoBean) {
        ImageView iv = quickViewHolder.getView(R.id.cell_fun_team_setting_users_mingdan_head_iv);
        TextView tv = quickViewHolder.getView(R.id.cell_fun_team_setting_users_mingdan_tv_index);

        GroupInfoBean infoBean1;
        if (_isSearch) {
            infoBean1 = contacts.get(position);
            tv.setVisibility(View.GONE);
        } else {
            infoBean1 = contacts.get(position);
            if (position == 0 || !contacts.get(position - 1).getIndex().equals(infoBean1.getIndex())) {
                tv.setVisibility(View.VISIBLE);
                tv.setText(infoBean1.getIndex());
            } else {
                tv.setVisibility(View.GONE);
            }
        }
        quickViewHolder.setText(R.id.cell_fun_team_setting_users_mingdan_name_tv, (infoBean1.remark != null && !infoBean1.remark.isEmpty() ) ? infoBean1.remark : infoBean1.name);
        GlideUtil.yh_loadImageRoundedCorner(getContext(), iv, infoBean1.avatar, 22);


    }
    @Override
    protected int getItemViewType(int position, @NonNull List<? extends GroupInfoBean> list) {
        return Constant.RECYCLE_VIEW_ITEM;
    }
    @NonNull
    @Override
    protected QuickViewHolder onCreateViewHolder(@NonNull Context context, @NonNull ViewGroup viewGroup, int i) {
        return new QuickViewHolder(R.layout.caontact_new_user, viewGroup);
    }

    @Override
    protected int getItemCount(@NonNull List<? extends GroupInfoBean> items) {
        return super.getItemCount(items);
    }
}

