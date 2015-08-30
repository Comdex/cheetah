package com.zhaijiong.stock.model;

import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * author: xuqi.xq
 * mail: xuqi.xq@alibaba-inc.com
 * date: 15-8-15.
 */
public class StockData extends LinkedHashMap<String,Double>{

    public String       symbol;     //代码

    public String       name;       //名称

    public Date         date;       //时间

    public BoardType    boardType;  //版块信息：主版，中小板，创业板

    public StockMarketType stockMarketType; //市场：深市，沪市

    public StockData(){}

    public StockData(String symbol){
        this.symbol = symbol;
        this.stockMarketType = StockMarketType.getType(symbol);
        this.boardType = BoardType.getType(symbol);
    }

    public StockData(Map<String,Double> map){
        this.putAll(map);
    }

    @Override
    public String toString() {
        return "StockData{" +
                "symbol='" + symbol + '\'' +
                ", name='" + name + '\'' +
                ", date=" + date +
                ", boardType=" + boardType +
                ", stockMarketType=" + stockMarketType +
                '}';
    }
}
