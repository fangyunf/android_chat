package com.netease.yunxin.kit.teamkit.ui.fun.activity.adapter;

import android.content.Context;
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

import java.util.List;

public class TeamSettingUserListAdapter extends BaseQuickAdapter<GroupInfoBean, QuickViewHolder> {

public int opt_type = 0;

    @Override
    protected void onBindViewHolder(@NonNull QuickViewHolder quickViewHolder, int i, @Nullable GroupInfoBean infoBean) {

        ImageView iv = quickViewHolder.getView(R.id.cell_fun_team_setting_users_head_iv);

            TextView tv = quickViewHolder.getView(R.id.cell_fun_team_setting_users_name_tv_role);
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
//            if (opt_type > 0) {
//                if (opt_type == 1) {
//                    if (infoBean.rankState == 1) {
//                        tv.setText("群主");
//                    } else {
//                        tv.setVisibility(View.GONE);
//                    }
//                } else if (opt_type == 2) {
//                    if (infoBean.rankState == 2) {
//                        tv.setText("管理");
//                    } else {
//                        tv.setVisibility(View.GONE);
//                    }
//                }
//            } else {
//
//            }
    }
    @NonNull
    @Override
    protected QuickViewHolder onCreateViewHolder(@NonNull Context context, @NonNull ViewGroup viewGroup, int i) {
        return new QuickViewHolder(R.layout.cell_fun_team_setting_users,viewGroup);
    }
}

