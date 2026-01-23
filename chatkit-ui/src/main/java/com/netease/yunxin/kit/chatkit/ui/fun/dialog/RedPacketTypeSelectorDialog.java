package com.netease.yunxin.kit.chatkit.ui.fun.dialog;

import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.netease.yunxin.kit.chatkit.ui.R;

/**
 * 红包类型选择对话框
 */
public class RedPacketTypeSelectorDialog extends DialogFragment {

    public interface OnTypeSelectedListener {
        void onLuckyRedPacketSelected();
        void onExclusiveRedPacketSelected();
        void onCancel();
    }

    private OnTypeSelectedListener listener;

    public static RedPacketTypeSelectorDialog newInstance() {
        return new RedPacketTypeSelectorDialog();
    }

    public void setOnTypeSelectedListener(OnTypeSelectedListener listener) {
        this.listener = listener;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NO_TITLE, com.yaoxin.appbase.R.style.BottomDialog);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_red_packet_type_selector, container, false);
        
        TextView tvLucky = view.findViewById(R.id.tv_lucky_red_packet);
        TextView tvExclusive = view.findViewById(R.id.tv_exclusive_red_packet);
        TextView tvCancel = view.findViewById(R.id.tv_cancel);

        tvLucky.setOnClickListener(v -> {
            if (listener != null) {
                listener.onLuckyRedPacketSelected();
            }
            dismiss();
        });

        tvExclusive.setOnClickListener(v -> {
            if (listener != null) {
                listener.onExclusiveRedPacketSelected();
            }
            dismiss();
        });

        tvCancel.setOnClickListener(v -> {
            if (listener != null) {
                listener.onCancel();
            }
            dismiss();
        });

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        if (getDialog() != null && getDialog().getWindow() != null) {
            Window window = getDialog().getWindow();
            WindowManager.LayoutParams params = window.getAttributes();
            params.gravity = Gravity.BOTTOM;
            params.width = WindowManager.LayoutParams.MATCH_PARENT;
            params.height = WindowManager.LayoutParams.WRAP_CONTENT;
            window.setAttributes(params);
            window.setBackgroundDrawableResource(android.R.color.transparent);
            getDialog().setCanceledOnTouchOutside(true);
        }
    }
}
