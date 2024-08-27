package com.turunsi.yaoxin.eggs;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentManager;

import com.netease.yunxin.kit.teamkit.ui.databinding.DialogTeamMaxMemberBinding;
import com.turunsi.yaoxin.databinding.DialogBugEggSucessBinding;
import com.yaoxin.appbase.fragment.BaseDialogFragment;

public class EggSuccessDialogFragment extends BaseDialogFragment implements View.OnClickListener {
    public interface EggSuccessDialogFragmentBlock {
        public void upGrade();
    }
    DialogBugEggSucessBinding binding;
    EggSuccessDialogFragmentBlock _block;
    String _title;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = DialogBugEggSucessBinding.inflate(inflater, container, false);
        binding.dialogBugEggSucessCancelTv.setOnClickListener(this);
        binding.dialogBugEggSucessConfrimTv.setOnClickListener(this);
        if (_title != null) {
            binding.dialogBugEggSucessTitleTv.setText(_title);
            binding.dialogBugEggSucessContentTv.setVisibility(View.GONE);
        }
        return binding.getRoot();
    }

    public static void showV(FragmentManager fragmentManager,EggSuccessDialogFragmentBlock block) {

        EggSuccessDialogFragment fragment = new EggSuccessDialogFragment();
        fragment._block = block;
        fragment.showNow(fragmentManager,"TeamMaxMemberDialogFragment");
    }
    public static void showTitle(FragmentManager fragmentManager,EggSuccessDialogFragmentBlock block,String title) {

        EggSuccessDialogFragment fragment = new EggSuccessDialogFragment();
        fragment._block = block;
        fragment._title = title;
        fragment.showNow(fragmentManager,"TeamMaxMemberDialogFragment");
    }
    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onClick(View v) {
         if (v == binding.dialogBugEggSucessCancelTv) {
            dismiss();
        } else if (binding.dialogBugEggSucessConfrimTv == v) {
             _block.upGrade();
             dismiss();
         }
    }
}
