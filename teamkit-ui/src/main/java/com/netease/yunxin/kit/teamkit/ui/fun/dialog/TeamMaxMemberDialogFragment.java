package com.netease.yunxin.kit.teamkit.ui.fun.dialog;

import android.app.Activity;
import android.os.Bundle;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentManager;

import com.google.gson.Gson;
import com.netease.yunxin.kit.teamkit.ui.databinding.DialogTeamMaxMemberBinding;
import com.yaoxin.appbase.fragment.BaseDialogFragment;
import com.yaoxin.appbase.model.NetData;
import com.yaoxin.appbase.model.RegisterBean;
import com.yaoxin.appbase.model.UserBean;
import com.yaoxin.appbase.net.CommonCallback;
import com.yaoxin.appbase.net.HttpUtil;
import com.yaoxin.appbase.utils.CommonNetUtil;
import com.yaoxin.appbase.utils.DataUtil;
import com.yaoxin.appbase.utils.ToastUtils;
import com.yaoxin.appbase.view.loginlib.utils.LoginLoader;
import com.yaoxin.appbase.view.loginlib.view.CountDownView;

import retrofit2.Call;
import retrofit2.Response;

public class TeamMaxMemberDialogFragment extends BaseDialogFragment implements View.OnClickListener {
    public interface TeamMaxMemberDialogFragmentBlock {
        public void upGrade();
    }
    DialogTeamMaxMemberBinding binding;
    TeamMaxMemberDialogFragmentBlock _block;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = DialogTeamMaxMemberBinding.inflate(inflater, container, false);
        binding.dialogTeamMaxMemberDontUpgradeTv.setOnClickListener(this);
        binding.dialogTeamMaxMemberUpgradeTv.setOnClickListener(this);
        return binding.getRoot();
    }

    public static void showV(FragmentManager fragmentManager,TeamMaxMemberDialogFragmentBlock block) {

        TeamMaxMemberDialogFragment fragment = new  TeamMaxMemberDialogFragment();
        fragment._block = block;
        fragment.showNow(fragmentManager,"TeamMaxMemberDialogFragment");
    }
    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onClick(View v) {
         if (v == binding.dialogTeamMaxMemberDontUpgradeTv) {
            dismiss();
        } else if (binding.dialogTeamMaxMemberUpgradeTv == v) {
             _block.upGrade();
         }
    }
}
