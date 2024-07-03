package com.netease.yunxin.kit.contactkit.ui.fun.verify.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.chad.library.adapter4.BaseQuickAdapter;
import com.chad.library.adapter4.viewholder.QuickViewHolder;
import com.netease.yunxin.kit.contactkit.ui.R;
import com.yaoxin.appbase.model.UserBean;
import com.yaoxin.appbase.utils.GlideUtil;

public class FunVerifyFriendListAdapter extends BaseQuickAdapter<UserBean, QuickViewHolder> {

    public int business_type;
    @Override
    protected void onBindViewHolder(@NonNull QuickViewHolder quickViewHolder, int i, @Nullable UserBean userInfo) {

        if (business_type == 1) {
            TextView view = quickViewHolder.getView(R.id.fun_verify_friend_list_cell_invite_tv);
            view.setVisibility(View.VISIBLE);
            view.setText("邀请人: " + userInfo.inviteName);

            quickViewHolder.setText(R.id.fun_verify_friend_list_cell_name_tv, userInfo.userName)
                    .setText(R.id.fun_verify_friend_list_cell_id_tv, "ID:"+userInfo.userMemberCode)
                    .setText(R.id.fun_verify_friend_list_cell_verify_content, "申请加入"+userInfo.groupName)
                    .setText(R.id.fun_verify_friend_list_cell_verify_msg, "您好，我是"+ userInfo.userName+"，申请加入该群");
            GlideUtil.yh_loadImage(getContext(), quickViewHolder.getView(R.id.fun_verify_friend_list_cell_head_iv), userInfo.userAvatar);

        } else {
            quickViewHolder.setText(R.id.fun_verify_friend_list_cell_name_tv, userInfo.name)
                    .setText(R.id.fun_verify_friend_list_cell_id_tv, userInfo.userId)
                    .setText(R.id.fun_verify_friend_list_cell_verify_content, userInfo.leaveMessage)
                    .setText(R.id.fun_verify_friend_list_cell_verify_msg, "");
            GlideUtil.yh_loadImage(getContext(), quickViewHolder.getView(R.id.fun_verify_friend_list_cell_head_iv), userInfo.avatar);
        }
    }

    @NonNull
    @Override
    protected QuickViewHolder onCreateViewHolder(@NonNull Context context, @NonNull ViewGroup viewGroup, int i) {
        return new QuickViewHolder(R.layout.fun_verify_friend_list_cell,viewGroup);
    }
}

