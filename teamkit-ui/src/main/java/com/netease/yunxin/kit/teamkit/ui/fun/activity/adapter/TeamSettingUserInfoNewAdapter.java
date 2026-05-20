package com.netease.yunxin.kit.teamkit.ui.fun.activity.adapter;

import android.content.Context;
import android.graphics.Color;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.chad.library.adapter4.BaseQuickAdapter;
import com.chad.library.adapter4.viewholder.QuickViewHolder;
import com.netease.yunxin.kit.teamkit.ui.R;
import com.yaoxin.appbase.model.GroupInfoBean;
import com.yaoxin.appbase.utils.GlideUtil;
import com.yaoxin.appbase.utils.ResourceHelper;

import java.util.List;

public class TeamSettingUserInfoNewAdapter extends BaseQuickAdapter<GroupInfoBean, QuickViewHolder> {

    List<GroupInfoBean> userInfoList;

    boolean isManager = false;

    public TeamSettingUserInfoNewAdapter(boolean isM, List<GroupInfoBean> userInfo) {
        isManager = isM;
        userInfoList = userInfo;
    }

    @Override
    protected void onBindViewHolder(@NonNull QuickViewHolder quickViewHolder, int i, @Nullable GroupInfoBean bean) {
        ImageView iv = quickViewHolder.getView(R.id.cell_fun_team_setting_users_head_iv);
        ImageView ivGradeBg = quickViewHolder.getView(R.id.tvGradBg);
        ImageView ivGrade = quickViewHolder.getView(R.id.ivGrade);
        TextView tv = quickViewHolder.getView(R.id.cell_fun_team_setting_users_name_tv_role);
        GroupInfoBean infoBean = userInfoList.get(i);
        quickViewHolder.setText(R.id.cell_fun_team_setting_users_name_tv, infoBean.name);
        GlideUtil.yh_loadImageRoundedCorner(getContext(), iv, infoBean.avatar, 26);
        tv.setVisibility(View.VISIBLE);
        if (infoBean.rankState == 1) {
            tv.setText("群主");
        } else if (infoBean.rankState == 2) {
            tv.setText("管理");
        } else {
            tv.setVisibility(View.GONE);
        }
        if (infoBean.grade > 0) {
            ivGrade.setVisibility(View.VISIBLE);
            ivGradeBg.setVisibility(View.VISIBLE);
            ivGrade.setImageDrawable(ResourceHelper.getGradeDrawable(iv.getContext(), infoBean.grade));
            quickViewHolder.setTextColor(R.id.cell_fun_team_setting_users_name_tv, ResourceHelper.getGradeColor(iv.getContext(), infoBean.grade));
            ivGradeBg.setImageDrawable(ResourceHelper.getGradeBackground(iv.getContext(), infoBean.grade));
        } else {
            quickViewHolder.setTextColor(R.id.cell_fun_team_setting_users_name_tv, Color.parseColor("#333333"));
            ivGrade.setVisibility(View.GONE);
            ivGradeBg.setVisibility(View.GONE);
        }
    }

    @NonNull
    @Override
    protected QuickViewHolder onCreateViewHolder(@NonNull Context context, @NonNull ViewGroup viewGroup, int i) {
        return new QuickViewHolder(R.layout.cell_fun_team_setting_new_users, viewGroup);
    }

    protected int getItemCount(@NonNull List<? extends GroupInfoBean> items) {
        return userInfoList.size();
    }
}

