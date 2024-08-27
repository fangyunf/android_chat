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
import com.netease.yunxin.kit.chatkit.ui.R;
import com.netease.yunxin.kit.chatkit.ui.databinding.DialogChatEggOpenBinding;
import com.yaoxin.appbase.fragment.BaseDialogFragment;
import com.yaoxin.appbase.model.CustomMsgBean;
import com.yaoxin.appbase.utils.DataUtil;
import com.yaoxin.appbase.utils.GlideUtil;
import com.yaoxin.appbase.utils.NumberUtil;
import com.yaoxin.appbase.utils.ToastUtils;

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
        Glide.with(this)
                .asGif()
                .load(R.drawable.zadan_chuizi) // 替换为你的本地GIF文件名
                .into(binding.dialogChatEggOpenEggIv);
        // 加载本地GIF文件

        return binding.getRoot();
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
             if ("1".equals(DataUtil.getUserInfo().hy)) {
                 _currentProgress += 0.2;
             } else {
                 _currentProgress += 0.1;
             }
             if (_currentProgress >= 100) {
                 _currentProgress = 99.99;
             }
             binding.dialogChatEggOpenProgressTv.setText(String.format("%.2f", _currentProgress) + "%");
             binding.dialogChatEggOpenPb6.setProgress((int)(_currentProgress));
             Glide.with(this)
                     .asGif()
                     .load(R.drawable.zadan_dongxiao) // 替换为你的本地GIF文件名
                     .listener(new RequestListener<GifDrawable>() {
                         @Override
                         public boolean onLoadFailed(@Nullable GlideException e, Object o, Target<GifDrawable> target, boolean b) {
                             return false;
                         }

                         @Override
                         public boolean onResourceReady(GifDrawable resource, Object model, Target<GifDrawable> target, DataSource dataSource, boolean isFirstResource) {
                             resource.setLoopCount(1); // 设置只播放一次
                             return false;
                         }
                     })

                     .into(binding.dialogChatEggOpenGifIv);
//            dismiss();
        }
//        else if (binding.dialogBugEggSucessConfrimTv == v) {
//             _block.upGrade();
//         }
    }
}
