package com.turunsi.yaoxin.main.mine.fuhao.adapter;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.chad.library.adapter4.BaseQuickAdapter;
import com.chad.library.adapter4.viewholder.QuickViewHolder;
import com.makeramen.roundedimageview.RoundedImageView;
import com.turunsi.yaoxin.R;
import com.yaoxin.appbase.model.SubUserBean;
import com.yaoxin.appbase.utils.GlideUtil;
import com.yaoxin.appbase.utils.ToastUtils;

public class MyFuHaoListAdapter extends BaseQuickAdapter<SubUserBean, QuickViewHolder> {

    @Override
    protected void onBindViewHolder(@NonNull QuickViewHolder quickViewHolder, int i, @Nullable SubUserBean subUserBean) {
        // 头像
        RoundedImageView avatarIv = quickViewHolder.getView(R.id.item_mine_fuhao_list_cell_avatar_iv);
        GlideUtil.yh_loadImageRoundedCorner(quickViewHolder.itemView.getContext(), avatarIv, subUserBean.avatar, 2);

        // 名称
        TextView nameTv = quickViewHolder.getView(R.id.item_mine_fuhao_list_cell_name_tv);
        nameTv.setText(subUserBean.name != null ? subUserBean.name : "");

        // ID
        TextView idTv = quickViewHolder.getView(R.id.item_mine_fuhao_list_cell_id_tv);
        idTv.setText(subUserBean.phone);

        // 复制按钮
        TextView copyTv = quickViewHolder.getView(R.id.item_mine_fuhao_list_cell_copy_tv);
        copyTv.setOnClickListener(v -> {
            // 复制副号ID到剪贴板
            if (subUserBean.subId != null && !subUserBean.subId.isEmpty()) {
                ClipboardManager clipboard = (ClipboardManager) quickViewHolder.itemView.getContext()
                        .getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText("副号电话", subUserBean.phone);
                clipboard.setPrimaryClip(clip);
                ToastUtils.toastMsg("已复制");
            }
        });
    }

    @NonNull
    @Override
    protected QuickViewHolder onCreateViewHolder(@NonNull Context context, @NonNull ViewGroup viewGroup, int i) {
        return new QuickViewHolder(R.layout.item_mine_fuhao_list_cell, viewGroup);
    }
}

