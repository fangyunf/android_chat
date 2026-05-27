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

import java.util.List;

public class TeamSettingUserMingDanListAdapter extends BaseQuickAdapter<GroupInfoBean, QuickViewHolder> {

    /** 0 禁止领取红包名单，1 单人禁言名单 */
    public static final int LIST_TYPE_FORBID_RED_PACKET = 0;
    public static final int LIST_TYPE_MUTE = 1;

    public int listType = LIST_TYPE_FORBID_RED_PACKET;
    public int opt_type = 0;
    public List<GroupInfoBean> contacts;

    @Override
    protected void onBindViewHolder(@NonNull QuickViewHolder quickViewHolder, int position, @Nullable GroupInfoBean infoBean) {

        ImageView iv = quickViewHolder.getView(R.id.cell_fun_team_setting_users_mingdan_head_iv);

        TextView tv = quickViewHolder.getView(R.id.cell_fun_team_setting_users_mingdan_tv_index);
        quickViewHolder.setText(R.id.cell_fun_team_setting_users_mingdan_name_tv, infoBean.name);
        GlideUtil.yh_loadImageRoundedCorner(getContext(), iv, infoBean.avatar, 22);

        TextView state_tv = quickViewHolder.getView(R.id.cell_fun_team_setting_users_mingdan_state_tv);
        boolean isSelf =
                listType == LIST_TYPE_MUTE
                        && infoBean != null
                        && TextUtils.equals(infoBean.userId, DataUtil.getUserid());
        if (isSelf) {
            state_tv.setVisibility(View.GONE);
        } else {
            state_tv.setVisibility(View.VISIBLE);
            int activeState = listType == LIST_TYPE_MUTE ? infoBean.muteState : infoBean.forbidState;
            state_tv.setSelected(activeState == 1);
            state_tv.setTextColor(
                    getContext()
                            .getResources()
                            .getColor(activeState == 1 ? R.color.color_white : R.color.color_999999));
            if (listType == LIST_TYPE_MUTE) {
                state_tv.setText(activeState == 0 ? "禁言" : "禁言中");
            } else {
                state_tv.setText(activeState == 0 ? "禁止" : "禁领中");
            }
        }
        if (position == 0 || !contacts.get(position-1).getIndex().equals(infoBean.getIndex())) {
            tv.setVisibility(View.VISIBLE);
            tv.setText(infoBean.getIndex());
        } else {
            tv.setVisibility(View.GONE);
        }
    }

    @NonNull
    @Override
    protected QuickViewHolder onCreateViewHolder(@NonNull Context context, @NonNull ViewGroup viewGroup, int i) {
        return new QuickViewHolder(R.layout.cell_fun_team_setting_users_mingdan, viewGroup);
    }
}

