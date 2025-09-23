package com.yaoxin.appbase.model;

import java.io.Serializable;
import java.util.List;

public class HuiYuanBean implements Serializable {

    public List<HuiYuanBean> list;
    public HuiYuanBean memberConfig;
    public List<Integer> memberCode;

    public int id;
    public String memberLevel;
    public String productName;
    public String price;
    public String groupNum;
    public String personNumGroup;
    public String dayRaffle;

    public int currentIndex = 0;
}
