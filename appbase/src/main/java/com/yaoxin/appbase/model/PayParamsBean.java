package com.yaoxin.appbase.model;

import java.util.List;

public class PayParamsBean {

    public String member_id;
    public String created_time;
    public String error_msg;
    public String card_id;
    public String tel_no;
    public String error_type;
    public String prod_mode;
    public String error_code;
    public String id;
    public String app_id;
    public String mer_cust_id;
    public String object;
    public String status;
    public String tokenNo;
    public String orderCode;
    public String bankCardNo;
    public String bankName;
    public String order_no;


    public String getBankCardNo() {
        return bankCardNo != null && bankCardNo.length() >= 4
                ? bankCardNo.substring(bankCardNo.length() - 4)
                : "未知";
    }
}
