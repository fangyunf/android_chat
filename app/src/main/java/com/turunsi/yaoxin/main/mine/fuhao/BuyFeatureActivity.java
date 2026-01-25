package com.turunsi.yaoxin.main.mine.fuhao;

import android.os.Bundle;
import android.view.Gravity;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.GridLayoutManager;

import com.chad.library.adapter4.BaseQuickAdapter;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.netease.yunxin.kit.common.utils.SizeUtils;
import com.turunsi.yaoxin.R;
import com.turunsi.yaoxin.databinding.ActivityBuyFeatureBinding;
import com.turunsi.yaoxin.databinding.ActivityMineMyFuhaoListBinding;
import com.turunsi.yaoxin.main.mine.fuhao.adapter.GroupBuyListAdapter;
import com.turunsi.yaoxin.main.mine.fuhao.adapter.MyFuHaoListAdapter;
import com.yaoxin.appbase.activity.BaseActivity;
import com.yaoxin.appbase.model.GroupInfoBean;
import com.yaoxin.appbase.model.NetData;
import com.yaoxin.appbase.model.RegisterBean;
import com.yaoxin.appbase.model.UserBean;
import com.yaoxin.appbase.net.CommonCallback;
import com.yaoxin.appbase.net.HttpUtil;
import com.yaoxin.appbase.pswkeyboard.OnPasswordInputFinish;
import com.yaoxin.appbase.pswkeyboard.widget.PopEnterPassword;
import com.yaoxin.appbase.utils.BaseEvent;
import com.yaoxin.appbase.utils.DataUtil;
import com.yaoxin.appbase.utils.ToastUtils;
import com.yaoxin.appbase.view.CommonGridSpacingItemDecoration;
import com.yaoxin.appbase.view.LoadingDialog;

import org.greenrobot.eventbus.EventBus;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Response;

public class BuyFeatureActivity extends BaseActivity implements View.OnClickListener {
    ActivityBuyFeatureBinding binding;
    MyFuHaoListAdapter adapter = new MyFuHaoListAdapter();
    ArrayList<UserBean> userBeanList = new ArrayList<>();

    int _type = 0;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityBuyFeatureBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        binding.activityBuyFeatureNav.addCloseImageButton().setOnClickListener(this);
        binding.activityBuyFeatureBuyTv.setOnClickListener(this);
        binding.activityBuyFeatureConfrimTv.setOnClickListener(this);
        _updateUI();
    }


    void _updateUI() {
        binding.activityBuyFeatureNav.getTitleView().setText("购买副号");
        binding.activityBuyFeatureMoneyTv.setText("￥188");
        binding.activityBuyFeatureDetailTv.setText("购买即得15个副号");

//        GridLayoutManager gridLayoutManager = new GridLayoutManager(this, 3);
//        binding.activityBuyFeatureRv.setLayoutManager(gridLayoutManager);
//        CommonGridSpacingItemDecoration gridSpacingItemDecoration =
//                new CommonGridSpacingItemDecoration(3, SizeUtils.dp2px(10), false);
//        binding.activityBuyFeatureRv.addItemDecoration(gridSpacingItemDecoration);
//        binding.activityBuyFeatureRv.setAdapter(adapter);
//
//        for (int i = 0; i < 20; i++) {
//            UserBean bean = new UserBean();
//            bean.phone = "1234444444";
//            userBeanList.add(bean);
//        }
//
//        adapter.setItems(userBeanList);
    }

    @Override
    protected void _requestData() {
//        HttpUtil.apiW().group_groupGrade()
//                .enqueue(new CommonCallback<NetData>() {
//                    @Override
//                    public void Successful(Call<NetData> call, Response<NetData> response, NetData body) {
//                        Type type = new TypeToken<List<GroupInfoBean>>() {}.getType();
//                        List<GroupInfoBean> tempList = new Gson().fromJson(body.data.toString(), type);
//
//                    }
//
//                    @Override
//                    public void Failure(Call<NetData> call, Throwable t) {
//
//                    }
//                });
    }

    @Override
    public void onClick(View v) {
        if (v == binding.activityBuyFeatureNav.addCloseImageButton()) {
            finish();
        } else if (v == binding.activityBuyFeatureConfrimTv || v == binding.activityBuyFeatureBuyTv) {
            String phone = getTextStr(binding.activityBuyFeatureEt);
            if (phone.length() != 8) {
                ToastUtils.toastMsg("请输入8位");
                return;
            }

//            String smsPhone = binding.etPhone.getText().toString();
//            if (smsPhone.length() != 6) {
//                ToastUtils.toastMsg("请输入6位的自定义验证码");
//                return;
//            }

            PopEnterPassword popEnterPassword = new PopEnterPassword(this, new OnPasswordInputFinish() {
                @Override
                public void inputFinish(String password) {
                    RegisterBean registerBean = new RegisterBean();
                    // phone: 副号手机号前8位（用户输入5位，加上"1"是6位，再补2位到8位）
                    registerBean.phone = phone; // 确保是8位
                    // toPhone: 主号手机号
                    registerBean.userId = DataUtil.getUserInfo().userId;
                    //registerBean.toPhone = DataUtil.getUserInfo().phoneNo;
                    registerBean.password = password;
                    //registerBean.smsPhone = smsPhone;
                    LoadingDialog.showDialog(getSupportFragmentManager(), "购买中..");
                    HttpUtil.apiW().subUser_createSubUser(registerBean).enqueue(new CommonCallback<NetData>() {
                        @Override
                        public void Successful(Call<NetData> call, Response<NetData> response, NetData body) {
                            ToastUtils.toastMsg("购买成功");
                            EventBus.getDefault().post(new BaseEvent("reload_fuhao"));
                            finish();
                        }

                        @Override
                        public void Failure(Call<NetData> call, Throwable t) {

                        }

                        @Override
                        public void end() {
                            super.end();
                            LoadingDialog.dismissDialog();
                        }
                    });
                }
            }, "188");
            // 显示窗口
            popEnterPassword.showAtLocation(binding.activityBuyFeatureRootRl, Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL, 0, 0); // 设置layout在PopupWindow中显示的位置
        }
    }

}
