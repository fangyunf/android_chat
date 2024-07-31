package com.netease.yunxin.kit.conversationkit.ui.fun.page;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.chad.library.adapter4.BaseQuickAdapter;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.nanchen.wavesidebar.WaveSideBarView;
import com.netease.yunxin.kit.conversationkit.ui.databinding.ActivityFunSelectedUserBinding;
import com.netease.yunxin.kit.conversationkit.ui.fun.page.adapter.Fun_Selected_UserListAdapter;
import com.netease.yunxin.kit.corekit.im.utils.RouterConstant;
import com.netease.yunxin.kit.corekit.model.ErrorMsg;
import com.netease.yunxin.kit.corekit.model.ResultInfo;
import com.netease.yunxin.kit.corekit.route.XKitRouter;
import com.yaoxin.appbase.activity.BaseActivity;
import com.yaoxin.appbase.model.CustomMsgBean;
import com.yaoxin.appbase.model.GroupInfoBean;
import com.yaoxin.appbase.model.NetData;
import com.yaoxin.appbase.model.RegisterBean;
import com.yaoxin.appbase.net.CommonCallback;
import com.yaoxin.appbase.net.HttpUtil;
import com.yaoxin.appbase.utils.AppProxy;
import com.yaoxin.appbase.utils.BaseEvent;
import com.yaoxin.appbase.utils.DataUtil;
import com.yaoxin.appbase.utils.DialogAlertUtil;
import com.yaoxin.appbase.utils.PinnedHeaderDecoration;
import com.yaoxin.appbase.utils.TeamIconUtils;
import com.yaoxin.appbase.utils.ToastUtils;

import org.greenrobot.eventbus.EventBus;

import java.io.Serializable;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Response;

public class FunSelected_User_Activity extends BaseActivity implements View.OnClickListener {

    ArrayList<GroupInfoBean> mContactModels = new ArrayList<>();
    private ActivityFunSelectedUserBinding binding;
    Fun_Selected_UserListAdapter adapter = new Fun_Selected_UserListAdapter();

    int page_type = 0;
    GroupInfoBean groupInfoBean;
    ArrayList ids = new ArrayList<>();
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        String type1 = getIntent().getStringExtra("type");
        String temp = DataUtil.getStringValue("groupInfo");

        if (temp != null) {
            groupInfoBean = new Gson().fromJson(temp,GroupInfoBean.class);
        }

        if (type1 != null) {
            page_type = Integer.parseInt(type1);
        }
        binding = ActivityFunSelectedUserBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.activityFunSelectedUserNav.addCloseImageButton().setOnClickListener(this);
        binding.activityFunSelectedUserConfirmTv.setOnClickListener(this);

        if (page_type == 2) {
            binding.activityFunSelectedUserNav.getTitleView().setText("邀请好友");
            for (GroupInfoBean tempBen : groupInfoBean.userInfos) {
                ids.add(tempBen.userId);
            }
        }
        if (page_type == 3) {
            binding.activityFunSelectedUserNav.getTitleView().setText("移除成员");
        }
        if (page_type == 4) {
            binding.activityFunSelectedUserNav.getTitleView().setText("赠送对象");
            binding.activityFunSelectedUserConfirmTv.setVisibility(View.GONE);
        }
        _requestData1();
        _initView();
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        DataUtil.setStringValue("","groupInfo");
    }


    protected void _requestData1() {
        if (page_type == 3 || page_type == 4) {
            if (groupInfoBean == null ) {
                return;
            }
            mContactModels.addAll(groupInfoBean.userInfos);
            adapter.contacts = mContactModels;
            adapter.setItems(mContactModels);
            adapter.notifyDataSetChanged();
            return;
        }
        HttpUtil.apiW().friends_friendList(new RegisterBean())
                .enqueue(new CommonCallback<NetData>() {
                    @Override
                    public void Successful(Call<NetData> call, Response<NetData> response, NetData body) {

                        Type type = new TypeToken<List<GroupInfoBean>>() {
                        }.getType();
                        mContactModels = new Gson().fromJson(body.data.toString(), type);
                        for (GroupInfoBean tempBean :
                                mContactModels) {
                            if (tempBean.userId.equals(DataUtil.getKeFuId())) {
                                mContactModels.remove(tempBean);
                                break;
                            }

                        }

                        if (page_type == 2) {
                            ArrayList<GroupInfoBean> tempArray = new ArrayList<>();
                            for (GroupInfoBean tempBean :mContactModels) {
                                if (!ids.contains(tempBean.userId)) {
                                    tempArray.add(tempBean);
                                }
                            }
                            mContactModels = tempArray;
                        } else {

                        }
                        adapter.contacts = mContactModels;
                        adapter.setItems(mContactModels);
                        adapter.notifyDataSetChanged();
                    }

                    @Override
                    public void Failure(Call<NetData> call, Throwable t) {

                    }
                });
    }


    @Override
    protected void _initView() {
        RecyclerView mRecyclerView = binding.activityFunSelectedUserRv;
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        final PinnedHeaderDecoration decoration = new PinnedHeaderDecoration();
        decoration.registerTypePinnedHeader(1, new PinnedHeaderDecoration.PinnedHeaderCreator() {
            @Override
            public boolean create(RecyclerView parent, int adapterPosition) {
                return true;
            }
        });
        mRecyclerView.addItemDecoration(decoration);
        mRecyclerView.setAdapter(adapter);


        Activity that = this;
        adapter.setOnItemClickListener(new BaseQuickAdapter.OnItemClickListener<GroupInfoBean>() {
            @Override
            public void onClick(@NonNull BaseQuickAdapter<GroupInfoBean, ?> baseQuickAdapter, @NonNull View view, int i) {
                if (page_type == 4) {

                    Intent resultIntent = new Intent();
                    GroupInfoBean groupInfoBean1 = mContactModels.get(i);
                    resultIntent.putExtra("userInfo", new Gson().toJson(groupInfoBean1));
                    that.setResult(Activity.RESULT_OK, resultIntent);
                    that.finish();
                    finish();
                    return;
                }

                baseQuickAdapter.getItem(i).isSelected = !baseQuickAdapter.getItem(i).isSelected;
                int count = 0;
                for (GroupInfoBean tempInfoBean : baseQuickAdapter.getItems()) {
                    if (tempInfoBean.isSelected) {
                        count ++;
                    }

                }
                if (count > 0) {
                    binding.activityFunSelectedUserConfirmTv.setText("确定  " + count);
                } else  {

                    binding.activityFunSelectedUserConfirmTv.setText("确定");
                }
                adapter.notifyDataSetChanged();
            }
        });

        // 侧边设置相关
        WaveSideBarView mWaveSideBarView = binding.activityFunSelectedUserSideBar;
        mWaveSideBarView.setOnSelectIndexItemListener(new WaveSideBarView.OnSelectIndexItemListener() {
            @Override
            public void onSelectIndexItem(String letter) {
                for (int i = 0; i < mContactModels.size(); i++) {
                    if (mContactModels.get(i).getIndex().equals(letter)) {
                        ((LinearLayoutManager) mRecyclerView.getLayoutManager()).scrollToPositionWithOffset(i, 0);
                        return;
                    }
                }
            }
        });

        binding.activityFunSelectedUserSearchEt.addTextChangedListener(new TextWatcher() {
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
                    adapter.setItems(mContactModels);
                    adapter.notifyDataSetChanged();
                } else {
                    ArrayList<GroupInfoBean> tempArr = new ArrayList<>();
                    for (GroupInfoBean temp :
                            mContactModels) {
                        if (temp.name.contains(string)) {
                            tempArr.add(temp);
                        }
                    }
                    adapter.setItems(tempArr);
                    adapter.notifyDataSetChanged();
                }
            }
        });

    }


    @Override
    public void onClick(View view) {
        if (view == binding.activityFunSelectedUserNav.addCloseImageButton()){
            finish();
        } else if (view == binding.activityFunSelectedUserConfirmTv) {

            ArrayList list = new ArrayList();
            ArrayList nameList = new ArrayList();
            for (GroupInfoBean tempBen : mContactModels) {
                if (tempBen.isSelected) {
                    list.add(tempBen.userId);
                    if (nameList.size() < 3) {
                        nameList.add(tempBen.name);
                    }
                }
            }
            if (page_type == 1) {

                if (list.size() == 0) {
                    ToastUtils.toastMsg("请选择好友");
                    return;
                }

                DialogAlertUtil.showInputAlert(this,"温馨提示","请输入群聊名称", new DialogAlertUtil.InputAlertCallBack() {
                    @Override
                    public void inputText(String text) {
                        RegisterBean bean = new RegisterBean();
                        bean.members = list;
                        bean.groupName = text;
                        bean.groupHead = TeamIconUtils.getDefaultRandomIconUrl(true);
                        HttpUtil.apiW().group_createGroup(bean)
                                .enqueue(new CommonCallback<NetData>() {
                                    @Override
                                    public void Successful(Call<NetData> call, Response<NetData> response, NetData body) {
                                        finish();
                                        CustomMsgBean bean1 = new Gson().fromJson(body.data.toString(),CustomMsgBean.class);
                                        XKitRouter.withKey(RouterConstant.PATH_FUN_CHAT_TEAM_PAGE)
                                                .withParam(RouterConstant.CHAT_ID_KRY, bean1.groupId)
                                                .withContext(AppProxy.getInstance().getContext())
                                                .navigate();
                                    }

                                    @Override
                                    public void Failure(Call<NetData> call, Throwable t) {
                                    }
                                });
                    }
                });

            } else if (page_type == 2) {
                RegisterBean bean = new RegisterBean();
                bean.groupId = groupInfoBean.groupId;
                bean.members = list;
                HttpUtil.apiW().group_pullPeopleGroup(bean)
                        .enqueue(new CommonCallback<NetData>() {
                            @Override
                            public void Successful(Call<NetData> call, Response<NetData> response, NetData body) {
                                ToastUtils.toastMsg(body.msg);

                                EventBus.getDefault().post(new BaseEvent("reloadTeamSettingData"));
                                finish();
                            }

                            @Override
                            public void Failure(Call<NetData> call, Throwable t) {

                            }
                        });
            } else if (page_type == 3) {
                RegisterBean registerBean = new RegisterBean();
                registerBean.groupId = groupInfoBean.groupId;
                registerBean.members = list;
                HttpUtil.apiW().group_outGroup(registerBean)
                        .enqueue(new CommonCallback<NetData>() {
                            @Override
                            public void Successful(Call<NetData> call, Response<NetData> response, NetData body) {

                                ToastUtils.toastMsg(body.msg);

                                EventBus.getDefault().post(new BaseEvent("reloadTeamSettingData"));
                                finish();
                            }

                            @Override
                            public void Failure(Call<NetData> call, Throwable t) {

                            }
                        });
            }
        }
    }
}
