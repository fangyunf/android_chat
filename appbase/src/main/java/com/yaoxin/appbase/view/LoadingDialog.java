package com.yaoxin.appbase.view;


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
import androidx.fragment.app.FragmentManager;


import com.yaoxin.appbase.R;

import java.util.Objects;

/**
 * LoadingDialog
 */

public class LoadingDialog extends DialogFragment {

    private static LoadingDialog sDialog;
    private static String mMsg;

    public static void showDialog(FragmentManager fragmentManager, String msg) {
        if (sDialog == null) {
            sDialog = new LoadingDialog();
        }
        mMsg = msg;
        if (!sDialog.isAdded()) {
            sDialog.showNow(fragmentManager, null);
        }
    }

    public static void dismissDialog() {
        if (sDialog == null || !sDialog.isAdded()) {
            return;
        }
        sDialog.dismissAllowingStateLoss();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NO_FRAME, R.style.custom_dlg);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.dialog_loading, container);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Objects.requireNonNull(getDialog()).setCanceledOnTouchOutside(false);
        TextView mTvMsg = view.findViewById(R.id.tv_msg);
        mTvMsg.setText(mMsg);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Window window = Objects.requireNonNull(getDialog()).getWindow();
        if (window != null) {
            WindowManager.LayoutParams layoutParams = window.getAttributes();
            layoutParams.width = WindowManager.LayoutParams.WRAP_CONTENT;
            layoutParams.height = WindowManager.LayoutParams.WRAP_CONTENT;
            layoutParams.gravity = Gravity.CENTER;
        }
    }
}
