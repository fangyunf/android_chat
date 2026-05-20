package com.yaoxin.appbase.dialog;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.Gravity;
import android.view.Window;
import android.view.WindowManager;

import androidx.annotation.NonNull;

import com.yaoxin.appbase.R;

public class GroupEditActionDialog extends Dialog {
    private OnActionListener listener;

    public interface OnActionListener {
        void onEditName();

        void onEditNotice();

        void onEditAvatar();
    }

    public GroupEditActionDialog(@NonNull Context context, OnActionListener listener) {
        super(context, R.style.BottomDialog);
        this.listener = listener;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_group_edit_action);
        setCancelable(true);
        Window window = getWindow();
        if (window != null) {
            window.setGravity(Gravity.BOTTOM);
            window.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);
            window.setBackgroundDrawableResource(android.R.color.transparent);
        }
        findViewById(R.id.tvCancel).setOnClickListener(v -> dismiss());
        findViewById(R.id.tvEditName).setOnClickListener(v -> {
            if (listener != null) listener.onEditName();
            dismiss();
        });
        findViewById(R.id.tvEditNotice).setOnClickListener(v -> {
            if (listener != null) listener.onEditNotice();
            dismiss();
        });
        findViewById(R.id.tvEditAvatar).setOnClickListener(v -> {
            if (listener != null) listener.onEditAvatar();
            dismiss();
        });
    }
} 