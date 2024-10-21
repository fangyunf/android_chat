// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.contactkit.ui.fun.verify;

import android.app.Activity;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.chad.library.adapter4.BaseQuickAdapter;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.netease.nimlib.sdk.msg.constant.SessionTypeEnum;
import com.netease.yunxin.kit.contactkit.ui.ILoadListener;
import com.netease.yunxin.kit.contactkit.ui.R;
import com.netease.yunxin.kit.contactkit.ui.databinding.BaseListActivityLayoutBinding;
import com.netease.yunxin.kit.contactkit.ui.databinding.FunAddFriendVerifyActivityBinding;
import com.netease.yunxin.kit.contactkit.ui.databinding.FunVerifyFriendListActivityBinding;
import com.netease.yunxin.kit.contactkit.ui.fun.addfriend.FunAddFriendVerifyActivity;
import com.netease.yunxin.kit.contactkit.ui.fun.verify.adapter.FunVerifyFriendListAdapter;
import com.netease.yunxin.kit.contactkit.ui.fun.view.FunContactViewHolderFactory;
import com.netease.yunxin.kit.contactkit.ui.fun.view.viewholder.FunVerifyInfoViewHolder;
import com.netease.yunxin.kit.contactkit.ui.model.ContactVerifyInfoBean;
import com.netease.yunxin.kit.contactkit.ui.model.IViewTypeConstant;
import com.netease.yunxin.kit.contactkit.ui.verify.BaseVerifyListActivity;
import com.netease.yunxin.kit.contactkit.ui.view.viewholder.BaseContactViewHolder;
import com.netease.yunxin.kit.corekit.im.model.SystemMessageInfoStatus;
import com.netease.yunxin.kit.corekit.im.model.SystemMessageInfoType;
import com.netease.yunxin.kit.corekit.im.provider.FetchCallback;
import com.netease.yunxin.kit.corekit.im.utils.RouterConstant;
import com.netease.yunxin.kit.corekit.route.XKitRouter;
import com.yaoxin.appbase.activity.BaseActivity;
import com.yaoxin.appbase.model.NetData;
import com.yaoxin.appbase.model.RegisterBean;
import com.yaoxin.appbase.model.UserBean;
import com.yaoxin.appbase.net.CommonCallback;
import com.yaoxin.appbase.net.HttpUtil;

import java.util.HashMap;
import java.util.List;

import retrofit2.Call;
import retrofit2.Response;

public class FunVerifyListActivity extends BaseActivity implements View.OnClickListener {

    FunVerifyFriendListActivityBinding binding;
    FunVerifyFriendListAdapter adapter = new FunVerifyFriendListAdapter();
    int type = 0;
  @Override
  protected void onCreate(@Nullable Bundle savedInstanceState) {
      String type1 = getIntent().getStringExtra("type");
      if (type1 != null) {
          type = Integer.parseInt(type1);
          adapter.business_type = type;
      }
      super.onCreate(savedInstanceState);
      binding = FunVerifyFriendListActivityBinding.inflate(getLayoutInflater());
      setContentView(binding.getRoot());
      binding.funVerifyFriendListActivityNav.addCloseImageButton().setOnClickListener(this);

      binding.funVerifyFriendListActivityRv.setLayoutManager(new LinearLayoutManager(this));
      binding.funVerifyFriendListActivityRv.setAdapter(adapter);
      Activity that = this;
      adapter.addOnItemChildClickListener(R.id.fun_verify_friend_list_cell_opt_rl, new BaseQuickAdapter.OnItemChildClickListener<UserBean>() {
          @Override
          public void onItemClick(@NonNull BaseQuickAdapter<UserBean, ?> baseQuickAdapter, @NonNull View view, int i) {
              UserBean bean = baseQuickAdapter.getItem(i);
              bean.page_type = type == 1 ? 101 : 100;
              HashMap map = new HashMap();
              map.put("user",new Gson().toJson(bean));
              FunAddFriendVerifyActivity.start(FunAddFriendVerifyActivity.class,that,map);
          }
      });
  }

    @Override
    protected void _requestData() {
      if (type == 1) {

          HttpUtil.api8446().group_applyGroups("",0,1000)
                  .enqueue(new CommonCallback<NetData>() {
                      @Override
                      public void Successful(Call<NetData> call, Response<NetData> response, NetData body) {
                          NetData listData = new Gson().fromJson(new Gson().toJson(body.data),NetData.class);
                          Gson gson = new Gson();
                          List<UserBean> verifyList =
                                  gson.fromJson(new Gson().toJson(listData.data), new TypeToken<List<UserBean>>() {
                                  }.getType());
                          adapter.setItems(verifyList);
                          adapter.notifyDataSetChanged();
                      }

                      @Override
                      public void Failure(Call<NetData> call, Throwable t) {

                      }
                  });
      } else {

          HttpUtil.api8444().friends_applyList(0,100)
                  .enqueue(new CommonCallback<NetData>() {
                      @Override
                      public void Successful(Call<NetData> call, Response<NetData> response, NetData body) {
                          String jsonStr = new Gson().toJson(body.data);
                          UserBean listData = new Gson().fromJson(jsonStr,UserBean.class);
                          List<UserBean> verifyList = listData.applyInfos;
                          adapter.setItems(verifyList);
                          adapter.notifyDataSetChanged();
                      }

                      @Override
                      public void Failure(Call<NetData> call, Throwable t) {

                      }
                  });
      }
    }

    @Override
    public void onClick(View v) {
        if (v == binding.funVerifyFriendListActivityNav.addCloseImageButton()) {
            finish();
        }
    }
//
//  protected void configTitle(BaseListActivityLayoutBinding binding) {
//    super.configTitle(binding);
//    binding.title.getTitleTextView().setTextSize(17);
//    binding.title.getTitleTextView().setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));
//    binding.title.setBackgroundResource(R.color.color_ededed);
//  }
//
//  protected int getEmptyStateViewRes() {
//    return R.drawable.fun_ic_contact_empty;
//  }
//
//  @Override
//  protected void configViewHolderFactory() {
//    binding.contactListView.setViewHolderFactory(
//        new FunContactViewHolderFactory() {
//          @Override
//          protected BaseContactViewHolder getCustomViewHolder(ViewGroup view, int viewType) {
//            if (viewType == IViewTypeConstant.CONTACT_VERIFY_INFO) {
//              FunVerifyInfoViewHolder viewHolder = new FunVerifyInfoViewHolder(view);
//              viewHolder.setVerifyListener(
//                  new FunVerifyInfoViewHolder.VerifyListener() {
//                    @Override
//                    public void onAccept(ContactVerifyInfoBean bean) {
//                      viewModel.agree(
//                          bean,
//                          new FetchCallback<Void>() {
//                            @Override
//                            public void onSuccess(@Nullable Void param) {
//                              viewModel.setVerifyStatus(bean, SystemMessageInfoStatus.Passed);
//                              binding.contactListView.updateContactData(bean);
//                              if (bean.data.getInfoType() == SystemMessageInfoType.AddFriend) {
//                                XKitRouter.withKey(RouterConstant.PATH_CHAT_SEND_TEXT_ACTION)
//                                    .withContext(FunVerifyListActivity.this)
//                                    .withParam(
//                                        RouterConstant.KEY_SESSION_ID, bean.data.getFromAccount())
//                                    .withParam(
//                                        RouterConstant.KEY_SESSION_TYPE,
//                                        SessionTypeEnum.P2P.getValue())
//                                    .withParam(
//                                        RouterConstant.KEY_MESSAGE_CONTENT,
//                                        getResources()
//                                            .getString(R.string.verify_agree_message_text))
//                                    .navigate();
//                              }
//                            }
//
//                            @Override
//                            public void onFailed(int code) {
//                              if (code == error_duplicate) {
//                                viewModel.setVerifyStatus(bean, SystemMessageInfoStatus.Passed);
//                                binding.contactListView.updateContactData(bean);
//                              }
//                              toastResult(true, bean.data.getInfoType(), code);
//                            }
//
//                            @Override
//                            public void onException(@Nullable Throwable exception) {
//                              toastResult(true, bean.data.getInfoType(), 0);
//                            }
//                          });
//                    }
//
//                    @Override
//                    public void onReject(ContactVerifyInfoBean bean) {
//                      viewModel.disagree(
//                          bean,
//                          new FetchCallback<Void>() {
//                            @Override
//                            public void onSuccess(@Nullable Void param) {
//                              viewModel.setVerifyStatus(bean, SystemMessageInfoStatus.Declined);
//                              binding.contactListView.updateContactData(bean);
//                            }
//
//                            @Override
//                            public void onFailed(int code) {
//                              if (code == error_duplicate) {
//                                viewModel.setVerifyStatus(bean, SystemMessageInfoStatus.Passed);
//                                binding.contactListView.updateContactData(bean);
//                              }
//                              toastResult(false, bean.data.getInfoType(), code);
//                            }
//
//                            @Override
//                            public void onException(@Nullable Throwable exception) {
//                              toastResult(false, bean.data.getInfoType(), 0);
//                            }
//                          });
//                    }
//                  });
//              return viewHolder;
//            }
//            return null;
//          }
//        });
//  }
}
