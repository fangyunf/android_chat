package com.turunsi.yaoxin.main.mine.huiyuan;

import android.graphics.Rect;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.LinearSnapHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.chad.library.adapter4.BaseQuickAdapter;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.netease.yunxin.kit.common.utils.SizeUtils;
import com.turunsi.yaoxin.databinding.ActivityMineMyFuhaoListBinding;
import com.turunsi.yaoxin.databinding.ActivityMineMyHuiyuanListBinding;
import com.turunsi.yaoxin.main.mine.fuhao.BuyFeatureActivity;
import com.turunsi.yaoxin.main.mine.fuhao.adapter.MyFuHaoListAdapter;
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
    MyHuiYuanListAdapter topAdapter = new MyHuiYuanListAdapter();

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
                new CommonGridSpacingItemDecoration(3, SizeUtils.dp2px(10), false);
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


        RecyclerView recyclerView = binding.activityMineMyHuiyuanListTopRv;

        // 设置水平线性布局
        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        recyclerView.setLayoutManager(layoutManager);

        // 设置适配器
        recyclerView.setAdapter(topAdapter);

        // 添加 LinearSnapHelper，让每次滑动后卡片居中
        LinearSnapHelper snapHelper = new LinearSnapHelper();
        snapHelper.attachToRecyclerView(recyclerView);

        topAdapter.setOnItemClickListener(new BaseQuickAdapter.OnItemClickListener<HuiYuanBean>() {
            @Override
            public void onClick(@NonNull BaseQuickAdapter<HuiYuanBean, ?> baseQuickAdapter, @NonNull View view, int i) {

                // 现在 position 是居中的 item 的位置
                adapter.setItems(dataBean.list.get(i).memberCode);
                adapter.selectNumber = 0;
                dataBean.currentIndex = i;
                adapter.notifyDataSetChanged();
                binding.activityMineMyHuiyuanListIntroduceTv.setText("以下是" + dataBean.list.get(i).memberConfig.productName);
            }
        });
        // 设置卡片左右间距
        recyclerView.addItemDecoration(new RecyclerView.ItemDecoration() {
            @Override
            public void getItemOffsets(@NonNull Rect outRect, @NonNull View view, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
                int position = parent.getChildAdapterPosition(view);
                int offset = 16; // 每张卡片的间距

                // 设置第一个卡片的左边距和最后一个卡片的右边距
                if (position == 0) {
                    outRect.left = offset * 2;
                } else {
                    outRect.left = offset;
                }
                outRect.right = offset;
            }
        });
        // 监听 RecyclerView 滚动以获取当前居中的项目
//        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
//            @Override
//            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
//                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
//                    // 当滚动停止时
//                    View centerView = snapHelper.findSnapView(layoutManager);
//                    if (centerView != null) {
//                        int i = layoutManager.getPosition(centerView);
//                        // 现在 position 是居中的 item 的位置
//                        adapter.setItems(dataBean.list.get(i).memberCode);
//                        adapter.selectNumber = 0;
//                        dataBean.currentIndex = i;
//                        adapter.notifyDataSetChanged();
//                        binding.activityMineMyHuiyuanListIntroduceTv.setText("以下是" + dataBean.list.get(i).memberConfig.productName);
//                    }
//                }
//            }
//        });
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(com.yaoxin.appbase.utils.BaseEvent event) {
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
                        topAdapter.setItems(dataBean.list);
                        adapter.setItems(dataBean.list.get(0).memberCode);
                        binding.activityMineMyHuiyuanListIntroduceTv.setText("以下是" + dataBean.list.get(0).memberConfig.productName);

                        topAdapter.notifyDataSetChanged();
                        adapter.notifyDataSetChanged();

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
