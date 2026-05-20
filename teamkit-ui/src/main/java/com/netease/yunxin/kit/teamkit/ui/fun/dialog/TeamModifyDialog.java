package com.netease.yunxin.kit.teamkit.ui.fun.dialog;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentManager;

import com.netease.yunxin.kit.teamkit.ui.databinding.FunTeamModifyDialogBinding;
import com.yaoxin.appbase.fragment.BaseDialogFragment;
import com.yaoxin.appbase.fragment.BaseFragment;
import com.yaoxin.appbase.utils.ToastUtils;

public class TeamModifyDialog extends BaseDialogFragment implements View.OnClickListener {
    @Override
    public void onClick(View v) {
        if (v == binding.funTeamModifyDialogCloseIcon) {
            dismiss();

        }
        if (v == binding.funTeamModifyDialogConfirmTv) {
            String textStr = getTextStr(binding.funTeamModifyDialogEt);
            if (textStr.isEmpty()) {
                ToastUtils.toastMsg("请输入内容");
                return;
            }
            if (_block != null) {
                _block.returnResult(textStr);
                dismiss();
            }
        }
    }

    public interface TeamModifyDialogBlock {
        public void returnResult(String result);
    }
    FunTeamModifyDialogBinding binding;
    TeamModifyDialogBlock _block;
    int _type = 0;

    String _defaultText;
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FunTeamModifyDialogBinding.inflate(inflater, container, false);
        if (_type == 1) {
            binding.funTeamModifyDialogTitleTv.setText("修改群名称");
        }
        if (_type == 2) {
            binding.funTeamModifyDialogTitleTv.setText("修改公告栏");

        }
        if (_type == 3) {
            binding.funTeamModifyDialogTitleTv.setText("修改我的群昵称");

        }
        binding.funTeamModifyDialogConfirmTv.setOnClickListener(this);
        binding.funTeamModifyDialogCloseIcon.setOnClickListener(this);
        binding.funTeamModifyDialogEt.setText(_defaultText);
        return binding.getRoot();
    }
    public static void showV(FragmentManager fragmentManager,int type, TeamModifyDialogBlock block, String defaultText) {
        TeamModifyDialog fragment = new  TeamModifyDialog();
        fragment._type = type;
        fragment._block = block;
        fragment._defaultText = defaultText;
        fragment.showNow(fragmentManager,"TeamModifyDialog");
    }


}
