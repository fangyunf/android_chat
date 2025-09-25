package com.turunsi.yaoxin.main.mine.huiyuan;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.turunsi.yaoxin.R;
import com.yaoxin.appbase.fragment.BaseDialogFragment;
import com.yaoxin.appbase.utils.ToastUtils;

public class ExchangeLiangHaoDialogFragment extends BaseDialogFragment implements View.OnClickListener {

    public interface OnExchangeListener {
        void onExchange(String code, String level);
    }

    private OnExchangeListener listener;
    private EditText etCode;
    private EditText etLevel;

    public ExchangeLiangHaoDialogFragment setOnExchangeListener(OnExchangeListener l) {
        this.listener = l;
        return this;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_exchange_lianghao, container, false);
        etCode = view.findViewById(R.id.et_code);
        etLevel = view.findViewById(R.id.et_level);
        TextView btnCancel = view.findViewById(R.id.tv_cancel);
        TextView btnConfirm = view.findViewById(R.id.tv_confirm);
        btnCancel.setOnClickListener(this);
        btnConfirm.setOnClickListener(this);
        return view;
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.tv_cancel) {
            dismiss();
        } else if (id == R.id.tv_confirm) {
            String code = etCode.getText().toString().trim();
            String level = etLevel.getText().toString().trim();
            if (code.isEmpty()) {
                ToastUtils.toastMsg("请输入兑换码");
                return;
            }
            if (level.isEmpty()) {
                ToastUtils.toastMsg("请输入靓号等级");
                return;
            }
            if (listener != null) {
                listener.onExchange(code, level);
            }
            dismiss();
        }
    }
}


