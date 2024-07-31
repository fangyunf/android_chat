package com.turunsi.yaoxin.main.mine.purse.bankcard.bean;

public class BankCardListBean {
    public String id;
    public String userId;
    public String name;
    public String certNo;
    public String usdt;
    public String phone;
    public String type;

    public String getShowText() {
        return certNo +"("+ ((phone != null && phone.length() > 4) ? phone.substring(phone.length() - 4):phone)+")";
    }
}
