package com.turunsi.yaoxin.main.mine.collection.adapter;

import android.content.Context;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.chad.library.adapter4.BaseQuickAdapter;
import com.chad.library.adapter4.viewholder.QuickViewHolder;
import com.netease.nimlib.sdk.msg.model.CollectInfo;
import com.turunsi.yaoxin.R;
import com.turunsi.yaoxin.main.mine.collection.CollectionListHelper;
import com.yaoxin.appbase.utils.GlideUtil;

public class CollectionListAdapter extends BaseQuickAdapter<CollectInfo, QuickViewHolder> {

    @Override
    protected void onBindViewHolder(
            @NonNull QuickViewHolder holder, int position, @Nullable CollectInfo item) {
        if (item == null) {
            return;
        }
        String contentType = CollectionListHelper.getContentType(item);
        String displayData = CollectionListHelper.getDisplayData(item);

        holder.setText(R.id.item_collection_list_cell_user_tv, CollectionListHelper.getSourceName(item));
        holder.setText(
                R.id.item_collection_list_cell_date_tv, CollectionListHelper.formatCollectDate(item.getCreateTime()));

        FrameLayout mediaFl = holder.findView(R.id.item_collection_list_cell_media_fl);
        ImageView imageIv = holder.findView(R.id.item_collection_list_cell_content_iv);
        ImageView playIv = holder.findView(R.id.item_collection_list_cell_play_iv);
        LinearLayout textLl = holder.findView(R.id.item_collection_list_cell_text_ll);
        LinearLayout fileLl = holder.findView(R.id.item_collection_list_cell_file_ll);
        TextView contentTv = holder.findView(R.id.item_collection_list_cell_content_tv);
        TextView fileNameTv = holder.findView(R.id.item_collection_list_cell_file_name_tv);

        if (mediaFl == null || textLl == null || fileLl == null) {
            return;
        }

        mediaFl.setVisibility(View.GONE);
        textLl.setVisibility(View.GONE);
        fileLl.setVisibility(View.GONE);
        if (playIv != null) {
            playIv.setVisibility(View.GONE);
        }

        switch (contentType) {
            case CollectionListHelper.CONTENT_IMAGE:
            case CollectionListHelper.CONTENT_VIDEO:
                mediaFl.setVisibility(View.VISIBLE);
                if (imageIv != null) {
                    GlideUtil.yh_loadImage(getContext(), imageIv, displayData);
                }
                if (playIv != null
                        && CollectionListHelper.CONTENT_VIDEO.equals(contentType)) {
                    playIv.setVisibility(View.VISIBLE);
                }
                break;
            case CollectionListHelper.CONTENT_FILE:
                fileLl.setVisibility(View.VISIBLE);
                if (fileNameTv != null) {
                    fileNameTv.setText(
                            TextUtils.isEmpty(displayData) ? "文件" : displayData);
                }
                break;
            default:
                textLl.setVisibility(View.VISIBLE);
                if (contentTv != null) {
                    contentTv.setText(displayData);
                }
                break;
        }
    }

    @NonNull
    @Override
    protected QuickViewHolder onCreateViewHolder(
            @NonNull Context context, @NonNull ViewGroup parent, int viewType) {
        return new QuickViewHolder(R.layout.item_collection_list_cell, parent);
    }
}
