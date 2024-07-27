package com.turunsi.yaoxin.main.mine.purse.recharge;

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
import com.netease.yunxin.kit.teamkit.ui.fun.dialog.TeamModifyDialog;
import com.turunsi.yaoxin.databinding.FragmentChargeScanBinding;
import com.turunsi.yaoxin.databinding.FragmentOtherPlaceLoginBinding;
import com.turunsi.yaoxin.utils.IMUtil;
import com.yaoxin.appbase.fragment.BaseDialogFragment;
import com.yaoxin.appbase.model.NetData;
import com.yaoxin.appbase.model.RegisterBean;
import com.yaoxin.appbase.model.UserBean;
import com.yaoxin.appbase.net.CommonCallback;
import com.yaoxin.appbase.net.HttpUtil;
import com.yaoxin.appbase.utils.CommonNetUtil;
import com.yaoxin.appbase.utils.DataUtil;
import com.yaoxin.appbase.utils.GlideUtil;
import com.yaoxin.appbase.utils.ToastUtils;
import com.yaoxin.appbase.view.loginlib.utils.LoginLoader;
import com.yaoxin.appbase.view.loginlib.view.CountDownView;

import retrofit2.Call;
import retrofit2.Response;

public class RechargeScanFragment extends BaseDialogFragment implements View.OnClickListener {
    FragmentChargeScanBinding binding;
    String _title;
    String _imgUrl;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentChargeScanBinding.inflate(inflater, container, false);

        binding.fragmentChargeScanTv.setText(_title);
        GlideUtil.yh_loadImageRoundedCorner(getContext(),binding.fragmentChargeScanIv,_imgUrl,100);

        return binding.getRoot();
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onClick(View v) {

    }
    public static void showV(FragmentManager fragmentManager, String title, String imgUrl) {
        RechargeScanFragment fragment = new  RechargeScanFragment();
        fragment._title = title;
        fragment._imgUrl = imgUrl;
        fragment.showNow(fragmentManager,"RechargeScanFragment");
    }
}
