package com.turunsi.yaoxin.main.found;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.chad.library.adapter4.BaseQuickAdapter;
import com.netease.yunxin.kit.common.ui.fragments.BaseFragment;
import com.netease.yunxin.kit.common.utils.SizeUtils;
import com.netease.yunxin.kit.corekit.im.utils.RouterConstant;
import com.netease.yunxin.kit.corekit.route.XKitRouter;
import com.turunsi.yaoxin.R;
import com.turunsi.yaoxin.databinding.FragmentFoundBinding;
import com.yaoxin.appbase.utils.BarUtils;
import com.yaoxin.appbase.utils.BaseEvent;
import com.yaoxin.appbase.utils.StatusBarUtils;
import com.yaoxin.appbase.utils.ToastUtils;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.List;

public class FoundFragment extends BaseFragment {

    private FragmentFoundBinding binding;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentFoundBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        StatusBarUtils.setStatusBarLightMode(getActivity(), true, true);
        ViewGroup.MarginLayoutParams layoutParams =
                (ViewGroup.MarginLayoutParams) binding.foundFragmentTopLl.getLayoutParams();
        layoutParams.topMargin = BarUtils.getStatusBarHeight() + SizeUtils.dp2px(20);
        binding.foundFragmentTopLl.setLayoutParams(layoutParams);
        initMenuList();
    }

    private void initMenuList() {
        FoundMenuAdapter adapter = new FoundMenuAdapter();
        adapter.submitList(buildMenuItems());
        adapter.setOnItemClickListener(new BaseQuickAdapter.OnItemClickListener<FoundMenuItem>() {
            @Override
            public void onClick(@NonNull BaseQuickAdapter<FoundMenuItem, ?> baseQuickAdapter, @NonNull View view, int i) {
                FoundMenuItem item = baseQuickAdapter.getItem(i);
                if (item == null || item.type != FoundMenuItem.TYPE_MENU) {
                    return;
                }
                handleMenuClick(item);
            }
        });
        binding.foundFragmentRv.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.foundFragmentRv.setAdapter(adapter);
    }

    private void handleMenuClick(FoundMenuItem item) {
        switch (item.action) {
            case FoundMenuItem.ACTION_SCAN:
                EventBus.getDefault().post(new BaseEvent("gotoScan"));
                break;
            case FoundMenuItem.ACTION_ADD_FRIEND:
                XKitRouter.withKey(RouterConstant.PATH_FUN_ADD_FRIEND_PAGE)
                        .withContext(requireContext())
                        .navigate();
                break;
            default:
                ToastUtils.toastMsg("敬请期待,等待开发");
                break;
        }
    }

    /**
     * 发现页菜单图标命名（mipmap-xxhdpi）：
     * found_icon_moments        朋友圈
     * found_icon_channels       视频号
     * found_icon_live           直播
     * found_icon_scan           扫一扫
     * found_icon_listen         听一听
     * found_icon_look           看一看
     * found_icon_search         搜一搜
     * found_icon_nearby         附近的人
     * found_icon_game           游戏
     * found_icon_mini_program   小程序
     */
    private List<FoundMenuItem> buildMenuItems() {
        List<FoundMenuItem> items = new ArrayList<>();
        items.add(FoundMenuItem.menu("朋友圈", R.mipmap.found_icon_moments));
        items.add(FoundMenuItem.gap());
        items.add(FoundMenuItem.menu("视频号", R.mipmap.found_icon_channels));
        items.add(FoundMenuItem.menu("直播", R.mipmap.found_icon_live));
        items.add(FoundMenuItem.gap());
        items.add(FoundMenuItem.menu("扫一扫", R.mipmap.found_icon_scan, FoundMenuItem.ACTION_SCAN));
        items.add(FoundMenuItem.menu("听一听", R.mipmap.found_icon_listen));
        items.add(FoundMenuItem.gap());
        items.add(FoundMenuItem.menu("看一看", R.mipmap.found_icon_look));
        items.add(FoundMenuItem.menu("搜一搜", R.mipmap.found_icon_search, FoundMenuItem.ACTION_ADD_FRIEND));
        items.add(FoundMenuItem.gap());
        items.add(FoundMenuItem.menu("附近的人", R.mipmap.found_icon_nearby));
        items.add(FoundMenuItem.gap());
        items.add(FoundMenuItem.menu("游戏", R.mipmap.found_icon_game));
        items.add(FoundMenuItem.gap());
        items.add(FoundMenuItem.menu("小程序", R.mipmap.found_icon_mini_program));
        return items;
    }
}
