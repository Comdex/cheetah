package com.zhaijiong.stock.strategy.buy;

import com.google.common.collect.Lists;
import com.zhaijiong.stock.common.StockConstants;
import com.zhaijiong.stock.common.Utils;
import com.zhaijiong.stock.model.StockData;
import com.zhaijiong.stock.provider.Provider;
import com.zhaijiong.stock.strategy.StrategyUtils;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * author: eryk
 * mail: xuqi86@gmail.com
 * date: 15-11-22.
 */
public class GoldenSpiderBuyStrategy implements BuyStrategy {

    private int crossCount = 3;

    public GoldenSpiderBuyStrategy(){}

    public GoldenSpiderBuyStrategy(int crossCount){
        this.crossCount = crossCount;
    }

    @Override
    public double buy(String symbol) {
        List<StockData> stockDataList = Provider.dailyData(symbol,true);
        return buy(stockDataList);
    }

    @Override
    public double buy(List<StockData> stockDataList) {
        stockDataList = StrategyUtils.goldenSpider(stockDataList);
        if(stockDataList.size()>1){
            double status = stockDataList.get(stockDataList.size() - 1).get(StockConstants.GOLDEN_SPIDER);
            if(status>=crossCount){
                System.out.println("buy:"+stockDataList.get(stockDataList.size() - 1));
                return stockDataList.get(stockDataList.size() - 1).get(StockConstants.CLOSE);
            }
        }
        return -1;
    }

    @Override
    public boolean isBuy(String symbol) {
        List<StockData> stockDataList = Provider.dailyData(symbol, true);
        return isBuy(stockDataList);
    }

    @Override
    public boolean isBuy(List<StockData> stockDataList) {
        int size = stockDataList.size();
        stockDataList = StrategyUtils.goldenSpider(stockDataList);
        if(size>2){
            double status = stockDataList.get(size - 1).get(StockConstants.GOLDEN_SPIDER);
            if(status>=crossCount){
                return true;
            }
        }
        return false;
    }

}
