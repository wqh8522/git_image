package com.wqh.demo.drools.pojo;

/**
 * 产品类
 */

public class Product {

    public static final String DIAMOND = "DIAMOND"; // 钻石
    public static final String GOLD = "GOLD"; // 黄金


    /**
     * 商品类型
     */
    private String type;

    /**
     * 折扣
     */
    private int discount;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public int getDiscount() {
        return discount;
    }

    public void setDiscount(int discount) {
        this.discount = discount;
    }
}
