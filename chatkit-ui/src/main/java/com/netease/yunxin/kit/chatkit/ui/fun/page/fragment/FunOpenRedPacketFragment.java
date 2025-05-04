package com.netease.yunxin.kit.chatkit.ui.fun.page.fragment;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentManager;

import com.google.gson.Gson;
import com.netease.nimlib.sdk.NIMClient;
import com.netease.nimlib.sdk.msg.MessageBuilder;
import com.netease.nimlib.sdk.msg.MsgService;
import com.netease.nimlib.sdk.msg.constant.SessionTypeEnum;
import com.netease.nimlib.sdk.msg.model.CustomMessageConfig;
import com.netease.nimlib.sdk.msg.model.IMMessage;
import com.netease.yunxin.kit.chatkit.model.IMMessageInfo;
import com.netease.yunxin.kit.chatkit.repo.ChatRepo;
import com.netease.yunxin.kit.chatkit.ui.R;
import com.netease.yunxin.kit.chatkit.ui.databinding.FragmentOpenRedPacketDialogBinding;
import com.netease.yunxin.kit.chatkit.ui.fun.page.FunRedPacketResultActivity;
import com.yaoxin.appbase.fragment.BaseDialogFragment;
import com.yaoxin.appbase.model.CustomMsgBean;
import com.yaoxin.appbase.model.NetData;
import com.yaoxin.appbase.model.RegisterBean;
import com.yaoxin.appbase.net.CommonCallback;
import com.yaoxin.appbase.net.HttpUtil;
import com.yaoxin.appbase.utils.AppProxy;
import com.yaoxin.appbase.utils.DataUtil;
import com.yaoxin.appbase.utils.GlideUtil;
import com.yaoxin.appbase.utils.ToastUtils;

import java.util.HashMap;

import retrofit2.Call;
import retrofit2.Response;

public class FunOpenRedPacketFragment extends BaseDialogFragment implements View.OnClickListener {

    public interface OpenRedPacketBlock {
        public void hasOpen(IMMessage messageInfo);
    }
    private OpenRedPacketBlock block;
    FragmentOpenRedPacketDialogBinding binding;
    private CustomMsgBean redBean;
    private CustomMsgBean redResultBean;
    private String redPacketId;
    private String groupId;
    private int type;
    CustomMsgBean sendBean;
    IMMessage messageInfo;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentOpenRedPacketDialogBinding.inflate(inflater, container, false);
        binding.fragmentOpenRedPacketDialogOpenRl.setOnClickListener(this);
        binding.fragmentOpenRedPacketDialogDetailRl.setOnClickListener(this);
        _requestData();
        return binding.getRoot();
    }
    void _requestData() {
        if (type == 1) {
            updateUI();
            return;
        }
        RegisterBean bean = new RegisterBean();
        bean.redpacketId = redPacketId;
        HttpUtil.apiW().red_redpacketDetail(bean)
                .enqueue(new CommonCallback<NetData>() {
                    @Override
                    public void Successful(Call<NetData> call, Response<NetData> response, NetData body) {
                        redBean = new Gson().fromJson(body.data.toString(), CustomMsgBean.class);

                        updateUI();

                    }

                    @Override
                    public void Failure(Call<NetData> call, Throwable t) {
                    }
                });
    }
    private void updateUI() {

        if (redBean != null) {

            GlideUtil.yh_loadImageRoundedCorner(getContext(),binding.fragmentOpenRedPacketDialogHeadIv,redBean.sendAvatar,24);
            binding.fragmentOpenRedPacketDialogNameTv.setText(redBean.sendName);
            binding.fragmentOpenRedPacketDialogGreetingTv.setText(redBean.title);
        }
        if (type == 1) {
            //可领取
            binding.fragmentOpenRedPacketDialogBgIv.setImageResource(R.drawable.chat_red_packet_open_bg_can_open);
            binding.fragmentOpenRedPacketDialogDetailRl.setVisibility(View.GONE);
            binding.fragmentOpenRedPacketDialogOpenRl.setVisibility(View.VISIBLE);
            GlideUtil.yh_loadImageRoundedCorner(getContext(),binding.fragmentOpenRedPacketDialogHeadIv,sendBean.result.sendAvatar,24);
            binding.fragmentOpenRedPacketDialogNameTv.setText(sendBean.result.sendName);
            binding.fragmentOpenRedPacketDialogGreetingTv.setText(sendBean.result.title);
        }
        if (type == 2) {
            //已领完
            binding.fragmentOpenRedPacketDialogGreetingTv.setText("手慢啦，红包已抢完");
            binding.fragmentOpenRedPacketDialogBgIv.setImageResource(R.drawable.chat_red_packet_open_bg_cant_open);
            binding.fragmentOpenRedPacketDialogDetailRl.setVisibility(View.VISIBLE);
            binding.fragmentOpenRedPacketDialogOpenRl.setVisibility(View.GONE);
        }
        if (type == 3) {
            //红包已退款，当前用户未领取

            binding.fragmentOpenRedPacketDialogGreetingTv.setText("手慢啦，红包已抢完");
            binding.fragmentOpenRedPacketDialogBgIv.setImageResource(R.drawable.chat_red_packet_open_bg_cant_open);
            binding.fragmentOpenRedPacketDialogDetailRl.setVisibility(View.VISIBLE);
            binding.fragmentOpenRedPacketDialogOpenRl.setVisibility(View.GONE);
        }
    }
    public static void showV(FragmentManager fragmentManager, String redPacketId,int type,String groupId,CustomMsgBean sendBean, IMMessage messageInfo, OpenRedPacketBlock block1) {
        FunOpenRedPacketFragment fragment = new  FunOpenRedPacketFragment();
        fragment.redPacketId = redPacketId;
        fragment.messageInfo = messageInfo;
        fragment.type = type;
        fragment.groupId = groupId;
        fragment.sendBean = sendBean;
        fragment.block = block1;
//        fragment.sendTipMsg();
//        if (type == 1) {
//            fragment. updateUI();
//        } else {
//            fragment._requestData();
//        }
        fragment.showNow(fragmentManager,"FunOpenRedPacketFragment");

    }


    @Override
    public void onClick(View v) {


        if (v == binding.fragmentOpenRedPacketDialogDetailRl) {
            gotoRedPacketDetail(false);

        } else if (v == binding.fragmentOpenRedPacketDialogOpenRl) {
            RegisterBean bean = new RegisterBean();
            bean.redpacketId = redPacketId;
            if (sendBean.type == 21) {
                HttpUtil.apiW().red_reciveExclusiveRedpacket(bean)
                        .enqueue(new CommonCallback<NetData>() {
                            @Override
                            public void Successful(Call<NetData> call, Response<NetData> response, NetData body) {
                                gotoRedPacketDetail(true);
//                                sendTipMsg(true);
                            }

                            @Override
                            public void Failure(Call<NetData> call, Throwable t) {

                            }
                        });
            } else if (sendBean.type == 22) {
                //type == 22 个人
                HttpUtil.apiW().red_recivePersonRedpacket(bean)
                        .enqueue(new CommonCallback<NetData>() {
                            @Override
                            public void Successful(Call<NetData> call, Response<NetData> response, NetData body) {
                                redResultBean = new Gson().fromJson(body.data.toString(), CustomMsgBean.class);
                                gotoRedPacketDetail(true);
                                sendTipMsg(false);
                            }

                            @Override
                            public void Failure(Call<NetData> call, Throwable t) {

                            }
                        });
            } else if (sendBean.type == 23) {
                //type == 23 群
                HttpUtil.apiW().red_grab(bean)
                        .enqueue(new CommonCallback<NetData>() {
                            @Override
                            public void Successful(Call<NetData> call, Response<NetData> response, NetData body) {
                                redResultBean = new Gson().fromJson(body.data.toString(), CustomMsgBean.class);
                                gotoRedPacketDetail(true);
                                sendTipMsg(true);
                            }

                            @Override
                            public void Failure(Call<NetData> call, Throwable t) {

                            }
                        });
            }
        }
    }
    void updateMessage() {
        HashMap map = new HashMap<>();
        map.put("userId",DataUtil.getUserid());
        map.put("hasDragDown",1);
        IMMessage message = messageInfo;
        message.setLocalExtension(map);
        NIMClient.getService(MsgService.class).updateIMMessage(message);
        if (block != null) {
            block.hasOpen(message);
        }
    }
    void sendTipMsg(boolean isGroup) {
        boolean isExit = false;
        if (redResultBean != null && !redResultBean.vos.isEmpty()) {
            for (CustomMsgBean bean :
                    redResultBean.vos) {
                if (bean.userId.equals(DataUtil.getUserid())) {
                    isExit = true;
                    break;
                }
            }
        }
        if (isExit) {

            IMMessage msg = MessageBuilder.createTipMessage(groupId, isGroup ? SessionTypeEnum.Team :SessionTypeEnum.P2P);
            CustomMsgBean msgBean = new CustomMsgBean();
            msgBean.receiveUserId = DataUtil.getUserid();
            msgBean.receiveUserName = DataUtil.getUserInfo().username;
            msgBean.sendUserId = sendBean.result.fromUserId;
            msgBean.sendUserName = sendBean.result.sendName;
            msg.setContent(new Gson().toJson(msgBean));
            CustomMessageConfig messageConfig = new CustomMessageConfig();
            messageConfig.enableUnreadCount = false;
            msg.setConfig(messageConfig);
            ChatRepo.sendMessage(msg, null);
            updateMessage();
        }
    }

    void gotoRedPacketDetail(boolean needToast) {
        if (needToast) {
            ToastUtils.toastMsg("领取成功");
        }
        HashMap map = new HashMap();
        map.put("redpacketId",redPacketId);
        Activity context = getActivity();
        if (context == null) {
            ToastUtils.toastMsg("请重试");
            dismiss();
            return;
        }
        FunRedPacketResultActivity.start(FunRedPacketResultActivity.class,context,map);
        dismiss();
    }
}
