package com.netease.yunxin.kit.teamkit.ui.fun.activity.adapter;

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
import com.netease.yunxin.kit.teamkit.ui.R;
import com.yaoxin.appbase.model.GroupInfoBean;
import com.yaoxin.appbase.utils.DataUtil;
import com.yaoxin.appbase.utils.GlideUtil;

public class TeamSettingUserVerticalAdapter extends BaseQuickAdapter<GroupInfoBean, QuickViewHolder> {

    public int opt_type = 0;

    @Override
    protected void onBindViewHolder(@NonNull QuickViewHolder quickViewHolder, int i, @Nullable GroupInfoBean infoBean) {
        ImageView headIv = quickViewHolder.getView(R.id.cell_fun_team_setting_users_vertical_head_iv);
        TextView nameTv = quickViewHolder.getView(R.id.cell_fun_team_setting_users_vertical_name_tv);
        TextView roleTv = quickViewHolder.getView(R.id.cell_fun_team_setting_users_vertical_role_tv);
        
        String remark = "";
        try {
            for (GroupInfoBean groupInfoBean : DataUtil.getFriendInfoList()) {
                if (groupInfoBean.userId.equals(infoBean.userId)) {
                    if (!TextUtils.isEmpty(groupInfoBean.remark)) {
                        remark = groupInfoBean.remark;
                    }
                    break;
                }
            }
        } catch (Exception e) {
        }

        // 设置名称
        if (!TextUtils.isEmpty(remark)) {
            nameTv.setText(remark);
        } else {
            nameTv.setText(infoBean.name);
        }

        // 加载方形头像（不使用圆角）
        GlideUtil.yh_loadImage(getContext(), headIv, infoBean.avatar);

        // 设置角色标签
        roleTv.setVisibility(View.VISIBLE);
        if (infoBean.rankState == 1) {
            roleTv.setText("群主");
        } else if (infoBean.rankState == 2) {
            roleTv.setText("管理员");
        } else {
            roleTv.setVisibility(View.GONE);
        }
    }

    @NonNull
    @Override
    protected QuickViewHolder onCreateViewHolder(@NonNull Context context, @NonNull ViewGroup viewGroup, int i) {
        return new QuickViewHolder(R.layout.cell_fun_team_setting_users_vertical, viewGroup);
    }
}
