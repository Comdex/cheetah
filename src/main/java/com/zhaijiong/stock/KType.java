package com.zhaijiong.stock;

/**
 * author: xuqi.xq
 * mail: xuqi.xq@alibaba-inc.com
 * date: 15-8-9.
 */
public enum KType {
    FIVE_MIN("5"),
    FIFTEEN_MIN("15"),
    THIRTY_MIN("30"),
    SIXTY_MIN("60"),
    DAY("daily"),
    WEEK("week"),
    MONTH("month"),
    YEAR("year");

    private String type;

    private KType(String type){
        this.type = type;
    }

    public String getType() {
        return type;
    }
}
