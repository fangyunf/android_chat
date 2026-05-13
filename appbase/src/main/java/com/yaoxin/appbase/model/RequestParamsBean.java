package com.yaoxin.appbase.model;

import com.yaoxin.appbase.net.Constant;
import com.yaoxin.appbase.utils.AppProxy;
import com.yaoxin.appbase.utils.DeviceUtils;

public class RequestParamsBean {
    public String phone;
    public String id;
    public String payChannel;
    public String zfb;
    public String certNo;
    public String usdt;
    public String name;
    public String configId;
    public String payWay;
    public String userId;
    public int amount;
    public int summary;
    private String phone1;
    public String type;
    private String deviceId;
    private String clientType;
    public String bankUserName;
    public String bankCardNo;
    public String bankPhone;
    public String bankName;
    public String idNumber;
    public String payment_id;
    public String order_no;
    public String apply_id;
    public String app_id;
    public String memberId;
    public String smsCode;
    public String sms_code;
    public String goodsTitle;
    public String goodsDesc;
    public String description;
    /** USDT 充值：区块链交易哈希（若有后端字段以此为准，可多字段兼容时再扩展） */
    public String txHash;
    /** USDT 充值：凭证图 URL（上传后） */
    public String voucher;
    /** USDT 充值：凭证图 URL（后端字段名 zfpz） */
    public String zfpz;
    /** USDT 充值：充值金额（元）字符串，与 {@link #amount}（分）同笔订单一致，按后台需要可选 */
    public String rechargeMoney;
    /** USDT 充值：应付 USDT 数量（字符串，如 14.29），按后台需要可选 */
    public String usdtAmount;
    public String token_no;
    public String in_member_id;
    public String out_member_id;
    public RequestParamsBean(String phone1,String name1, String type1) {
        deviceId = DeviceUtils.getDeviceId(AppProxy.getInstance().getContext());
        clientType = Constant.clientType;
        phone = phone1;
        name = name1;
        type = type1;
    }

    public RequestParamsBean() {
        deviceId = DeviceUtils.getDeviceId(AppProxy.getInstance().getContext());
        clientType = Constant.clientType;
    }
}
