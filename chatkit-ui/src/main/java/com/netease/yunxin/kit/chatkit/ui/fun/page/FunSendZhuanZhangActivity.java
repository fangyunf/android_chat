package com.netease.yunxin.kit.chatkit.ui.fun.page;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.text.TextUtils;
import android.text.method.DigitsKeyListener;
import android.view.Gravity;
import android.view.View;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.netease.yunxin.kit.chatkit.ui.common.MessageHelper;
import com.netease.yunxin.kit.chatkit.ui.databinding.ActivityFunSendZhuanzhangPacketBinding;
import com.netease.yunxin.kit.corekit.im.model.UserInfo;
import com.netease.yunxin.kit.corekit.route.XKitRouter;
import com.yaoxin.appbase.activity.BaseActivity;
import com.yaoxin.appbase.model.GroupInfoBean;
import com.yaoxin.appbase.model.NetData;
import com.yaoxin.appbase.model.RegisterBean;
import com.yaoxin.appbase.model.UserBean;
import com.yaoxin.appbase.net.CommonCallback;
import com.yaoxin.appbase.net.Constant;
import com.yaoxin.appbase.net.HttpUtil;
import com.yaoxin.appbase.net.NetServerException;
import com.yaoxin.appbase.pswkeyboard.widget.PopEnterPassword;
import com.yaoxin.appbase.utils.DataUtil;
import com.yaoxin.appbase.utils.GlideUtil;
import com.yaoxin.appbase.utils.NumberUtil;
import com.yaoxin.appbase.utils.StatusBarUtils;
import com.yaoxin.appbase.utils.ToastUtils;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import retrofit2.Call;
import retrofit2.Response;

public class FunSendZhuanZhangActivity extends BaseActivity implements View.OnClickListener {

  ActivityFunSendZhuanzhangPacketBinding binding;
  protected ActivityResultLauncher<Intent> forwardTeamLauncher;
  private String sessionId = "";
  /** 0 个人 / 1 群内选人 / 2 专属（已指定收款人） */
  private int sessionType = 0;
  private String selectToUserId = "";
  private UserBean targetUserBean;
  private GroupInfoBean groupInfoBean;
  private GroupInfoBean exclusiveTargetUser;
  private boolean loadingGroupMembers;

  @Override
  protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    binding = ActivityFunSendZhuanzhangPacketBinding.inflate(getLayoutInflater());
    setContentView(binding.getRoot());
    StatusBarUtils.transtStatusBar(this, binding.activityFunSendRedPacketNav);
    if (extras != null && extras.get("sessionId") != null) {
      sessionId = (String) extras.get("sessionId");
    }
    if (extras != null && extras.get("sessionType") != null) {
      try {
        sessionType = Integer.parseInt(String.valueOf(extras.get("sessionType")));
      } catch (Exception ignored) {
        sessionType = 0;
      }
    }
    if (extras != null && extras.get("userInfo") != null) {
      exclusiveTargetUser = parseExclusiveTarget((String) extras.get("userInfo"));
    }
    forwardTeamLauncher =
        registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
              if (result.getResultCode() != Activity.RESULT_OK) {
                return;
              }
              Intent data = result.getData();
              if (data == null) {
                return;
              }
              String userInfoJson = data.getStringExtra("userInfo");
              if (TextUtils.isEmpty(userInfoJson)) {
                return;
              }
              GroupInfoBean userInfo = new Gson().fromJson(userInfoJson, GroupInfoBean.class);
              updateSelectedGroupMember(userInfo, sessionType != 2);
            });
    _initView();
    _requestUserInfo();
    _requestData();
  }

  @Override
  protected void _requestData() {
    HttpUtil.apiW()
        .home_balance()
        .enqueue(
            new CommonCallback<NetData>() {
              @Override
              public void Successful(Call<NetData> call, Response<NetData> response, NetData body) {
                if (binding == null || body == null || body.data == null) {
                  return;
                }
                UserBean bean = new Gson().fromJson(body.data.toString(), UserBean.class);
                if (bean != null) {
                  binding.activityFunSendRedPacketBalanceTv.setText(
                      NumberUtil.formartMoney(bean.balance));
                }
              }

              @Override
              public void Failure(Call<NetData> call, Throwable t) {}
            });
  }

  private void _requestUserInfo() {
    if (sessionType == 1 || sessionType == 2) {
      binding
          .activityFunSendRedPacketNav
          .getTitleView()
          .setText(sessionType == 2 ? "专属转账" : "群内转账");
      binding.activityFunSendRedPacketToPeopleNameTv.setText("请选择收款人");
      binding.activityFunSendZhuanzhangLiushuihaoTv.setVisibility(View.VISIBLE);
      binding.activityFunSendZhuanzhangLiushuihaoTv.setText(
          sessionType == 2 ? "指定收款人" : "点击选择收款人");
      if (sessionType == 2 && exclusiveTargetUser != null) {
        updateSelectedGroupMember(exclusiveTargetUser, false);
      }
      loadGroupMembers();
      return;
    }
    if (TextUtils.isEmpty(sessionId)) {
      return;
    }
    // 个人转账：不走 friends/searchByUserId，用好友缓存 / 云信资料展示
    List<GroupInfoBean> friends = DataUtil.getFriendInfoList();
    if (friends != null) {
      for (GroupInfoBean friend : friends) {
        if (friend != null
            && !TextUtils.isEmpty(friend.userId)
            && TextUtils.equals(friend.userId, sessionId)) {
          targetUserBean = new UserBean();
          targetUserBean.userId = friend.userId;
          targetUserBean.name =
              !TextUtils.isEmpty(friend.remark) ? friend.remark : friend.name;
          targetUserBean.avatar = friend.avatar;
          bindTargetUserUi();
          return;
        }
      }
    }
    UserInfo nimUser = MessageHelper.getChatMessageUserInfo(sessionId);
    if (nimUser != null) {
      targetUserBean = new UserBean();
      targetUserBean.userId = sessionId;
      targetUserBean.name =
          !TextUtils.isEmpty(nimUser.getName()) ? nimUser.getName() : sessionId;
      targetUserBean.avatar = nimUser.getAvatar();
      bindTargetUserUi();
      return;
    }
    binding.activityFunSendRedPacketToPeopleNameTv.setText(sessionId);
  }

  private void bindTargetUserUi() {
    if (targetUserBean == null || binding == null) {
      return;
    }
    if (!TextUtils.isEmpty(targetUserBean.name)) {
      binding.activityFunSendRedPacketToPeopleNameTv.setText(targetUserBean.name);
    }
    if (!TextUtils.isEmpty(targetUserBean.avatar)) {
      GlideUtil.yh_loadImage(
          this, binding.activityFunSendRedPacketToPeopleHeadIv, targetUserBean.avatar);
    }
  }

  private void loadGroupMembers() {
    if (TextUtils.isEmpty(sessionId) || loadingGroupMembers) {
      return;
    }
    loadingGroupMembers = true;
    HttpUtil.apiW()
        .group_groupHomeInfo(sessionId)
        .enqueue(
            new CommonCallback<NetData>() {
              @Override
              public void Successful(Call<NetData> call, Response<NetData> response, NetData body) {
                if (body == null || body.data == null) {
                  loadingGroupMembers = false;
                  return;
                }
                groupInfoBean = new Gson().fromJson(body.data.toString(), GroupInfoBean.class);
                if (groupInfoBean == null) {
                  loadingGroupMembers = false;
                  return;
                }
                if (groupInfoBean.userInfos == null) {
                  groupInfoBean.userInfos = new ArrayList<>();
                } else {
                  groupInfoBean.userInfos.clear();
                }
                requestGroupUserPage(1);
              }

              @Override
              public void Failure(Call<NetData> call, Throwable t) {
                loadingGroupMembers = false;
              }
            });
  }

  private void requestGroupUserPage(int page) {
    RegisterBean bean = new RegisterBean();
    bean.groupId = sessionId;
    bean.page = String.valueOf(page);
    bean.pageNo = "100";
    HttpUtil.apiW()
        .group_groupUserListPost(bean)
        .enqueue(
            new CommonCallback<NetData>() {
              @Override
              public void Successful(Call<NetData> call, Response<NetData> response, NetData body) {
                if (groupInfoBean == null) {
                  loadingGroupMembers = false;
                  return;
                }
                if (groupInfoBean.userInfos == null) {
                  groupInfoBean.userInfos = new ArrayList<>();
                }
                if (body != null && body.data != null) {
                  Type type = new TypeToken<List<GroupInfoBean>>() {}.getType();
                  List<GroupInfoBean> tempList =
                      new Gson().fromJson(body.data.toString(), type);
                  if (tempList != null && !tempList.isEmpty()) {
                    groupInfoBean.userInfos.addAll(tempList);
                    if (tempList.size() == 100) {
                      requestGroupUserPage(page + 1);
                      return;
                    }
                  }
                }
                loadingGroupMembers = false;
              }

              @Override
              public void Failure(Call<NetData> call, Throwable t) {
                loadingGroupMembers = false;
              }
            });
  }

  @Nullable
  private GroupInfoBean parseExclusiveTarget(String userInfoJson) {
    if (TextUtils.isEmpty(userInfoJson)) {
      return null;
    }
    try {
      GroupInfoBean groupTarget = new Gson().fromJson(userInfoJson, GroupInfoBean.class);
      if (groupTarget != null && !TextUtils.isEmpty(groupTarget.userId)) {
        return groupTarget;
      }
    } catch (Exception ignored) {
    }
    try {
      UserInfo nimUser = new Gson().fromJson(userInfoJson, UserInfo.class);
      if (nimUser != null && !TextUtils.isEmpty(nimUser.getAccount())) {
        GroupInfoBean target = new GroupInfoBean();
        target.userId = nimUser.getAccount();
        target.name = nimUser.getName();
        target.avatar = nimUser.getAvatar();
        return target;
      }
    } catch (Exception ignored) {
    }
    return null;
  }

  @Override
  protected void _initView() {
    binding.activityFunSendRedPacketNav.addCloseImageButton().setOnClickListener(this);
    binding.activityFunSendRedPacketSendTv.setOnClickListener(this);
    binding.activityFunSendRedPacketToPeopleLl.setOnClickListener(this);
    binding.activityFunSendRedPacketMoneyEt.setInputType(InputType.TYPE_NUMBER_FLAG_DECIMAL);
    binding.activityFunSendRedPacketMoneyEt.setKeyListener(
        DigitsKeyListener.getInstance("0123456789."));
    binding.activityFunSendRedPacketGreetingEt.setHint("添加转账说明");
  }

  @Override
  public void onClick(View v) {
    if (v == binding.activityFunSendRedPacketNav.addCloseImageButton()) {
      finish();
    } else if (v == binding.activityFunSendRedPacketToPeopleLl) {
      if (sessionType != 1 && sessionType != 2) {
        return;
      }
      if (groupInfoBean == null
          || groupInfoBean.userInfos == null
          || groupInfoBean.userInfos.isEmpty()) {
        ToastUtils.toastMsg(loadingGroupMembers ? "群成员加载中" : "暂无群成员");
        if (!loadingGroupMembers) {
          loadGroupMembers();
        }
        return;
      }
      DataUtil.setStringValue(new Gson().toJson(groupInfoBean), "groupInfo");
      XKitRouter.withKey(Constant.FunSelected_User_ActivityKey)
          .withParam("type", "4")
          .withParam("groupId", sessionId)
          .withContext(this)
          .navigate(forwardTeamLauncher);
    } else if (v == binding.activityFunSendRedPacketSendTv) {
      if ((sessionType == 1 || sessionType == 2)
          && TextUtils.isEmpty(selectToUserId)) {
        ToastUtils.toastMsg("请选择收款人");
        return;
      }
      String moneyStr = getTextStr(binding.activityFunSendRedPacketMoneyEt);
      if (TextUtils.isEmpty(moneyStr)) {
        ToastUtils.toastMsg("请输入金额");
        return;
      }
      PopEnterPassword popEnterPassword =
          new PopEnterPassword(this, password -> sendZhuanZhangWithPwd(password), moneyStr);
      popEnterPassword.showAtLocation(
          binding.activityFunSendRedPacketLl, Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL, 0, 0);
    }
  }

  void sendZhuanZhangWithPwd(String pwd) {
    String moneyStr = getTextStr(binding.activityFunSendRedPacketMoneyEt);
    String greeting = getTextStr(binding.activityFunSendRedPacketGreetingEt);

    int amount = 0;
    if (!TextUtils.isEmpty(moneyStr)) {
      amount = NumberUtil.formartUploadMoney(moneyStr);
    }
    if (amount <= 0) {
      ToastUtils.toastMsg("请输入金额");
      return;
    }

    RegisterBean bean = new RegisterBean();
    bean.amount = amount;
    bean.title = TextUtils.isEmpty(greeting) ? "你发起了一笔转账" : greeting;
    bean.password = pwd;
    bean.tradePassword = pwd;
    if (sessionType == 1 || sessionType == 2) {
      if (TextUtils.isEmpty(selectToUserId)) {
        ToastUtils.toastMsg("请选择收款人");
        return;
      }
      bean.toUserId = selectToUserId;
      bean.groupId = sessionId;
      HttpUtil.apiW()
          .red_groupZZ(bean)
          .enqueue(
              new CommonCallback<NetData>() {
                @Override
                public void Successful(
                    Call<NetData> call, Response<NetData> response, NetData body) {
                  ToastUtils.toastMsg("发送成功");
                  finish();
                }

                @Override
                public void Failure(Call<NetData> call, Throwable t) {
                  if (handlePayPasswordNotSet(t)) {
                    return;
                  }
                  ToastUtils.toastMsg(t != null ? t.getMessage() : "发送失败");
                }
              });
      return;
    }
    bean.toUserId = sessionId;
    HttpUtil.apiW()
        .red_zz(bean)
        .enqueue(
            new CommonCallback<NetData>() {
              @Override
              public void Successful(Call<NetData> call, Response<NetData> response, NetData body) {
                ToastUtils.toastMsg("发送成功");
                finish();
              }

              @Override
              public void Failure(Call<NetData> call, Throwable t) {
                if (handlePayPasswordNotSet(t)) {
                  return;
                }
                ToastUtils.toastMsg(t != null ? t.getMessage() : "发送失败");
              }
            });
  }

  private void updateSelectedGroupMember(GroupInfoBean userInfo, boolean allowChangeHint) {
    if (userInfo == null || TextUtils.isEmpty(userInfo.userId)) {
      return;
    }
    if (TextUtils.equals(userInfo.userId, DataUtil.getUserid())) {
      ToastUtils.toastMsg("不能给自己转账");
      return;
    }
    selectToUserId = userInfo.userId;
    binding.activityFunSendRedPacketToPeopleNameTv.setText(
        !TextUtils.isEmpty(userInfo.name) ? userInfo.name : userInfo.userId);
    binding.activityFunSendZhuanzhangLiushuihaoTv.setVisibility(View.VISIBLE);
    binding.activityFunSendZhuanzhangLiushuihaoTv.setText(
        allowChangeHint ? "点击更换收款人" : "指定收款人");
    if (!TextUtils.isEmpty(userInfo.avatar)) {
      GlideUtil.yh_loadImageRoundedCorner(
          this, binding.activityFunSendRedPacketToPeopleHeadIv, userInfo.avatar, 6);
    }
  }

  private boolean handlePayPasswordNotSet(Throwable t) {
    if (!(t instanceof NetServerException) || ((NetServerException) t).getErrCode() != 8008) {
      return false;
    }
    try {
      Intent intent = new Intent();
      intent.putExtra("type", "0");
      intent.setClassName(
          getPackageName(),
          "com.turunsi.yaoxin.main.mine.purse.pwdmanager.PursePwdManagerSetActivity");
      startActivity(intent);
      return true;
    } catch (Exception ignored) {
      return false;
    }
  }
}
