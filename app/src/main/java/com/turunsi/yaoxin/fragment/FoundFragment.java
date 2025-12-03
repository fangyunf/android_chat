package com.turunsi.yaoxin.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.netease.yunxin.kit.common.utils.SizeUtils;
import com.turunsi.yaoxin.databinding.FragmentFoundBinding;
import com.turunsi.yaoxin.main.mine.dynamics.OfficialDynamicsActivity;
import com.yaoxin.appbase.fragment.BaseFragment;
import com.yaoxin.appbase.utils.BarUtils;
import com.yaoxin.appbase.utils.BaseEvent;
import com.yaoxin.appbase.utils.StatusBarUtils;

import org.greenrobot.eventbus.EventBus;

public class FoundFragment extends BaseFragment {
    private FragmentFoundBinding binding;
    private ActivityResultLauncher<Intent> launcher;

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        binding = FragmentFoundBinding.inflate(inflater);

        StatusBarUtils.setStatusBarLightMode(getActivity(), true, true);
        ViewGroup.MarginLayoutParams layoutParams = (ViewGroup.MarginLayoutParams) binding.contactNewFragmentTopLl.getLayoutParams();
        layoutParams.topMargin = BarUtils.getStatusBarHeight() + SizeUtils.dp2px(20);

        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        binding.tvDaymic.setOnClickListener(view2 -> {
            Intent intent = new Intent(getActivity(), OfficialDynamicsActivity.class);
            startActivity(intent);
        });

        binding.tvSao.setOnClickListener(view1 -> EventBus.getDefault().post(new BaseEvent("gotoScan")));
    }
}
