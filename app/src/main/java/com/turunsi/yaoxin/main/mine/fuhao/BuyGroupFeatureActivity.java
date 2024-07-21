package com.turunsi.yaoxin.main.mine.fuhao;

import android.os.Bundle;
import android.view.Gravity;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.chad.library.adapter4.BaseQuickAdapter;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.turunsi.yaoxin.R;
import com.turunsi.yaoxin.databinding.ActivityBuyFeatureBinding;
import com.turunsi.yaoxin.databinding.ActivityBuyGroupFeatureBinding;
import com.turunsi.yaoxin.main.mine.fuhao.adapter.GroupBuyListAdapter;
import com.yaoxin.appbase.activity.BaseActivity;
import com.yaoxin.appbase.model.GroupInfoBean;
import com.yaoxin.appbase.model.NetData;
import com.yaoxin.appbase.model.RegisterBean;
import com.yaoxin.appbase.net.CommonCallback;
import com.yaoxin.appbase.net.HttpUtil;
import com.yaoxin.appbase.pswkeyboard.OnPasswordInputFinish;
import com.yaoxin.appbase.pswkeyboard.widget.PopEnterPassword;
import com.yaoxin.appbase.utils.NumberUtil;
import com.yaoxin.appbase.utils.ToastUtils;

import java.lang.reflect.Type;
import java.util.List;

import retrofit2.Call;
import retrofit2.Response;

public class BuyGroupFeatureActivity extends BaseActivity implements View.OnClickListener {
    ActivityBuyGroupFeatureBinding binding;

    GroupBuyListAdapter adapter = new GroupBuyListAdapter();
    int _type = 0;
    String _groupId;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        _type = getIntent().getIntExtra("type",0);
        _groupId = getIntent().getStringExtra("groupId");
        binding = ActivityBuyGroupFeatureBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        binding.activityBuyGroupFeatureNav.addCloseImageButton().setOnClickListener(this);
        binding.activityBuyGroupFeatureQsjRuleTv.setOnClickListener(this);
        binding.activityBuyGroupFeatureBuyTv.setOnClickListener(this);

        binding.activityBuyGroupFeatureRv.setLayoutManager(new LinearLayoutManager(this));
        binding.activityBuyGroupFeatureRv.setAdapter(adapter);
        adapter.setOnItemClickListener(new BaseQuickAdapter.OnItemClickListener<GroupInfoBean>() {
            @Override
            public void onClick(@NonNull BaseQuickAdapter<GroupInfoBean, ?> baseQuickAdapter, @NonNull View view, int i) {
                for (GroupInfoBean tempBean:
                     baseQuickAdapter.getItems()) {
                    tempBean.isSelected = false;
                }
                baseQuickAdapter.getItem(i).isSelected = true;
                adapter.notifyDataSetChanged();
            }
        });
        _updateUI();
    }



    void _updateUI() {

//        if (_type == 0) {
//            binding.activityBuyFeatureNav.getTitleView().setText("购买副号");
//            binding.activityBuyFeatureMoneyTv.setText("￥68");
//            binding.activityBuyFeatureDetailTv.setText("购买即得20个副号");
//            binding.activityBuyFeatureShuomingTv.setVisibility(View.GONE);
//            binding.activityBuyFeatureIv.setImageResource(R.mipmap.buy_feature_fuhao);
//        }
//        if (_type == 1) {
//            binding.activityBuyFeatureBottomLl.setVisibility(View.VISIBLE);
//            binding.activityBuyFeatureIvRl.setVisibility(View.VISIBLE);
//            binding.activityBuyFeatureNav.getTitleView().setText("升级群组");
//            binding.activityBuyFeatureMoneyTv.setText("￥88");
//            binding.activityBuyFeatureDetailTv.setText("购买即升级当前群组为1000人群");
//            binding.activityBuyFeatureShuomingTv.setVisibility(View.VISIBLE);
//            binding.activityBuyFeatureIv.setImageResource(R.mipmap.buy_feature_group);
//            binding.activityBuyFeatureUpdateInfoTv.setVisibility(View.GONE);
//        }
        if (_type == 2) {
            binding.activityBuyGroupFeatureInfoTv.setVisibility(View.VISIBLE);
            binding.activityBuyFeatureBottomLl.setVisibility(View.GONE);
            binding.activityBuyGroupFeatureRv.setVisibility(View.GONE);
            binding.activityBuyGroupFeatureNav.getTitleView().setText("升级规则");
        } else {
            binding.activityBuyFeatureBottomLl.setVisibility(View.VISIBLE);
            binding.activityBuyGroupFeatureRv.setVisibility(View.VISIBLE);
            binding.activityBuyGroupFeatureNav.getTitleView().setText("升级群组");
            binding.activityBuyGroupFeatureQsjRuleTv.setVisibility(View.VISIBLE);
            binding.activityBuyGroupFeatureInfoTv.setVisibility(View.GONE);
        }
    }
    @Override
    protected void _requestData() {
        HttpUtil.apiW().group_groupGrade()
                .enqueue(new CommonCallback<NetData>() {
                    @Override
                    public void Successful(Call<NetData> call, Response<NetData> response, NetData body) {
                        Type type = new TypeToken<List<GroupInfoBean>>() {}.getType();
                        List<GroupInfoBean> tempList = new Gson().fromJson(body.data.toString(), type);
//                        if (!tempList.isEmpty()) {
//                            tempList.get(0).isSelected = true;
//                        }
                        adapter.setItems(tempList);
                        adapter.notifyDataSetChanged();
                    }

                    @Override
                    public void Failure(Call<NetData> call, Throwable t) {

                    }
                });
    }

    @Override
    public void onClick(View v) {
        if (v == binding.activityBuyGroupFeatureNav.addCloseImageButton()) {
            if (_type == 2) {
                _type = 1;
                _updateUI();
            } else {
                finish();
            }
        } else if (v == binding.activityBuyGroupFeatureQsjRuleTv) {
            _type = 2;
            _updateUI();
        } else if (v == binding.activityBuyGroupFeatureBuyTv) {
            int grade = -1;
            String moneyStr = "";
            for (GroupInfoBean tempBean :adapter.getItems()) {
                if (tempBean.isSelected) {
                    grade = tempBean.grade;
                    moneyStr = NumberUtil.formartMoney(tempBean.price + "");
                }

            }
            if (grade == -1) {
                ToastUtils.toastMsg("请选择升级类型");
                return;
            }
            int finalGrade = grade;
            PopEnterPassword popEnterPassword = new PopEnterPassword(this, new OnPasswordInputFinish() {
                @Override
                public void inputFinish(String password) {
                    RegisterBean registerBean = new RegisterBean();
                    registerBean.grade = finalGrade + "";
                    registerBean.groupId = _groupId;
                    registerBean.password = password;
                    HttpUtil.apiW().group_buyGroupGrade(registerBean)
                            .enqueue(new CommonCallback<NetData>() {
                                @Override
                                public void Successful(Call<NetData> call, Response<NetData> response, NetData body) {

                                }

                                @Override
                                public void Failure(Call<NetData> call, Throwable t) {

                                }
                            });
                }
            },moneyStr);
            // 显示窗口
            popEnterPassword.showAtLocation(binding.activityBuyGroupFeatureRootRl,
                    Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL, 0, 0); // 设置layout在PopupWindow中显示的位置



        }
    }

}
