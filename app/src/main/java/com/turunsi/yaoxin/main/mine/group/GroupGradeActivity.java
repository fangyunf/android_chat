package com.turunsi.yaoxin.main.mine.group;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.chad.library.adapter4.BaseQuickAdapter;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.turunsi.yaoxin.databinding.ActivityGroupGradeBinding;
import com.yaoxin.appbase.model.GroupInfoBean;
import com.yaoxin.appbase.model.NetData;
import com.yaoxin.appbase.model.RegisterBean;
import com.yaoxin.appbase.net.CommonCallback;
import com.yaoxin.appbase.net.HttpUtil;
import com.yaoxin.appbase.utils.StatusBarUtils;
import com.yaoxin.appbase.utils.ToastUtils;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Response;

import com.yaoxin.appbase.activity.BaseActivity;

/**
 * 群等级购买页面
 */
public class GroupGradeActivity extends BaseActivity {

    private ActivityGroupGradeBinding binding;
    private GroupGradeAdapter adapter;
    private List<GroupInfoBean> gradeList = new ArrayList<>();
    private String groupId;
    private GroupInfoBean groupInfo;

    public static void start(Class<? extends Activity> clazz, Context context, String groupId) {
        Intent intent = new Intent(context, clazz);
        intent.putExtra("groupId", groupId);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityGroupGradeBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        groupId = getIntent().getStringExtra("groupId");
        initView();
        loadData();
    }

    private void initView() {
        binding.activityBuyFeatureNav.addCloseImageButton().setOnClickListener(view -> finish());
        StatusBarUtils.transtStatusBar(this, binding.activityBuyFeatureNav);
        // 初始化 RecyclerView
        binding.activityGroupGradeRv.setLayoutManager(new LinearLayoutManager(this));
        adapter = new GroupGradeAdapter();
        binding.activityGroupGradeRv.setAdapter(adapter);
        // 点击购买
        adapter.setOnItemClickListener((baseQuickAdapter, view, i) -> {
            GroupInfoBean item = baseQuickAdapter.getItem(i);
            if (item != null) {
                showBuyDialog(item);
            }
        });

        // 购买记录
        binding.activityGroupGradePurchaseRecordTv.setOnClickListener(v -> {
            // TODO: 跳转到购买记录页面
            ToastUtils.toastMsg("购买记录");
        });

        // 立即开通
        binding.activityGroupGradeBuyBtn.setOnClickListener(v -> {
            if (!gradeList.isEmpty()) {
                showBuyDialog(gradeList.get(0));
            }
        });
    }

    private void loadData() {
        // 加载群等级列表
        RegisterBean bean = new RegisterBean();
        bean.groupId = groupId;

        HttpUtil.apiW().group_groupGrade(bean).enqueue(new CommonCallback<NetData>() {
            @Override
            public void Successful(Call<NetData> call, Response<NetData> response, NetData body) {
                Type type = new TypeToken<List<GroupInfoBean>>() {
                }.getType();
                gradeList = new Gson().fromJson(body.data.toString(), type);

                if (gradeList != null && !gradeList.isEmpty()) {
                    adapter.setItems(gradeList);
                    adapter.notifyDataSetChanged();

                    // 默认选中第一个
                    if (!gradeList.isEmpty()) {
                        gradeList.get(0).isSelected = true;
                    }
                }
            }

            @Override
            public void Failure(Call<NetData> call, Throwable t) {
                ToastUtils.toastMsg("加载失败");
            }
        });
    }

    /**
     * 显示购买确认对话框
     */
    private void showBuyDialog(GroupInfoBean gradeInfo) {
//        new CommonChoiceDialog(this)
//                .setContent("确认购买 " + gradeInfo.gradeName + " 等级？\n价格：¥" + gradeInfo.price)
//                .setOnSureListener(() -> {
//                    // 需要输入支付密码
//                    showPasswordDialog(gradeInfo);
//                })
//                .showNow(getSupportFragmentManager(), "buyDialog");
    }

    /**
     * 显示密码输入对话框
     */
    private void showPasswordDialog(GroupInfoBean gradeInfo) {
        // 简单的密码输入对话框
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
        builder.setTitle("请输入支付密码");

        final android.widget.EditText input = new android.widget.EditText(this);
        input.setInputType(android.text.InputType.TYPE_CLASS_TEXT | android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD);
        builder.setView(input);

        builder.setPositiveButton("确定", (dialog, which) -> {
            String password = input.getText().toString();
            if (password.isEmpty()) {
                ToastUtils.toastMsg("请输入密码");
                return;
            }
            buyGroupGrade(gradeInfo, password);
        });

        builder.setNegativeButton("取消", (dialog, which) -> dialog.cancel());
        builder.show();
    }

    /**
     * 购买群等级
     */
    private void buyGroupGrade(GroupInfoBean gradeInfo, String password) {
        RegisterBean bean = new RegisterBean();
        bean.grade = gradeInfo.grade + "";
        bean.groupId = groupId;
        bean.password = password;

        HttpUtil.apiW().group_buyGroupGrade(bean).enqueue(new CommonCallback<NetData>() {
            @Override
            public void Successful(Call<NetData> call, Response<NetData> response, NetData body) {
                ToastUtils.toastMsg("购买成功");
                finish();
            }

            @Override
            public void Failure(Call<NetData> call, Throwable t) {
                if (t != null && t.getMessage() != null && t.getMessage().contains("588")) {
                    ToastUtils.toastMsg("等级不够，无法购买");
                } else {
                    ToastUtils.toastMsg("购买失败");
                }
            }
        });
    }
}

