package com.netease.yunxin.kit.chatkit.ui.dialog;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentManager;

import com.netease.yunxin.kit.chatkit.ui.databinding.DialogChatEggOpenBinding;
import com.yaoxin.appbase.fragment.BaseDialogFragment;

public class ChatEggOpenDialogFragment extends BaseDialogFragment implements View.OnClickListener {
    public interface ChatEggOpenDialogFragmentBlock {
        public void upGrade();
    }
    DialogChatEggOpenBinding binding;
    ChatEggOpenDialogFragmentBlock _block;
    String _title;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = DialogChatEggOpenBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    public static void showV(FragmentManager fragmentManager,ChatEggOpenDialogFragmentBlock block) {

        ChatEggOpenDialogFragment fragment = new ChatEggOpenDialogFragment();
        fragment._block = block;
        fragment.showNow(fragmentManager,"TeamMaxMemberDialogFragment");
    }


    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onClick(View v) {
//         if (v == binding.dialogBugEggSucessCancelTv) {
//            dismiss();
//        } else if (binding.dialogBugEggSucessConfrimTv == v) {
//             _block.upGrade();
//         }
    }
}
