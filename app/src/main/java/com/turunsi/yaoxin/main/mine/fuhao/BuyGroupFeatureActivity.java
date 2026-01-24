package com.turunsi.yaoxin.main.mine.fuhao;

import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.chad.library.adapter4.BaseQuickAdapter;
import com.turunsi.yaoxin.R;
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
import com.yaoxin.appbase.utils.DataUtil;
import com.yaoxin.appbase.utils.ToastUtils;

import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Response;

public class BuyGroupFeatureActivity extends BaseActivity implements View.OnClickListener {
    ActivityBuyGroupFeatureBinding binding;

    GroupBuyListAdapter adapter = new GroupBuyListAdapter();
    String _groupId;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        _groupId = getIntent().getStringExtra("groupId");
        binding = ActivityBuyGroupFeatureBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        binding.activityBuyGroupFeatureNav.addCloseImageButton().setOnClickListener(this);
        binding.activityBuyGroupFeatureQsjRuleTv.setOnClickListener(this);
        // 移除底部购买按钮的点击事件，改为每个卡片独立购买

        binding.activityBuyGroupFeatureRv.setLayoutManager(new LinearLayoutManager(this));
        binding.activityBuyGroupFeatureRv.setAdapter(adapter);
        adapter.setOnItemClickListener(new BaseQuickAdapter.OnItemClickListener<GroupInfoBean>() {
            @Override
            public void onClick(@NonNull BaseQuickAdapter<GroupInfoBean, ?> baseQuickAdapter, @NonNull View view, int i) {
                // 点击卡片时购买
                GroupInfoBean bean = baseQuickAdapter.getItem(i);
                if (bean != null) {
                    _buyGroupGrade(bean);
                }
            }
        });

        // 为每个item的"立即购买"按钮设置点击事件
        adapter.addOnItemChildClickListener(R.id.cell_buy_group_feature_buy_btn, new BaseQuickAdapter.OnItemChildClickListener<GroupInfoBean>() {
            @Override
            public void onItemClick(@NonNull BaseQuickAdapter<GroupInfoBean, ?> baseQuickAdapter, @NonNull View view, int i) {
                GroupInfoBean bean = baseQuickAdapter.getItem(i);
                if (bean != null) {
                    _buyGroupGrade(bean);
                }
            }
        });
        _initFixedData();
        _updateUI();
    }


    void _updateUI() {
        binding.activityBuyFeatureBottomLl.setVisibility(View.VISIBLE);
        binding.activityBuyGroupFeatureRv.setVisibility(View.VISIBLE);
        binding.activityBuyGroupFeatureNav.getTitleView().setText("升级群组");
        binding.activityBuyGroupFeatureQsjRuleTv.setVisibility(View.VISIBLE);
        binding.activityBuyGroupFeatureRv.setPadding(0, 0, 0, 80);
    }

    /**
     * 初始化固定数据：两个升级选项
     */
    void _initFixedData() {
        ArrayList<GroupInfoBean> dataList = new ArrayList<>();

        // 选项1：1000人群，¥200，grade=1
        GroupInfoBean option1 = new GroupInfoBean();
        option1.grade = 1;
        option1.price = 200;
        option1.groupMemberNum = 1000;
        dataList.add(option1);

        // 选项2：1000人以上，¥500，grade=2
        GroupInfoBean option2 = new GroupInfoBean();
        option2.grade = 2;
        option2.price = 500;
        option2.groupMemberNum = -1; // -1 表示1000人以上/不限制人数
        dataList.add(option2);

        adapter.setItems(dataList);
        adapter.notifyDataSetChanged();
    }

    @Override
    public void onClick(View v) {
        if (v == binding.activityBuyGroupFeatureNav.addCloseImageButton()) {
            finish();
        } else if (v == binding.activityBuyGroupFeatureQsjRuleTv) {
            startActivity(new Intent(this, GroupUpgradeRuleActivity.class));
        }
    }

    /**
     * 购买群升级
     *
     * @param bean 选中的升级选项
     */
    void _buyGroupGrade(GroupInfoBean bean) {
        if (bean == null) {
            return;
        }

        // 价格直接显示，不除以100（因为价格已经是元为单位）
        String moneyStr = bean.price + "";
        int finalGrade = bean.grade;

        PopEnterPassword popEnterPassword = new PopEnterPassword(this, new OnPasswordInputFinish() {
            @Override
            public void inputFinish(String password) {
                RegisterBean registerBean = new RegisterBean();
                registerBean.grade = finalGrade + "";
                registerBean.groupId = _groupId;
                registerBean.userId = DataUtil.getUserid(); // 当前登录人的ID
                registerBean.password = password; // 支付密码

                HttpUtil.apiW().group_buyGroupGrade(registerBean)
                        .enqueue(new CommonCallback<NetData>() {
                            @Override
                            public void Successful(Call<NetData> call, Response<NetData> response, NetData body) {
                                ToastUtils.toastMsg("购买成功");
                                finish();
                            }

                            @Override
                            public void Failure(Call<NetData> call, Throwable t) {
                                ToastUtils.toastMsg("购买失败，请重试");
                            }
                        });
            }
        }, moneyStr);
        // 显示窗口
        popEnterPassword.showAtLocation(binding.activityBuyGroupFeatureRootRl,
                Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL, 0, 0);
    }

}
