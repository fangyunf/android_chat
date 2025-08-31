package com.netease.yunxin.kit.contactkit.ui.search;

import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.chad.library.adapter4.BaseQuickAdapter;
import com.netease.yunxin.kit.contactkit.ui.databinding.FunSearchActivityNewBinding;
import com.netease.yunxin.kit.contactkit.ui.normal.contact.adapter.ContactUserListAdapter;
import com.netease.yunxin.kit.corekit.im.utils.RouterConstant;
import com.netease.yunxin.kit.corekit.route.XKitRouter;
import com.yaoxin.appbase.activity.BaseActivity;
import com.yaoxin.appbase.model.GroupInfoBean;
import com.yaoxin.appbase.net.Constant;
import com.yaoxin.appbase.utils.DataUtil;
import com.yaoxin.appbase.utils.StatusBarUtils;

import java.util.ArrayList;

public class SearchNewActivity extends BaseActivity implements View.OnClickListener {
    FunSearchActivityNewBinding binding;
    ContactUserListAdapter adapter = new ContactUserListAdapter();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = FunSearchActivityNewBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        StatusBarUtils.transtStatusBar(this, binding.funSearchActivityNav);
        binding.funSearchActivityNav.addCloseImageButton().setOnClickListener(this);
        binding.funSearchActivityRv.setLayoutManager(new LinearLayoutManager(this));
        binding.funSearchActivityRv.setAdapter(adapter);

        adapter._isSearch = true;
        Context that = this;
        adapter.setOnItemClickListener(new BaseQuickAdapter.OnItemClickListener<GroupInfoBean>() {
            @Override
            public void onClick(@NonNull BaseQuickAdapter<GroupInfoBean, ?> baseQuickAdapter, @NonNull View view, int i) {
                if (baseQuickAdapter.getItemViewType(i) == Constant.RECYCLE_VIEW_ITEM) {
                    XKitRouter.withKey(RouterConstant.PATH_FUN_CHAT_SETTING_PAGE)
                            .withParam(RouterConstant.CHAT_ID_KRY, baseQuickAdapter.getItem(i).userId)
                            .withParam("type", "1")
                            .withContext(that)
                            .navigate();
                }
            }
        });
        binding.funSearchActivityEt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                String string = s.toString();
                if (string.isEmpty()) {
                    adapter.contacts = new ArrayList<>();
                    adapter.setItems(new ArrayList<>());
                    adapter.notifyDataSetChanged();
                    binding.funSearchActivityEmptyLl.setVisibility(View.GONE);

                } else {
                    ArrayList<GroupInfoBean> tempArr = new ArrayList<>();
                    for (GroupInfoBean temp :
                            DataUtil.getFriendInfoList()) {
                        if (temp.remark != null && !temp.remark.isEmpty()) {
                            if (temp.remark.contains(string)) {
                                tempArr.add(temp);
                            }
                        } else {
                            if (temp.name.contains(string)) {
                                tempArr.add(temp);
                            }
                        }

                    }
                    if (tempArr.isEmpty()) {
                        binding.funSearchActivityEmptyLl.setVisibility(View.VISIBLE);
                    } else {
                        binding.funSearchActivityEmptyLl.setVisibility(View.GONE);

                    }
                    adapter.contacts = tempArr;
                    adapter.setItems(tempArr);
                    adapter.notifyDataSetChanged();
                }
            }
        });
    }

    @Override
    public void onClick(View v) {
        if (v == binding.funSearchActivityNav.addCloseImageButton()) {
            finish();
        }
    }

}
