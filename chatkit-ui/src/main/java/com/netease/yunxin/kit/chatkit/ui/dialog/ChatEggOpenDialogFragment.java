package com.netease.yunxin.kit.chatkit.ui.dialog;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentManager;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.load.resource.gif.GifDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.google.gson.Gson;
import com.netease.yunxin.kit.chatkit.ui.R;
import com.netease.yunxin.kit.chatkit.ui.databinding.DialogChatEggOpenBinding;
import com.yaoxin.appbase.fragment.BaseDialogFragment;
import com.yaoxin.appbase.model.CustomMsgBean;
import com.yaoxin.appbase.model.NetData;
import com.yaoxin.appbase.model.RegisterBean;
import com.yaoxin.appbase.net.CommonCallback;
import com.yaoxin.appbase.net.HttpUtil;
import com.yaoxin.appbase.utils.DataUtil;
import com.yaoxin.appbase.utils.GlideUtil;
import com.yaoxin.appbase.utils.NumberUtil;
import com.yaoxin.appbase.utils.ToastUtils;

import retrofit2.Call;
import retrofit2.Response;

public class ChatEggOpenDialogFragment extends BaseDialogFragment implements View.OnClickListener {
    DialogChatEggOpenBinding binding;
    CustomMsgBean _bean;
    double _currentProgress = 0.00;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = DialogChatEggOpenBinding.inflate(inflater, container, false);
        if ("1".equals(DataUtil.getUserInfo().hy)) {
            binding.dialogChatEggOpenOptTv.setText("加成砸蛋+2");
            binding.dialogChatEggOpenDescTv.setText("砸中即可获得"+ NumberUtil.formartMoney_zhengshu(_bean.amount) +"元余额");
        } else {
            binding.dialogChatEggOpenOptTv.setText("砸蛋+1");
            binding.dialogChatEggOpenDescTv.setText("砸中即可获得"+ NumberUtil.formartMoney_zhengshu(_bean.amount) +"元余额\n开通VIP更有机会砸中彩蛋哦");
        }
        binding.dialogChatEggOpenOptTv.setOnClickListener(this);
        binding.dialogChatEggOpenCloseIv.setOnClickListener(this);
        Glide.with(this)
                .asGif()
                .load(R.drawable.zadan_chuizi) // 替换为你的本地GIF文件名
                .into(binding.dialogChatEggOpenEggIv);
        // 加载本地GIF文件
        Glide.with(this)
                .asGif()
                .load(R.drawable.zadan_dongxiao) // 替换为你的本地GIF文件名
                .into(binding.dialogChatEggOpenGifIv);
        _requestData();
        return binding.getRoot();
    }

    void _requestData() {
        RegisterBean registerBean = new RegisterBean();
        registerBean.fafId = _bean.fafId;
        HttpUtil.apiW().caidan_caidaning(registerBean)
                .enqueue(new CommonCallback<NetData>() {
                    @Override
                    public void Successful(Call<NetData> call, Response<NetData> response, NetData body) {
                        CustomMsgBean tempBean = new Gson().fromJson(body.data.toString(), CustomMsgBean.class);
                        int value = (int) Math.round(Double.parseDouble(tempBean.groupLs) / Double.parseDouble(tempBean.caidanLs) * 100 * 100);
                        double doubleValue = (double) value / 100;
                        binding.dialogChatEggOpenPb6.setProgress((int) doubleValue);
                        binding.dialogChatEggOpenProgressTv.setText(doubleValue + "%");
                    }

                    @Override
                    public void Failure(Call<NetData> call, Throwable t) {

                    }
                });
    }

    public static void showV(FragmentManager fragmentManager, CustomMsgBean bean) {

        ChatEggOpenDialogFragment fragment = new ChatEggOpenDialogFragment();
        fragment._bean = bean;
        fragment.showNow(fragmentManager,"ChatEggOpenDialogFragment");
    }


    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onClick(View v) {
         if (v == binding.dialogChatEggOpenOptTv) {
//             if ("1".equals(DataUtil.getUserInfo().hy)) {
//                 _currentProgress += 0.2;
//             } else {
//                 _currentProgress += 0.1;
//             }
//             if (_currentProgress >= 100) {
//                 _currentProgress = 99.99;
//             }
//             binding.dialogChatEggOpenProgressTv.setText(String.format("%.2f", _currentProgress) + "%");
//             binding.dialogChatEggOpenPb6.setProgress((int)(_currentProgress));

//            dismiss();
        }
        else if (binding.dialogChatEggOpenCloseIv == v) {
             dismiss();
         }
    }
}
