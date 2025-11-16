package com.yaoxin.appbase.view;

import android.app.Dialog;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentManager;

import com.yaoxin.appbase.R;

/**
 * 更多功能底部对话框
 */
public class MoreFunctionsBottomDialog extends DialogFragment {

    private OnFunctionClickListener listener;
    private int badgeCount = 0; // 角标数量，默认0表示隐藏

    public interface OnFunctionClickListener {
        /**
         * 功能点击回调
         * @param functionType 功能类型：1-建群聊，2-加好友，3-扫一扫
         */
        void onFunctionClick(int functionType);
    }

    public MoreFunctionsBottomDialog setOnFunctionClickListener(OnFunctionClickListener listener) {
        this.listener = listener;
        return this;
    }

    /**
     * 设置扫一扫角标数量
     * @param count 角标数量，0或负数表示隐藏
     */
    public MoreFunctionsBottomDialog setBadgeCount(int count) {
        this.badgeCount = count;
        return this;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NO_TITLE, R.style.BottomDialog);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_more_functions, container, false);
        initView(view);
        return view;
    }

    private void initView(View view) {
        // 建群聊
        LinearLayout llCreateGroup = view.findViewById(R.id.ll_create_group);
        llCreateGroup.setOnClickListener(v -> {
            if (listener != null) {
                listener.onFunctionClick(1);
            }
            dismiss();
        });

        // 加好友
        LinearLayout llAddFriend = view.findViewById(R.id.ll_add_friend);
        llAddFriend.setOnClickListener(v -> {
            if (listener != null) {
                listener.onFunctionClick(2);
            }
            dismiss();
        });

        // 扫一扫
        LinearLayout llScan = view.findViewById(R.id.ll_scan);
        llScan.setOnClickListener(v -> {
            if (listener != null) {
                listener.onFunctionClick(3);
            }
            dismiss();
        });

        // 角标
        TextView tvBadge = view.findViewById(R.id.tv_badge);
        if (badgeCount > 0) {
            tvBadge.setVisibility(View.VISIBLE);
            tvBadge.setText(String.valueOf(badgeCount));
        } else {
            tvBadge.setVisibility(View.GONE);
        }

        // 关闭按钮
        ImageView ivClose = view.findViewById(R.id.iv_close);
        ivClose.setOnClickListener(v -> dismiss());
    }

    @Override
    public void onStart() {
        super.onStart();
        Dialog dialog = getDialog();
        if (dialog != null) {
            Window window = dialog.getWindow();
            if (window != null) {
                WindowManager.LayoutParams params = window.getAttributes();
                params.width = WindowManager.LayoutParams.MATCH_PARENT;
                params.height = WindowManager.LayoutParams.WRAP_CONTENT;
                params.gravity = Gravity.BOTTOM;
                params.dimAmount = 0.5f;
                window.setAttributes(params);
                window.setWindowAnimations(R.style.BottomDialog_AnimationStyle);
            }
            dialog.setCanceledOnTouchOutside(true);
            dialog.setCancelable(true);
        }
    }

    /**
     * 显示对话框
     */
    public void show(FragmentManager fragmentManager) {
        if (fragmentManager != null && !isAdded()) {
            show(fragmentManager, "MoreFunctionsBottomDialog");
        }
    }
}

