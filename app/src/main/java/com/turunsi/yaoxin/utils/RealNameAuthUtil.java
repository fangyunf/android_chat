package com.turunsi.yaoxin.utils;

import android.content.Context;
import android.util.Log;

import com.alipay.face.api.ZIMCallback;
import com.alipay.face.api.ZIMFacade;
import com.alipay.face.api.ZIMFacadeBuilder;
import com.alipay.face.api.ZIMResponse;
import com.netease.yunxin.kit.common.utils.SPUtils;
import com.yaoxin.appbase.model.NetData;
import com.yaoxin.appbase.net.CommonCallback;
import com.yaoxin.appbase.net.HttpUtil;

import retrofit2.Call;
import retrofit2.Response;

public class RealNameAuthUtil {
    public interface dispathBlockT {
        void finishBlock();
    }
    public static void start(Context ctx, String certifyId,dispathBlockT finishBlock) {

        ZIMFacade.install(ctx);
        ZIMFacade zimFacade = ZIMFacadeBuilder.create(ctx);
        zimFacade.verify(certifyId, true, null, new ZIMCallback() {
            @Override
            public boolean response(ZIMResponse response) {
                switch (response.code) {
                    case 1000:
                        Log.d("AliyunFace", "认证成功");
                        HttpUtil.apiW().consumer_certified()
                                .enqueue(new CommonCallback<NetData>() {
                                    @Override
                                    public void Successful(Call<NetData> call, Response<NetData> response, NetData body) {
                                        if (finishBlock != null) {
                                            finishBlock.finishBlock();
                                        }
//                                        SPUtils.getInstance().put("isRegister",false);
                                    }

                                    @Override
                                    public void Failure(Call<NetData> call, Throwable t) {

                                    }
                                });
                        break;
                    case 1001:
                        Log.e("AliyunFace", "系统错误");
                        break;
                    case 1003:
                        Log.e("AliyunFace", "验证中断");
                        break;
                    case 2002:
                        Log.e("AliyunFace", "网络错误");
                        break;
                    case 2003:
                        Log.e("AliyunFace", "客户端设备时间错误");
                        break;
                    case 2006:
                        Log.e("AliyunFace", "刷脸失败");
                        break;
                    default:
                        Log.e("AliyunFace", "未知错误");
                        break;
                }
                return true;
            }
        });

    }
}
