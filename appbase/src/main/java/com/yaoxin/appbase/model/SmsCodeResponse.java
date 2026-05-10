package com.yaoxin.appbase.model;

/**
 * /customer/smsCode 成功时 data 解密后的 JSON。
 * type: 0 已直接发短信；1 需图形验证（image 为验证码图地址）；2 网易盾（客户端暂未处理）
 */
public class SmsCodeResponse {
    public int type;
    public String image;
}
