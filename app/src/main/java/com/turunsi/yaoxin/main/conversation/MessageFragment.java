package com.turunsi.yaoxin.main.conversation;

import static com.netease.yunxin.kit.corekit.im.utils.RouterConstant.PATH_FUN_ADD_FRIEND_PAGE;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.google.android.material.tabs.TabLayoutMediator;
import com.google.gson.Gson;
import com.netease.yunxin.kit.alog.ALog;
import com.netease.yunxin.kit.conversationkit.ui.fun.page.FunConversationFragment;
import com.netease.yunxin.kit.conversationkit.ui.page.ConversationBaseFragment;
import com.netease.yunxin.kit.corekit.route.XKitRouter;
import com.turunsi.yaoxin.R;
import com.turunsi.yaoxin.databinding.FragmentMessageBinding;
import com.turunsi.yaoxin.main.MainActivity;
import com.turunsi.yaoxin.main.mine.setting.ExchangeAccountActivity;
import com.turunsi.yaoxin.utils.Constant;
import com.yaoxin.appbase.fragment.BaseFragment;
import com.yaoxin.appbase.model.GroupInfoBean;
import com.yaoxin.appbase.model.NetData;
import com.yaoxin.appbase.net.CommonCallback;
import com.yaoxin.appbase.net.HttpUtil;
import com.yaoxin.appbase.utils.BarUtils;
import com.yaoxin.appbase.utils.BaseEvent;
import com.yaoxin.appbase.utils.StatusBarUtils;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Response;

public class MessageFragment extends BaseFragment {

    private FragmentMessageBinding binding;
    private ConversationBaseFragment mConversationFragment;
    private ConversationBaseFragment mConversationFragment1;
    private ConversationBaseFragment mConversationFragment2;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        ALog.d(Constant.PROJECT_TAG, "MineFragment:onCreateView");
        binding = FragmentMessageBinding.inflate(inflater);
        StatusBarUtils.setStatusBarLightMode(getActivity(), true, true);
        binding.contactNewFragmentTopLl.setPadding(binding.contactNewFragmentTopLl.getPaddingLeft(), BarUtils.getStatusBarHeight() + binding.contactNewFragmentTopLl.getPaddingTop(), binding.contactNewFragmentTopLl.getPaddingRight(), 0);
        return binding.getRoot();
    }

    @Override
    public void onResume() {
        super.onResume();
        getSysNotice();
    }

    private void getSysNotice() {
        HttpUtil.apiW().customer_notice().enqueue(new CommonCallback<NetData>() {
            @Override
            public void Successful(Call<NetData> call, Response<NetData> response, NetData body) {
                GroupInfoBean groupInfoBean = new Gson().fromJson(body.data.toString(), GroupInfoBean.class);
                if (groupInfoBean != null && groupInfoBean.content != null) {
                    String message = groupInfoBean.content;
                    if (message != null && !message.isEmpty()) {
                        binding.marqueeView.startWithText(message);
                        binding.marqueeView.startWithText(message, com.sunfusheng.marqueeview.R.anim.anim_bottom_in, com.sunfusheng.marqueeview.R.anim.anim_top_out);
                    } else {
                        message = "";
                        binding.marqueeView.startWithText(message);
                        binding.marqueeView.startWithText(message, com.sunfusheng.marqueeview.R.anim.anim_bottom_in, com.sunfusheng.marqueeview.R.anim.anim_top_out);
                    }
                } else {

                    String message = "";
                    binding.marqueeView.startWithText(message);
                    binding.marqueeView.startWithText(message, com.sunfusheng.marqueeview.R.anim.anim_bottom_in, com.sunfusheng.marqueeview.R.anim.anim_top_out);

                }
            }

            @Override
            public void Failure(Call<NetData> call, Throwable t) {
                String message = "";
                binding.marqueeView.startWithText(message);
                binding.marqueeView.startWithText(message, com.sunfusheng.marqueeview.R.anim.anim_bottom_in, com.sunfusheng.marqueeview.R.anim.anim_top_out);
            }
        });
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        List<String> titles = new ArrayList<>();
        titles.add("全部");
        titles.add("单聊");
        titles.add("群聊");
        titles.add("系统消息");
        List<Fragment> fragments = new ArrayList<>();
        mConversationFragment = new FunConversationFragment(3);
        mConversationFragment1 = new FunConversationFragment(0);
        mConversationFragment2 = new FunConversationFragment(1);
        fragments.add(mConversationFragment);
        fragments.add(mConversationFragment1);
        fragments.add(mConversationFragment2);
        binding.viewPager2.setAdapter(new FragmentStateAdapter(this) {
            @NonNull
            @Override
            public Fragment createFragment(int position) {
                Fragment fragment = fragments.get(position);
                return fragment;
            }

            @Override
            public int getItemCount() {
                return fragments.size();
            }
        });
        binding.viewPager2.setOffscreenPageLimit(fragments.size());
        new TabLayoutMediator(binding.tabLayout, binding.viewPager2, true, (tab, position) -> {
            tab.setText(titles.get(position));
        }).attach();

        binding.contactNewFragmentSearchLl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                XKitRouter.withKey("SearchNewActivity").withContext(requireContext()).navigate();
            }
        });
        binding.contactNewFragmentMoreIv.setOnClickListener(view1 -> {
            EventBus.getDefault().post(new BaseEvent("gotoScan"));
        });
        //监听消息
        initConversationFragment(mConversationFragment);
    }

    private void initConversationFragment(ConversationBaseFragment conversationFragment) {
        if (conversationFragment != null) {
            conversationFragment.setConversationCallback(count -> {
                ((MainActivity) requireActivity()).setConversationDot(count);
            });
        }
    }
}
