package com.netease.yunxin.kit.teamkit.ui.fun.activity.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.chad.library.adapter4.BaseQuickAdapter;
import com.chad.library.adapter4.viewholder.QuickViewHolder;
import com.netease.yunxin.kit.teamkit.ui.R;
import com.yaoxin.appbase.model.CustomMsgBean;
import com.yaoxin.appbase.model.GroupInfoBean;
import com.yaoxin.appbase.utils.GlideUtil;
import com.yaoxin.appbase.utils.NumberUtil;
import com.yaoxin.appbase.utils.TimeUtil;

import java.util.List;

public class TeamSettingUserInfoAdapter extends BaseQuickAdapter<GroupInfoBean, QuickViewHolder> {

    List<GroupInfoBean> userInfoList;

    boolean isManager = false;
    public TeamSettingUserInfoAdapter(boolean isM, List<GroupInfoBean> userInfo) {
        isManager = isM;
        userInfoList = userInfo;
    }

    @Override
    protected void onBindViewHolder(@NonNull QuickViewHolder quickViewHolder, int i, @Nullable GroupInfoBean bean) {

        ImageView iv = quickViewHolder.getView(R.id.cell_fun_team_setting_users_head_iv);

        TextView tv = quickViewHolder.getView(R.id.cell_fun_team_setting_users_name_tv_role);
        if (i >= userInfoList.size()) {
            tv.setVisibility(View.GONE);
            quickViewHolder.setText(R.id.cell_fun_team_setting_users_name_tv, "");
            if (i == userInfoList.size()) {
                iv.setImageResource(com.yaoxin.appbase.R.drawable.team_setting_add_user);
            } else {
                iv.setImageResource(com.yaoxin.appbase.R.drawable.team_setting_delete_user);
            }
        } else {
            GroupInfoBean infoBean = userInfoList.get(i);
            quickViewHolder.setText(R.id.cell_fun_team_setting_users_name_tv, infoBean.name);
            GlideUtil.yh_loadImageRoundedCorner(getContext(),iv,infoBean.avatar,26);
            tv.setVisibility(View.VISIBLE);
            if (infoBean.rankState == 1) {
                tv.setText("群主");
            } else if (infoBean.rankState == 2) {
                tv.setText("管理");
            } else {
                tv.setVisibility(View.GONE);
            }

        }
    }
    @NonNull
    @Override
    protected QuickViewHolder onCreateViewHolder(@NonNull Context context, @NonNull ViewGroup viewGroup, int i) {
        return new QuickViewHolder(R.layout.cell_fun_team_setting_users,viewGroup);
    }
    protected int getItemCount(@NonNull List<? extends GroupInfoBean> items) {
        if (isManager) {
            return userInfoList.size() + 2;
        }
        return userInfoList.size() + 1;
    }
}

