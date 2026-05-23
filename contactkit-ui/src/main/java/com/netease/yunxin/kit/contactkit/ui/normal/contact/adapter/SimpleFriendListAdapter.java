package com.netease.yunxin.kit.contactkit.ui.normal.contact.adapter;

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
import com.netease.yunxin.kit.contactkit.ui.R;
import com.yaoxin.appbase.model.GroupInfoBean;
import com.yaoxin.appbase.utils.GlideUtil;
import com.yaoxin.appbase.utils.ResourceHelper;

import java.util.List;

/**
 * 简化的好友列表适配器（不含 header）
 */
public class SimpleFriendListAdapter extends BaseQuickAdapter<GroupInfoBean, QuickViewHolder> {

    public List<GroupInfoBean> contacts;

    @Override
    protected void onBindViewHolder(@NonNull QuickViewHolder holder, int position, @Nullable GroupInfoBean item) {
        if (item == null) {
            return;
        }

        ImageView avatarIv = holder.getView(R.id.cell_fun_team_setting_users_mingdan_head_iv);
        TextView indexTv = holder.getView(R.id.cell_fun_team_setting_users_mingdan_tv_index);
        TextView nameTv = holder.getView(R.id.cell_fun_team_setting_users_mingdan_name_tv);
        ImageView ivGrade = holder.getView(R.id.ivGrade);

        // 显示名称（优先显示备注，群聊显示群名）
        String displayName;
        if (item.userGroupName != null && !item.userGroupName.isEmpty()) {
            // 群聊
            displayName = item.userGroupName;
        } else if (item.remark != null && !item.remark.isEmpty()) {
            // 好友备注
            displayName = item.remark;
        } else {
            // 好友名称
            displayName = item.name;
        }
        nameTv.setText(displayName);

        // 加载头像
        GlideUtil.yh_loadImageRoundedCorner(getContext(), avatarIv, item.avatar, 22);

        if (item.grade > 0) {
            ivGrade.setVisibility(View.GONE);
            nameTv.setTextColor(ResourceHelper.getGradeColor(getContext(), item.grade));
            ivGrade.setImageDrawable(ResourceHelper.getGradeDrawable(getContext(), item.grade));
        } else {
            nameTv.setTextColor(Color.parseColor("#333333"));
            ivGrade.setVisibility(View.GONE);
        }

        // 显示首字母索引
        if (contacts == null || contacts.isEmpty()) {
            indexTv.setVisibility(View.GONE);
            return;
        }

        // 判断是群聊还是好友
        boolean isGroup = item.userGroupName != null && !item.userGroupName.isEmpty();

        String currentIndex;
        if (isGroup) {
            // 群聊使用群名的首字母
            currentIndex = com.nanchen.wavesidebar.FirstLetterUtil.getFirstLetter(item.userGroupName);
        } else {
            // 好友使用 getIndex()
            currentIndex = item.getIndex();
        }

        if (position == 0) {
            indexTv.setVisibility(View.VISIBLE);
            indexTv.setText(currentIndex);
        } else {
            GroupInfoBean prevItem = contacts.get(position - 1);
            boolean prevIsGroup = prevItem.userGroupName != null && !prevItem.userGroupName.isEmpty();

            String prevIndex;
            if (prevIsGroup) {
                prevIndex = com.nanchen.wavesidebar.FirstLetterUtil.getFirstLetter(prevItem.userGroupName);
            } else {
                prevIndex = prevItem.getIndex();
            }

            if (!prevIndex.equals(currentIndex)) {
                indexTv.setVisibility(View.VISIBLE);
                indexTv.setText(currentIndex);
            } else {
                indexTv.setVisibility(View.GONE);
            }
        }
    }

    @NonNull
    @Override
    protected QuickViewHolder onCreateViewHolder(@NonNull Context context, @NonNull ViewGroup parent, int viewType) {
        return new QuickViewHolder(R.layout.caontact_new_user, parent);
    }
}

