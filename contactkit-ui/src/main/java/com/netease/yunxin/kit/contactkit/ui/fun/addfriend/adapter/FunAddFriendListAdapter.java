package com.netease.yunxin.kit.contactkit.ui.fun.addfriend.adapter;

import android.content.Context;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.chad.library.adapter4.BaseQuickAdapter;
import com.chad.library.adapter4.viewholder.QuickViewHolder;
import com.netease.yunxin.kit.contactkit.ui.R;
import com.netease.yunxin.kit.contactkit.ui.fun.addfriend.bean.FunAddFriendListBean;
import com.netease.yunxin.kit.corekit.im.model.UserInfo;
import com.yaoxin.appbase.model.UserBean;
import com.yaoxin.appbase.utils.GlideUtil;

public class FunAddFriendListAdapter extends BaseQuickAdapter<UserBean, QuickViewHolder> {

    @Override
    protected void onBindViewHolder(@NonNull QuickViewHolder quickViewHolder, int i, @Nullable UserBean userInfo) {

        quickViewHolder.setText(R.id.item_fun_addfriend_list_cell_name_tv,userInfo.name)
                .setText(R.id.item_fun_addfriend_list_cell_id_tv,userInfo.memberCode);
        GlideUtil.yh_loadImage(getContext(),quickViewHolder.getView(R.id.item_fun_addfriend_list_cell_head_iv),userInfo.avatar);
    }

    @NonNull
    @Override
    protected QuickViewHolder onCreateViewHolder(@NonNull Context context, @NonNull ViewGroup viewGroup, int i) {
        return new QuickViewHolder(R.layout.item_fun_addfriend_list_cell,viewGroup);
    }
}

