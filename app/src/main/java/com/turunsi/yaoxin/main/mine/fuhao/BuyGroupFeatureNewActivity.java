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
import com.turunsi.yaoxin.databinding.ActivityBuyGroupFeatureBinding;
import com.turunsi.yaoxin.databinding.ActivityBuyGroupNewFeatureBinding;
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
import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Response;

public class BuyGroupFeatureNewActivity extends BaseActivity implements View.OnClickListener {
    ActivityBuyGroupNewFeatureBinding binding;
    GroupBuyListAdapter adapter = new GroupBuyListAdapter();
    int _type = 0;
    String _groupId;
    int _grade;
    int _groupMemberNum;
    int _price;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        _type = getIntent().getIntExtra("type", 0);
        _groupId = getIntent().getStringExtra("groupId");
        _grade = getIntent().getIntExtra("grade", 0);
        _groupMemberNum = getIntent().getIntExtra("groupMemberNum", 0);
        _price = getIntent().getIntExtra("price", 0);

        binding = ActivityBuyGroupNewFeatureBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        binding.activityBuyGroupFeatureNav.addCloseImageButton().setOnClickListener(this);
        binding.activityBuyGroupFeatureQsjRuleTv.setOnClickListener(this);
        binding.activityBuyGroupFeatureBuyTv.setOnClickListener(this);
        binding.cellBuyGroupFeatureMoneyTv.setText("￥" + NumberUtil.formartMoney(_price + ""));

        String b = "1000以上";
        if (_groupMemberNum == -1) {
            binding.cellBuyGroupFeatureDetailTv.setText("购买即升级当前群组为" + b + "人群");
        } else {
            binding.cellBuyGroupFeatureDetailTv.setText("购买即升级当前群组为" + _groupMemberNum + "人群");
        }
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
            binding.activityBuyFeatureBottomLl.setVisibility(View.GONE);
            binding.activityBuyGroupFeatureNav.getTitleView().setText("升级规则");
        } else {
            binding.activityBuyFeatureBottomLl.setVisibility(View.VISIBLE);
            binding.activityBuyGroupFeatureNav.getTitleView().setText("升级群组");
            binding.activityBuyGroupFeatureQsjRuleTv.setVisibility(View.VISIBLE);
        }
    }

    @Override
    protected void _requestData() {
        if (_type == 0) return;
        HttpUtil.apiW().group_groupGrade()
                .enqueue(new CommonCallback<NetData>() {
                    @Override
                    public void Successful(Call<NetData> call, Response<NetData> response, NetData body) {
                        Type type = new TypeToken<List<GroupInfoBean>>() {
                        }.getType();
                        List<GroupInfoBean> tempList = new Gson().fromJson(body.data.toString(), type);
//                        if (!tempList.isEmpty()) {
//                            tempList.get(0).isSelected = true;
//                        }
                        ArrayList<GroupInfoBean> tempArr = new ArrayList<>();
                        if (tempList.size() > 1) {
                            tempList.get(1).isSelected = true;
                            tempArr.add(tempList.get(1));
                            adapter.setItems(tempArr);
                            adapter.notifyDataSetChanged();
                        }
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
            int finalGrade = _grade;
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
                                    ToastUtils.toastMsg("购买成功");
                                }

                                @Override
                                public void Failure(Call<NetData> call, Throwable t) {

                                }
                            });
                }
            }, NumberUtil.formartMoney(_price + ""));
            // 显示窗口
            popEnterPassword.showAtLocation(binding.activityBuyGroupFeatureRootRl,
                    Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL, 0, 0); // 设置layout在PopupWindow中显示的位置


        }
    }

}
