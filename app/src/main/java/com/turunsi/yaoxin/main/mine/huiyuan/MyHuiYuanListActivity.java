package com.turunsi.yaoxin.main.mine.huiyuan;

import android.graphics.Rect;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.chad.library.adapter4.BaseQuickAdapter;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.netease.yunxin.kit.common.utils.SizeUtils;
import com.turunsi.yaoxin.R;
import com.turunsi.yaoxin.databinding.ActivityMineMyFuhaoListBinding;
import com.turunsi.yaoxin.databinding.ActivityMineMyHuiyuanListBinding;
import com.turunsi.yaoxin.main.mine.fuhao.BuyFeatureActivity;
import com.turunsi.yaoxin.main.mine.fuhao.adapter.MyFuHaoListAdapter;
import com.turunsi.yaoxin.main.mine.huiyuan.adapter.HuiYuanCardPagerAdapter;
import com.turunsi.yaoxin.main.mine.huiyuan.adapter.MyHuiYuanListAdapter;
import com.turunsi.yaoxin.main.mine.huiyuan.adapter.MyLiangHaoListAdapter;
import com.yaoxin.appbase.activity.BaseActivity;
import com.yaoxin.appbase.model.HuiYuanBean;
import com.yaoxin.appbase.model.NetData;
import com.yaoxin.appbase.model.RegisterBean;
import com.yaoxin.appbase.model.UserBean;
import com.yaoxin.appbase.net.CommonCallback;
import com.yaoxin.appbase.net.HttpUtil;
import com.yaoxin.appbase.pswkeyboard.OnPasswordInputFinish;
import com.yaoxin.appbase.pswkeyboard.widget.PopEnterPassword;
import com.yaoxin.appbase.utils.BaseEvent;
import com.yaoxin.appbase.utils.DataUtil;
import com.yaoxin.appbase.utils.GlideUtil;
import com.yaoxin.appbase.utils.NumberUtil;
import com.yaoxin.appbase.utils.StatusBarUtils;
import com.yaoxin.appbase.utils.ToastUtils;
import com.yaoxin.appbase.view.CommonGridSpacingItemDecoration;
import com.yaoxin.appbase.view.LoadingDialog;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import retrofit2.Call;
import retrofit2.Response;

public class MyHuiYuanListActivity extends BaseActivity implements View.OnClickListener {
    ActivityMineMyHuiyuanListBinding binding;

    MyLiangHaoListAdapter adapter = new MyLiangHaoListAdapter();
    HuiYuanCardPagerAdapter cardPagerAdapter = new HuiYuanCardPagerAdapter();

    HuiYuanBean dataBean = new HuiYuanBean();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMineMyHuiyuanListBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        StatusBarUtils.transtStatusBar(this,binding.activityMineMyHuiyuanListNav);

        GlideUtil.yh_loadImage(this, binding.activityMineMyHuiyuanListHeadIv, DataUtil.getUserInfo().avatar);
        binding.activityMineMyHuiyuanListNameTv.setText(DataUtil.getUserInfo().username);
        binding.activityMineMyHuiyuanListNav.addCloseImageButton().setOnClickListener(this);
        binding.activityMineMyHuiyuanListBuyRl.setOnClickListener(this);
//        binding.activityMineMyFuhaoListSelfPhoneTv.setText(DataUtil.getUserInfo().phoneNo);
        GridLayoutManager gridLayoutManager = new GridLayoutManager(this, 3);
        binding.activityMineMyHuiyuanListBottomRv.setLayoutManager(gridLayoutManager);
        CommonGridSpacingItemDecoration gridSpacingItemDecoration =
                new CommonGridSpacingItemDecoration(3, SizeUtils.dp2px(12), true);
        binding.activityMineMyHuiyuanListBottomRv.addItemDecoration(gridSpacingItemDecoration);
        binding.activityMineMyHuiyuanListBottomRv.setAdapter(adapter);
        adapter.setOnItemClickListener(new BaseQuickAdapter.OnItemClickListener<Integer>() {
            @Override
            public void onClick(@NonNull BaseQuickAdapter<Integer, ?> baseQuickAdapter, @NonNull View view, int i) {
                adapter.selectNumber = baseQuickAdapter.getItem(i);
                adapter.notifyDataSetChanged();
            }
        });
        EventBus.getDefault().register(this);

        cardInit();
    }

    void cardInit() {
        ViewPager2 viewPager = binding.activityMineMyHuiyuanListTopVp;
        
        // 设置适配器
        viewPager.setAdapter(cardPagerAdapter);
        
        // 设置页面间距
        viewPager.setPageTransformer((page, position) -> {
            float scale = 0.85f + (1 - Math.abs(position)) * 0.15f;
            page.setScaleX(scale);
            page.setScaleY(scale);
        });
        
        // 设置页面变化监听
        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                if (dataBean.list != null && position < dataBean.list.size()) {
                    adapter.setItems(dataBean.list.get(position).memberCode);
                    adapter.selectNumber = 0;
                    dataBean.currentIndex = position;
                    adapter.notifyDataSetChanged();
                    binding.activityMineMyHuiyuanListIntroduceTv.setText("以下是" + dataBean.list.get(position).memberConfig.productName);
                    updateIndicator(position);
                }
            }
        });
        
        // 初始化指示器
        initIndicator();
    }
    
    /**
     * 初始化页面指示器
     */
    private void initIndicator() {
        if (dataBean.list == null || dataBean.list.isEmpty()) {
            return;
        }
        LinearLayout indicatorLayout = binding.activityMineMyHuiyuanListIndicatorLl;
        indicatorLayout.removeAllViews();
        
        for (int i = 0; i < dataBean.list.size(); i++) {
            View indicator = new View(this);
            int size = SizeUtils.dp2px(6);
            int margin = SizeUtils.dp2px(4);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(size, size);
            params.setMargins(margin, 0, margin, 0);
            indicator.setLayoutParams(params);
            indicator.setBackgroundResource(R.drawable.bg_indicator_unselected);
            indicatorLayout.addView(indicator);
        }
        
        // 设置第一个为选中状态
        if (indicatorLayout.getChildCount() > 0) {
            indicatorLayout.getChildAt(0).setBackgroundResource(R.drawable.bg_indicator_selected);
        }
    }
    
    /**
     * 更新页面指示器
     */
    private void updateIndicator(int selectedPosition) {
        LinearLayout indicatorLayout = binding.activityMineMyHuiyuanListIndicatorLl;
        for (int i = 0; i < indicatorLayout.getChildCount(); i++) {
            View indicator = indicatorLayout.getChildAt(i);
            if (i == selectedPosition) {
                indicator.setBackgroundResource(R.drawable.bg_indicator_selected);
            } else {
                indicator.setBackgroundResource(R.drawable.bg_indicator_unselected);
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(BaseEvent event) {
        if ("reload_fuhao".equals(event.getTag())) {
            _requestData();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }


    @Override
    protected void _requestData() {
        HttpUtil.apiW().meteor_list()
                .enqueue(new CommonCallback<NetData>() {
                    @Override
                    public void Successful(Call<NetData> call, Response<NetData> response, NetData body) {

                        dataBean = new Gson().fromJson(body.data.toString(), HuiYuanBean.class);
                        cardPagerAdapter.setDataList(dataBean.list);
                        adapter.setItems(dataBean.list.get(0).memberCode);
                        binding.activityMineMyHuiyuanListIntroduceTv.setText("以下是" + dataBean.list.get(0).memberConfig.productName);

                        adapter.notifyDataSetChanged();
                        initIndicator();

                    }

                    @Override
                    public void Failure(Call<NetData> call, Throwable t) {

                    }
                });
    }

    @Override
    public void onClick(View v) {
        if (v == binding.activityMineMyHuiyuanListNav.addCloseImageButton()) {
//            HashMap map = new HashMap();
//            map.put("type","0");
//            BuyFeatureActivity.start(BuyFeatureActivity.class,this,map);
            finish();
        }
        else if (v == binding.activityMineMyHuiyuanListBuyRl) {

            if (adapter.selectNumber == 0) {
                ToastUtils.toastMsg("请选择");
                return;
            }
            PopEnterPassword popEnterPassword = new PopEnterPassword(this, new OnPasswordInputFinish() {
                @Override
                public void inputFinish(String password) {
                    RegisterBean registerBean = new RegisterBean();
                    registerBean.memberCode = adapter.selectNumber + "";
                    registerBean.password = password;
                    LoadingDialog.showDialog(getSupportFragmentManager(),"购买中..");
                    HttpUtil.apiW().meteor_buyMember(registerBean)
                            .enqueue(new CommonCallback<NetData>() {
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
            },NumberUtil.formartMoney_zhengshu(dataBean.list.get(dataBean.currentIndex).memberConfig.price));

            // 显示窗口
            popEnterPassword.showAtLocation(binding.activityMineMyHuiyuanListRootRl,
                    Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL, 0, 0); // 设置layout在PopupWindow中显示的位置

        }
    }

}
