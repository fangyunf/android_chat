package com.turunsi.yaoxin.main.mine.device;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.chad.library.adapter4.BaseQuickAdapter;
import com.chad.library.adapter4.viewholder.QuickViewHolder;
import com.turunsi.yaoxin.R;
import com.yaoxin.appbase.model.DeviceInfoBean;
import com.yaoxin.appbase.utils.DeviceUtils;
import com.yaoxin.appbase.utils.TimeUtil;

public class DeviceManageAdapter extends BaseQuickAdapter<DeviceInfoBean, QuickViewHolder> {

    // 定义点击回调接口
    public interface OnDeleteClickListener {
        void onDeleteClick(DeviceInfoBean deviceInfo, int position);
    }

    private OnDeleteClickListener onDeleteClickListener;

    // 设置点击监听器的方法
    public void setOnDeleteClickListener(OnDeleteClickListener listener) {
        this.onDeleteClickListener = listener;
    }

    @Override
    protected void onBindViewHolder(@NonNull QuickViewHolder quickViewHolder, int i, @Nullable DeviceInfoBean deviceInfoBean) {
        TextView tvTitle = quickViewHolder.getView(R.id.tvTitle);
        TextView tvLoginTime = quickViewHolder.getView(R.id.tvLoginTime);
        TextView tvDelete = quickViewHolder.getView(R.id.tvDelete);
        TextView tvJIzi = quickViewHolder.getView(R.id.tvJIzi);
        tvTitle.setText(deviceInfoBean.ua);
        if (deviceInfoBean.deviceId.equals(DeviceUtils.getDeviceId(tvLoginTime.getContext()))) {
            tvLoginTime.setText("当前在线");
            tvJIzi.setVisibility(View.VISIBLE);
            tvDelete.setVisibility(View.GONE);
        } else {
            tvJIzi.setVisibility(View.GONE);
            tvDelete.setVisibility(View.VISIBLE);
            tvLoginTime.setText("最近登录:" + TimeUtil.stampToDate(deviceInfoBean.updateTime + ""));
        }

//        if (getItemCount() > 1) {
//            tvDelete.setVisibility(View.VISIBLE);
//        } else {
//            tvDelete.setVisibility(View.GONE);
//        }

        tvDelete.setOnClickListener(view -> {
            if (onDeleteClickListener != null) {
                onDeleteClickListener.onDeleteClick(deviceInfoBean, i);
            }
        });
    }

    @NonNull
    @Override
    protected QuickViewHolder onCreateViewHolder(@NonNull Context context, @NonNull ViewGroup viewGroup, int i) {
        return new QuickViewHolder(R.layout.item_device_manage, viewGroup);
    }
}


