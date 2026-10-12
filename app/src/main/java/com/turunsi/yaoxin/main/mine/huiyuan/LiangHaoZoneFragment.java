package com.turunsi.yaoxin.main.mine.huiyuan;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.gson.Gson;
import com.netease.yunxin.kit.common.utils.SizeUtils;
import com.netease.yunxin.kit.corekit.route.XKitRouter;
import com.turunsi.yaoxin.databinding.FragmentLianghaoZoneBinding;
import com.turunsi.yaoxin.main.mine.huiyuan.adapter.LiangHaoGroupAdapter;
import com.yaoxin.appbase.fragment.BaseFragment;
import com.yaoxin.appbase.model.HuiYuanBean;
import com.yaoxin.appbase.model.NetData;
import com.yaoxin.appbase.net.CommonCallback;
import com.yaoxin.appbase.net.HttpUtil;
import com.yaoxin.appbase.utils.BarUtils;
import com.yaoxin.appbase.utils.StatusBarUtils;
import com.yaoxin.appbase.utils.ToastUtils;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import retrofit2.Call;
import retrofit2.Response;

/**
 * 会员商城 Tab：靓号专区
 */
public class LiangHaoZoneFragment extends BaseFragment {
    private FragmentLianghaoZoneBinding binding;
    private HuiYuanBean dataBean;
    private LiangHaoGroupAdapter groupAdapter;

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        binding = FragmentLianghaoZoneBinding.inflate(inflater, container, false);
        EventBus.getDefault().register(this);
        // 与消息/好友/群聊 Tab 一致：状态栏高度 + 20dp
        if (getActivity() != null) {
            StatusBarUtils.setStatusBarLightMode(getActivity(), true, true);
        }
        ViewGroup.MarginLayoutParams layoutParams =
                (ViewGroup.MarginLayoutParams) binding.lianghaoZoneTopLl.getLayoutParams();
        layoutParams.topMargin = BarUtils.getStatusBarHeight() + SizeUtils.dp2px(20);
        binding.lianghaoZoneTopLl.setLayoutParams(layoutParams);
        initView();
        loadData();
        return binding.getRoot();
    }

    private void initView() {
        binding.rvGroup.setLayoutManager(new LinearLayoutManager(getContext()));
        groupAdapter = new LiangHaoGroupAdapter();
        binding.rvGroup.setAdapter(groupAdapter);
        binding.layoutLiangHaoLast.setOnClickListener(
                view -> {
                    ExchangeLiangHaoDialogFragment dialog =
                            new ExchangeLiangHaoDialogFragment()
                                    .setOnExchangeListener(
                                            (code, level) -> {
                                                if (dataBean == null
                                                        || dataBean.list == null
                                                        || dataBean.list.isEmpty()) {
                                                    ToastUtils.toastMsg("数据加载中，请稍后重试");
                                                    return;
                                                }
                                                HuiYuanBean target = null;
                                                for (HuiYuanBean item : dataBean.list) {
                                                    if (item != null
                                                            && item.memberConfig != null
                                                            && level.equals(
                                                                    item.memberConfig
                                                                            .memberLevel)) {
                                                        target = item;
                                                        break;
                                                    }
                                                }
                                                if (target == null) {
                                                    ToastUtils.toastMsg("请输入正确的靓号等级");
                                                    return;
                                                }
                                                Intent intent =
                                                        new Intent(
                                                                requireContext(),
                                                                LiangHaoZoneMoreActivity.class);
                                                intent.putExtra("code", code);
                                                intent.putExtra("huiyuan", target);
                                                startActivity(intent);
                                            });
                    dialog.showNow(getChildFragmentManager(), "ExchangeLiangHaoDialog");
                });
        binding.layoutLiangHaoCj.setOnClickListener(
                view ->
                        XKitRouter.withKey(
                                        com.yaoxin.appbase.net.Constant.BaseWebViewActivityKey)
                                .withParam("type", "4")
                                .withParam("title", "抽奖")
                                .withContext(requireContext())
                                .navigate());
    }

    private void loadData() {
        HttpUtil.apiW()
                .meteor_list()
                .enqueue(
                        new CommonCallback<NetData>() {
                            @Override
                            public void Successful(
                                    Call<NetData> call, Response<NetData> response, NetData body) {
                                dataBean =
                                        new Gson()
                                                .fromJson(body.data.toString(), HuiYuanBean.class);
                                groupAdapter.setItems(dataBean.list);
                                groupAdapter.notifyDataSetChanged();
                            }

                            @Override
                            public void Failure(Call<NetData> call, Throwable t) {
                                // ignore
                            }
                        });
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(com.yaoxin.appbase.utils.BaseEvent event) {
        if ("reload_fuhao".equals(event.getTag())) {
            loadData();
        }
    }

    @Override
    public void onDestroyView() {
        EventBus.getDefault().unregister(this);
        binding = null;
        super.onDestroyView();
    }
}
