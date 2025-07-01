package com.foxishangxian.ebank.data;

public class GoldPrice {
    public String type;
    public String typename;
    public String price;
    public String openingprice;
    public String maxprice;
    public String minprice;
    public String changepercent;
    public String lastclosingprice;
    public String tradeamount;
    public String updatetime;

    public GoldPrice(String type, String typename, String price, String openingprice, String maxprice, String minprice, String changepercent, String lastclosingprice, String tradeamount, String updatetime) {
        this.type = type;
        this.typename = typename;
        this.price = price;
        this.openingprice = openingprice;
        this.maxprice = maxprice;
        this.minprice = minprice;
        this.changepercent = changepercent;
        this.lastclosingprice = lastclosingprice;
        this.tradeamount = tradeamount;
        this.updatetime = updatetime;
    }
} 