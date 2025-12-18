package com.turunsi.yaoxin.main.shop.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 商品模型
 */
public class ProductModel implements Serializable {
    public String productId;
    public String productName;
    public List<String> productCoverImages = new ArrayList<>();
    public String productDetailImage;
    public double monthlyRentPrice;
    public double depositPrice;
    public String productRecommendationTags;
    public List<String> productRentalDaysSpecs = new ArrayList<>();
    public boolean isCollected;
    public String limitedTimeActivityStartTime;
    public String limitedTimeActivityEndTime;
    public Date limitedTimeActivityStartDate;
    public Date limitedTimeActivityEndDate;

    /**
     * 活动状态枚举
     */
    public enum ActivityStatus {
        NotStarted,  // 未开始
        Ongoing,     // 进行中
        Ended        // 已结束
    }

    /**
     * 获取活动状态
     */
    public ActivityStatus getActivityStatus() {
        Date now = new Date();
        if (limitedTimeActivityStartDate != null && limitedTimeActivityEndDate != null) {
            if (now.before(limitedTimeActivityStartDate)) {
                return ActivityStatus.NotStarted;
            } else if (now.after(limitedTimeActivityEndDate)) {
                return ActivityStatus.Ended;
            } else {
                return ActivityStatus.Ongoing;
            }
        }
        return ActivityStatus.Ongoing;
    }

    /**
     * 获取状态描述
     */
    public String getStatusDescription() {
        switch (getActivityStatus()) {
            case NotStarted:
                return "未开始";
            case Ongoing:
                return "进行中";
            case Ended:
                return "已结束";
            default:
                return "进行中";
        }
    }
}

