package com.zhaijiong.stock.strategy;

import com.google.common.collect.Lists;
import com.zhaijiong.stock.common.StockConstants;
import com.zhaijiong.stock.common.Utils;
import com.zhaijiong.stock.indicators.Indicators;
import com.zhaijiong.stock.indicators.TDXFunction;
import com.zhaijiong.stock.model.StockData;

import java.util.List;

import static com.zhaijiong.stock.common.StockConstants.*;

/**
 * author: eryk
 * mail: xuqi86@gmail.com
 * date: 15-11-17.
 */
public class StrategyUtils {

    private static TDXFunction function = new TDXFunction();

    /**
     * 判断最近n个时间周期内是否出现金叉,或者是当前的MACD值在（-0.1,0.1）之间并且dif，dea持续缩短
     *
     * @param period
     * @return 如果是返回true
     */
    public static boolean isMACDGoldenCrossIn(List<StockData> stockDataList, int period) {
        int count = stockDataList.size();
        for (int i = count - 1; i > 0; i--) {
            StockData stockData = stockDataList.get(i);
            Double cross = stockData.get(MACD_CROSS);
            if (cross != null && count - i <= period && cross == 1  && stockDataList.get(i).get(DIF) < 0.1){
                return true;
            }
        }
        return false;
    }

    /**
     * 判断收盘价两条线是否相交，快线从下上传慢线叫金叉，快慢线名称请参考StockConstants类
     *
     * @param stockDataList
     * @param maFast        例如：close_ma5
     * @param maSlow        例如：close_ma10
     * @param period
     * @return
     */
    public static boolean isGoldenCrossIn(List<StockData> stockDataList, String maFast, String maSlow, int period) {
        int size = stockDataList.size() - 1;
        double[] maFastArr = Utils.getArrayFrom(stockDataList, maFast);
        double[] maSlowArr = Utils.getArrayFrom(stockDataList, maSlow);
        for (int i = size; i > 0; i--) {
            if (size - i > period) {
                break;
            }
            if (maFastArr[i] > maSlowArr[i] && maFastArr[i-1] < maSlowArr[i-1]) {
                return true;
            }
        }
        return false;
    }

    public static double goldenCrossPrice(List<StockData> stockDataList, String maFast, String maSlow, int period){
        int size = stockDataList.size() - 1;
        double[] maFastArr = Utils.getArrayFrom(stockDataList, maFast);
        double[] maSlowArr = Utils.getArrayFrom(stockDataList, maSlow);
        for (int i = size; i > 0; i--) {
            if (size - i < period) {
                break;
            }
            if (maFastArr[i] > maSlowArr[i] && maFastArr[i-1] < maSlowArr[i-1]) {
                return stockDataList.get(i).get(StockConstants.CLOSE);
            }
        }
        return -1;
    }

    /**
     * 判断最近n个时间周期内是否出现死叉
     *
     * @param period
     * @return 如果是返回true
     */
    public static boolean isMACDDiedCrossIn(List<StockData> stockDataList, int period) {
        int count = stockDataList.size();
        for (int i = count - 1; i > 0; i--) {
            StockData stockData = stockDataList.get(i);
            Double cross = stockData.get(MACD_CROSS);
            if (cross != null && count - i <= period && cross == 0)
                return true;
        }
        return false;
    }

    /**
     * 计算有几条均线粘合,粘合数加入到AVERAGE_BOND参数中
     *
     * @return
     */
    public static List<StockData> averageBond(List<StockData> stockDataList, double threshold) {
        List<StockData> result = Lists.newLinkedList();
        for (int i = 0; i < stockDataList.size(); i++) {
            StockData stockData = stockDataList.get(i);
            double ma5 = stockData.get(CLOSE_MA5);
            double ma10 = stockData.get(CLOSE_MA10);
            double ma20 = stockData.get(CLOSE_MA20);
            double ma30 = stockData.get(CLOSE_MA30);
            double ma40 = stockData.get(CLOSE_MA40);
            double ma60 = stockData.get(CLOSE_MA60);
//            double ma120 = stockData.get(CLOSE_MA120);
            if (ma5 == 0 || ma10 == 0 || ma20 == 0 || ma30 == 0 || ma40 == 0 || ma60 == 0) {
                continue;
            }
            double maCount = 0;
            double[] maArr = {ma5, ma10, ma20, ma30, ma40, ma60};
            for (int j = 0; j < 5; j++) {
                for (int k = j + 1; k < 6; k++) {
                    if (Math.abs(maArr[j] / maArr[k] - 1) < threshold) {
                        maCount++;
                    }
                }
            }
            stockData.put(AVERAGE_BOND, maCount);
            result.add(stockData);
        }
        return result;
    }

    /**
     * MA2:=EMA(C,2);
     * MA5:EMA(C,5);
     * MA13:EMA(C,13);
     * MA34:EMA(C,34);
     * MA55:EMA(C,55);
     * YCX:=MA5>=REF(MA5,1);
     * H1:=MAX(MAX(MA5,MA13),MA34);
     * L1:=MIN(MIN(MA5,MA13),MA34);
     * 一阳穿三线:= H1<C AND O<L1 AND YCX AND MA2>REF(MA2,1);
     *
     * @param stockDataList
     * @return
     */
    public static List<StockData> goldenSpider(List<StockData> stockDataList) {
        Indicators indicators = new Indicators();
        List<StockData> result = Lists.newLinkedList();
        double[] closes = Utils.getArrayFrom(stockDataList, CLOSE);
        double[] ma2 = indicators.ema(closes, 2);
        double[] ma5 = indicators.ema(closes, 5);
        double[] ma13 = indicators.ema(closes, 13);
        double[] ma34 = indicators.ema(closes, 34);
        double[] ma55 = indicators.ema(closes, 55);
        for (int i = 0; i < stockDataList.size(); i++) {
            StockData stockData = stockDataList.get(i);
            if (i > 0 && ma5[i] > ma5[i - 1]) {
                double close = stockData.get(CLOSE);
                double open = stockData.get(OPEN);
                double max1 = function.max(ma5[i], ma13[i], ma34[i]);
                double min1 = function.min(ma5[i], ma13[i], ma34[i]);
                double max2 = function.max(ma5[i], ma13[i], ma34[i], ma55[i]);
                double min2 = function.min(ma5[i], ma13[i], ma34[i], ma55[i]);
                if (max1 < close && open < min1 && ma2[i] > ma2[i - 1]
//                        && stockData.get(VOLUME) > stockDataList.get(i-1).get(VOLUME)*1.2
                        ) {
                    stockData.put(GOLDEN_SPIDER, 3d);
                } else if (max2 < close && open < min2 && ma2[i] > ma2[i - 1]
//                        && stockData.get(VOLUME) > stockDataList.get(i-1).get(VOLUME)*1.2
                        ) {
                    stockData.put(GOLDEN_SPIDER, 4d);
                } else {
                    stockData.put(GOLDEN_SPIDER, 0d);
                }
            } else {
                stockData.put(GOLDEN_SPIDER, 0d);
            }
            result.add(stockData);
        }
        return result;
    }
}
