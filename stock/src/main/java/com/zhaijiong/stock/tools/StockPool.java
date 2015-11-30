package com.zhaijiong.stock.tools;

import com.google.common.collect.Lists;
import com.zhaijiong.stock.common.Conditions;
import com.zhaijiong.stock.common.Utils;
import com.zhaijiong.stock.model.StockBlock;
import com.zhaijiong.stock.provider.Provider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * author: eryk
 * mail: xuqi86@gmail.com
 * date: 15-11-21.
 */
public class StockPool {

    private static final String REDIS_KEY_PREFIX = "stocklist:";

    @Autowired
    public RedisTemplate<String, String> redisTemplate;

    public StockPool() {
    }

    public String getKeyWithPrefix(String key) {
        return REDIS_KEY_PREFIX + key;
    }

    /**
     * stockpool添加list到redis存储
     *
     * @param name
     * @param stockList
     * @param timeout   单位：秒
     */
    public void add(String name, List<String> stockList, long timeout) {
        redisTemplate.delete(getKeyWithPrefix(name));
        String[] valueList = Utils.toArray(stockList);
        redisTemplate.opsForList().rightPushAll(getKeyWithPrefix(name), valueList);
        redisTemplate.expire(getKeyWithPrefix(name), timeout, TimeUnit.SECONDS);
    }

    public List<String> get(String key) {
        return redisTemplate.opsForList().range(getKeyWithPrefix(key), 0, 5000);
    }

    /**
     * 获取交易中的股票列表
     * @return
     */
    public List<String> tradingStock(){
        String key = "trade";
        List<String> stockList = redisTemplate.opsForList().range(REDIS_KEY_PREFIX + key, 0, 4000);
        if(stockList==null || stockList.size()==0){
            stockList = Provider.tradingStockList();
            add(key,stockList,86400);
        }
        return stockList;
    }

    /**
     * 获取股票列表中交易中的股票列表
     *
     * @param stockList
     * @return
     */
    public List<String> tradingStock(List<String> stockList) {
        List<String> tradingStockList = Lists.newArrayList(tradingStock());
        stockList.retainAll(tradingStockList);
        return stockList;
    }

    /**
     * 获取融资融券股票列表
     * @return
     */
    public List<String> marginTradingStock() {
        String key = "margin";
        List<String> stockList = redisTemplate.opsForList().range(REDIS_KEY_PREFIX + key, 0, 4000);
        if(stockList==null){
            stockList = Provider.marginTradingStockList();
            add(key,stockList,86400);
        }
        return stockList;
    }

    /**
     * 全部股票列表
     * @return
     */
    public List<String> stockList(){
        return getList("all",86400);
    }

    /**
     * 获取某个大分类下的版块名称
     *
     * @param category 概念，行业，地域
     * @param name
     * @return
     */
    public List<String> listByCategory(String category, String name) {
        List<StockBlock> stockBlocks = StockCategory.getCategory().get(category);
        for (StockBlock stockBlock : stockBlocks) {
            if (stockBlock.name.equals(name)) {
                return tradingStock(stockBlock.symbolList);
            }
        }
        return Lists.newArrayList();
    }

    public List<String> listByConditions(Conditions conditions) {
        List<String> tradingStockList = tradingStock();
        return Provider.getStockListWithConditions(tradingStockList, conditions);
    }

    private List<String> getList(String key,long timeout) {
        List<String> stockList = redisTemplate.opsForList().range(REDIS_KEY_PREFIX + key, 0, 4000);
        if(stockList==null || stockList.size()==0){
            stockList = Provider.marginTradingStockList();
            String[] array = Utils.toArray(stockList);
            redisTemplate.opsForList().rightPushAll(REDIS_KEY_PREFIX + key,array);
            redisTemplate.expire(getKeyWithPrefix(key), timeout, TimeUnit.SECONDS);
        }
        return stockList;
    }

}
