package com.yaoxin.appbase.fragment;

import android.os.Bundle;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDialogFragment;
import androidx.fragment.app.DialogFragment;

import com.yaoxin.appbase.R;

public class BaseDialogFragment extends AppCompatDialogFragment {


    public String getTextStr(TextView tv) {
        return tv.getText().toString().trim();
    }

    protected static void showV() {
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NO_FRAME, R.style.custom_dlg);
    }
}
