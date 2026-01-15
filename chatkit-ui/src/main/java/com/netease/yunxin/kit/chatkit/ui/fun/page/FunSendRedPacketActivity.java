package com.netease.yunxin.kit.chatkit.ui.fun.page;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.text.method.DigitsKeyListener;
import android.util.Log;
import android.view.Gravity;
import android.view.View;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;

import com.flyco.tablayout.CommonTabLayout;
import com.flyco.tablayout.listener.CustomTabEntity;
import com.google.gson.Gson;
import com.netease.yunxin.kit.chatkit.model.UserInfoWithTeam;
import com.netease.yunxin.kit.chatkit.repo.TeamRepo;
import com.netease.yunxin.kit.chatkit.ui.databinding.ActivityFunSendRedPacketBinding;
import com.netease.yunxin.kit.corekit.im.model.UserInfo;
import com.netease.yunxin.kit.corekit.im.provider.FetchCallback;
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
import com.yaoxin.appbase.pswkeyboard.OnPasswordInputFinish;
import com.yaoxin.appbase.pswkeyboard.widget.PopEnterPassword;
import com.yaoxin.appbase.utils.DataUtil;
import com.yaoxin.appbase.utils.GlideUtil;
import com.yaoxin.appbase.utils.NumberUtil;
import com.yaoxin.appbase.utils.StatusBarUtils;
import com.yaoxin.appbase.utils.ToastUtils;
import com.yaoxin.appbase.view.pwdkeyboard.Keyboard;
import com.yaoxin.appbase.view.pwdkeyboard.PayEditText;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

import retrofit2.Call;
import retrofit2.Response;

public class FunSendRedPacketActivity extends BaseActivity implements View.OnClickListener {
    ActivityFunSendRedPacketBinding binding;
    //0： 个人 1：拼手气  2：专属
    private int type = 0;
    private int sessionType = 0;
    private String sessionId = "";
    private String toUserId = "";
    private String selectToUserId = "";
    private UserInfo targetUserInfo;
    protected ActivityResultLauncher<Intent> forwardTeamLauncher;
    private static final String[] KEY = new String[]{"1", "2", "3", "4", "5", "6", "7", "8", "9", "<<", "0", "完成"};

    private PayEditText payEditText;
    private Keyboard keyboard;

    private GroupInfoBean groupInfoBean;

    ArrayList<GroupInfoBean> userList = new ArrayList<>();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityFunSendRedPacketBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        payEditText = binding.PayEditTextPay;
        keyboard = binding.KeyboardViewPay;
        if (extras.get("sessionId") != null) {
            sessionId = (String) extras.get("sessionId");
        }
        if (extras.get("sessionType") != null) {
            String sessT = (String) extras.get("sessionType");
            sessionType = Integer.parseInt(sessT);
            if (sessionType == 1) {
                type = 1;
                //_requestDataGroup();
            }
            if (sessionType == 2) {
                type = 2;
                //_requestDataGroup();
            }
        }
        if (extras.get("userInfo") != null) {
            String tempUserInfoString = (String) extras.get("userInfo");
            targetUserInfo = new Gson().fromJson(tempUserInfoString, UserInfo.class);
        }
        forwardTeamLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            if (result.getResultCode() != Activity.RESULT_OK) {
                return;
            }
            Intent data = result.getData();
            if (data != null) {
                Bundle extras1 = data.getExtras();
//                                GroupInfoBean groupInfoBean1 =
                String userInfo1 = data.getStringExtra("userInfo");
                if (userInfo1 == null) return;
                GroupInfoBean userInfo = new Gson().fromJson(userInfo1, GroupInfoBean.class);
                if (userInfo != null) {
                    binding.activityFunSendRedPacketToPeopleNameTv.setText(userInfo.name);
                    GlideUtil.yh_loadImageRoundedCorner(this, binding.activityFunSendRedPacketToPeopleHeadIv, userInfo.avatar, 15);
                    selectToUserId = userInfo.userId;
                }
            }
        });
        _initView();
        StatusBarUtils.transtStatusBar(this, binding.activityFunSendRedPacketNav);
        showTable();
        if (type != 0) {
            binding.tvShouqi.setOnClickListener(view -> {
                type = 1;
                showTable();
            });

            binding.tvZhuanshu.setOnClickListener(view -> {
                type = 2;
                showTable();
            });
        }
    }

    private void showTable() {
        if (type == 0) {
            binding.tvShouqi.setText("转账");
            binding.layoutZhuanShu.setVisibility(View.INVISIBLE);
        } else if (type == 1) {
            binding.tvShouqi.setTextColor(Color.parseColor("#000000"));
            binding.viewLine.setVisibility(View.VISIBLE);
            binding.tvZhuanshu.setTextColor(Color.parseColor("#666666"));
            binding.viewLine1.setVisibility(View.INVISIBLE);
            _requestDataGroup();
        } else if (type == 2) {
            binding.tvShouqi.setTextColor(Color.parseColor("#666666"));
            binding.viewLine.setVisibility(View.INVISIBLE);
            binding.tvZhuanshu.setTextColor(Color.parseColor("#000000"));
            binding.viewLine1.setVisibility(View.VISIBLE);
            _requestDataGroup();
        }
        _updateUI();
    }

    void _requestPeople() {
        TeamRepo.getMemberList(sessionId, new FetchCallback<List<UserInfoWithTeam>>() {
            @Override
            public void onSuccess(@Nullable List<UserInfoWithTeam> param) {
                binding.activityFunSendRedPacketTeamMemberCountTv.setText("本群共" + param.size() + "人");
            }

            @Override
            public void onFailed(int code) {
            }

            @Override
            public void onException(@Nullable Throwable exception) {
            }
        });
//        RegisterBean bean = new RegisterBean();
//        bean.groupId = sessionId;
//        bean.page = page +"";
//        bean.pageNo ="100";
//
//        HttpUtil.apiW().group_groupUserListPost(bean)
//                .enqueue(new CommonCallback<NetData>() {
//                    @Override
//                    public void Successful(Call<NetData> call, Response<NetData> response, NetData body) {
//
//                        Type type = new TypeToken<List<GroupInfoBean>>() {
//                        }.getType();
//                        List<GroupInfoBean> tempList = new Gson().fromJson(body.data.toString(), type);
//                        if (!tempList.isEmpty()) {
//                            userList.addAll(tempList);
//                            if (tempList.size() == 100) {
//                                _requestPeople((page + 1));
//                                return;
//                            }
//                        }
//                        binding.activityFunSendRedPacketTeamMemberCountTv.setText("本群共"+userList.size()+"人");
//
//
//
//                    }
//
//                    @Override
//                    public void Failure(Call<NetData> call, Throwable t) {
//
//                    }
//                });
    }

    void _requestDataGroup() {
        HttpUtil.apiW().group_groupHomeInfo(sessionId).enqueue(new CommonCallback<NetData>() {
            @Override
            public void Successful(Call<NetData> call, Response<NetData> response, NetData body) {
                groupInfoBean = new Gson().fromJson(body.data.toString(), GroupInfoBean.class);
                _requestPeople();
            }

            @Override
            public void Failure(Call<NetData> call, Throwable t) {

            }
        });
    }

    @Override
    protected void _requestData() {
        HttpUtil.apiW().home_balance().enqueue(new CommonCallback<NetData>() {
            @Override
            public void Successful(Call<NetData> call, Response<NetData> response, NetData body) {
                UserBean bean = new Gson().fromJson(body.data.toString(), UserBean.class);
                binding.activityFunSendRedPacketBalanceTv.setText(NumberUtil.formartMoney(bean.balance));
            }

            @Override
            public void Failure(Call<NetData> call, Throwable t) {

            }
        });

    }

    @Override
    protected void _initView() {
        binding.activityFunSendRedPacketNetworkFeeLl.setVisibility(View.GONE);
        binding.activityFunSendRedPacketNav.addCloseImageButton().setOnClickListener(this);
//        binding.activityFunSendRedPacketPinChangeTypeLl.setOnClickListener(this);
        binding.activityFunSendRedPacketSendTv.setOnClickListener(this);
        binding.activityFunSendRedPacketMoneyEt.setInputType(InputType.TYPE_NUMBER_FLAG_DECIMAL);
        binding.activityFunSendRedPacketMoneyEt.setKeyListener(DigitsKeyListener.getInstance("0123456789."));

        binding.activityFunSendRedPacketCountEt.setInputType(InputType.TYPE_CLASS_NUMBER);
        binding.activityFunSendRedPacketToPeopleLl.setOnClickListener(this);
        binding.activityFunSendRedPacketMoneyEt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {

                String input = s.toString();
                String formattedValue;
                int dotCount = input.length() - input.replace(".", "").length();
                if (dotCount > 1) {
                    // 找到第一个小数点的位置
                    int firstDotIndex = input.indexOf(".");
                    // 移除第一个小数点之后的所有小数点
                    StringBuilder cleanedInput = new StringBuilder(input.substring(0, firstDotIndex + 1));
                    for (int i = firstDotIndex + 1; i < input.length(); i++) {
                        if (input.charAt(i) != '.') {
                            cleanedInput.append(input.charAt(i));
                        }
                    }
                    // 设置过滤后的文本
                    binding.activityFunSendRedPacketMoneyEt.setText(cleanedInput.toString());
                    binding.activityFunSendRedPacketMoneyEt.setSelection(cleanedInput.length());
                    formattedValue = String.format("%.2f", Double.parseDouble(cleanedInput.toString()));
                } else {
                    if (!input.isEmpty()) {
                        formattedValue = String.format("%.2f", Double.parseDouble(input));
                    } else {
                        formattedValue = "0.00";
                    }
                }

                binding.activityFunSendRedPacketTotalTv.setText(formattedValue);
                // 更新网络费显示
                updateNetworkFee(formattedValue);
            }
        });

        // 初始化网络费显示（金额为0时，网络费为0）
        updateNetworkFee("0.00");

        keyboard.setKeyboardKeys(KEY);
        keyboard.setOnClickKeyboardListener(new Keyboard.OnClickKeyboardListener() {
            @Override
            public void onKeyClick(int position, String value) {
                if (position < 11 && position != 9) {
                    payEditText.add(value);
                } else if (position == 9) {
                    payEditText.remove();
                } else if (position == 11) {
                    //当点击完成的时候，也可以通过payEditText.getText()获取密码，此时不应该注册OnInputFinishedListener接口
//                    Toast.makeText(getApplication(), "您的密码是：" + payEditText.getText(), Toast.LENGTH_SHORT).show();
//                    finish();

                    binding.activityFunSendRedPacketKeybordRl.setVisibility(View.GONE);
                }
            }
        });

        /**
         * 当密码输入完成时的回调
         */
        payEditText.setOnInputFinishedListener(new PayEditText.OnInputFinishedListener() {
            @Override
            public void onInputFinished(String password) {
//                Toast.makeText(getApplication(), "您的密码是：" + password, Toast.LENGTH_SHORT).show();
                sendRedWithPwd(password);
                payEditText.remove();
                binding.activityFunSendRedPacketKeybordRl.setVisibility(View.GONE);
            }
        });
    }

    /**
     * 根据金额计算网络费并更新显示
     * 扣费规则: 10元以上0.01-30元以上0.02-100以上0.03-200以上0.05
     */
    private void updateNetworkFee(String amountStr) {
        double networkFee = 0.0; // 默认0
        try {
            double amount = Double.parseDouble(amountStr);
            if (amount > 200) {
                networkFee = 0.05;
            } else if (amount > 100) {
                networkFee = 0.03;
            } else if (amount > 30) {
                networkFee = 0.02;
            } else if (amount > 10) {
                networkFee = 0.01;
            } else {
                networkFee = 0.0; // 10元及以下为0
            }
        } catch (Exception e) {
            networkFee = 0.0;
        }

        String feeText = String.format("收发送方%.2f", networkFee);
        binding.activityFunSendRedPacketNetworkFeeTv.setText(feeText);
    }

    private void _updateUI() {
        //0： 个人 1：拼手气  2：专属
        if (type == 0) {
            binding.activityFunSendRedPacketPinLl.setVisibility(View.GONE);
            binding.activityFunSendRedPacketMoneyTv.setText("金额");
            binding.activityFunSendRedPacketGreetingLl.setVisibility(View.VISIBLE);
        } else if (type == 1) {
            binding.activityFunSendRedPacketPinLl.setVisibility(View.VISIBLE);
            binding.activityFunSendRedPacketToPeopleLl.setVisibility(View.GONE);
            binding.activityFunSendRedPacketGreetingLl.setVisibility(View.VISIBLE);
            binding.activityFunSendRedPacketCountLl.setVisibility(View.VISIBLE);
//            binding.activityFunSendRedPacketPinChangeTypeTv.setText("拼手气红包");
        } else if (type == 2) {
            if (targetUserInfo != null) {
                binding.activityFunSendRedPacketToPeopleNameTv.setText(targetUserInfo.getName());
                GlideUtil.yh_loadImageRoundedCorner(this, binding.activityFunSendRedPacketToPeopleHeadIv, targetUserInfo.getAvatar(), 15);
                selectToUserId = targetUserInfo.getAccount();
            }
            binding.activityFunSendRedPacketToPeopleLl.setVisibility(View.VISIBLE);
            binding.activityFunSendRedPacketCountLl.setVisibility(View.GONE);
            binding.activityFunSendRedPacketGreetingLl.setVisibility(View.VISIBLE);
//            binding.activityFunSendRedPacketPinChangeTypeTv.setText("专属红包");
        }
    }

    @Override
    public void onClick(View v) {
        if (v == binding.activityFunSendRedPacketNav.addCloseImageButton()) {
            finish();
        }
//        else if (v == binding.activityFunSendRedPacketPinChangeTypeLl) {
//            ActionSheet.createBuilder(this, getSupportFragmentManager())
//                    .setCancelButtonTitle("取消")
//                    .setOtherButtonTitles("拼手气红包", "专属红包")
//                    .setCancelableOnTouchOutside(true)
//                    .setListener(new ActionSheet.ActionSheetListener() {
//                        @Override
//                        public void onDismiss(ActionSheet actionSheet, boolean isCancel) {
//
//                        }
//
//                        @Override
//                        public void onOtherButtonClick(ActionSheet actionSheet, int index) {
//                            if (index == 0) {
//                                type = 1;
//                            } else if (index == 1) {
//                                type = 2;
//                            }
//                            _updateUI();
//
//                        }
//                    }).show();
//        }
        else if (v == binding.activityFunSendRedPacketSendTv) {
            String moneyStr = getTextStr(binding.activityFunSendRedPacketMoneyEt);

            if (moneyStr.isEmpty()) {
                ToastUtils.toastMsg("请输入金额");
                return;
            }
//            binding.activityFunSendRedPacketKeybordRl.setVisibility(View.VISIBLE);
            PopEnterPassword popEnterPassword = new PopEnterPassword(this, new OnPasswordInputFinish() {
                @Override
                public void inputFinish(String password) {
                    sendRedWithPwd(password);
                }
            }, moneyStr);
            // 显示窗口
            popEnterPassword.showAtLocation(binding.activityFunSendRedPacketLl, Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL, 0, 0); // 设置layout在PopupWindow中显示的位置


        } else if (v == binding.activityFunSendRedPacketToPeopleLl) {

            DataUtil.setStringValue(new Gson().toJson(groupInfoBean), "groupInfo");
            XKitRouter.withKey(Constant.FunSelected_User_ActivityKey).withParam("type", "4").withParam("groupId", sessionId).withContext(this).navigate(forwardTeamLauncher);
//            XKitRouter.withKey(Constant.TeamMemberListActivity_Router)
//                    .withParam("sessionId",sessionId)
//                    .withParam("b_type","1")
//                    .withContext(this)
//                    .navigate(forwardTeamLauncher);
        }
    }

    void sendRedWithPwd(String pwd) {
        String countStr = getTextStr(binding.activityFunSendRedPacketCountEt);
        String moneyStr = getTextStr(binding.activityFunSendRedPacketMoneyEt);
        String greeting = getTextStr(binding.activityFunSendRedPacketGreetingEt);

        int amout = 0;
        if (!moneyStr.isEmpty()) {
            amout = NumberUtil.formartUploadMoney(moneyStr);
        }

        if (amout <= 0) {
            ToastUtils.toastMsg("请输入金额");
            return;
        }
        RegisterBean bean = new RegisterBean();
        bean.amount = amout;
        bean.title = greeting.isEmpty() ? "恭喜发财,大吉大利" : greeting;
        if (type == 0) {
            bean.password = pwd;
            bean.toUserId = sessionId;
            HttpUtil.apiW().red_personRedpacket(bean).enqueue(new CommonCallback<NetData>() {
                @Override
                public void Successful(Call<NetData> call, Response<NetData> response, NetData body) {
                    ToastUtils.toastMsg("发送成功");
                    finish();
                }

                @Override
                public void Failure(Call<NetData> call, Throwable t) {
                    if (t instanceof NetServerException) {
                        if (((NetServerException) t).getErrCode() == 8008) {
                            try {
                                Intent intent = new Intent();
                                intent.putExtra("type", "0");
                                intent.setClassName(getPackageName(), "com.turunsi.yaoxin.main.mine.purse.pwdmanager.PursePwdManagerSetActivity");
                                startActivity(intent);
                            } catch (Exception e) {
                                // 处理找不到Activity的情况
                            }
                        }
                    }
                }
            });
        } else if (type == 1) {
            int count = Integer.parseInt(countStr);
            if (count <= 0) {
                ToastUtils.toastMsg("请输入份数");
                return;
            }
            bean.groupId = sessionId;
            bean.num = count;
            bean.tradePassword = pwd;
            HttpUtil.apiW().red_sendGroupRedpacket(bean).enqueue(new CommonCallback<NetData>() {
                @Override
                public void Successful(Call<NetData> call, Response<NetData> response, NetData body) {
                    ToastUtils.toastMsg("发送成功");
                    finish();
                }

                @Override
                public void Failure(Call<NetData> call, Throwable t) {
                    if (t instanceof NetServerException) {
                        if (((NetServerException) t).getErrCode() == 8008) {
                            try {
                                Intent intent = new Intent();
                                intent.putExtra("type", "0");
                                intent.setClassName(getPackageName(), "com.turunsi.yaoxin.main.mine.purse.pwdmanager.PursePwdManagerSetActivity");
                                startActivity(intent);
                            } catch (Exception e) {
                                // 处理找不到Activity的情况
                            }
                        }
                    }
                }
            });
        } else if (type == 2) {
            if (selectToUserId == null || selectToUserId.isEmpty()) {
                ToastUtils.toastMsg("请选择成员");
                return;
            }
            bean.password = pwd;
            bean.toUserId = selectToUserId;
            bean.groupId = sessionId;
            HttpUtil.apiW().red_sendExclusiveRedPacket(bean).enqueue(new CommonCallback<NetData>() {
                @Override
                public void Successful(Call<NetData> call, Response<NetData> response, NetData body) {
                    ToastUtils.toastMsg("发送成功");
                    finish();
                }

                @Override
                public void Failure(Call<NetData> call, Throwable t) {

                }
            });

        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }
}
