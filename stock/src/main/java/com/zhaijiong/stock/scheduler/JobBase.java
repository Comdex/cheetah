package com.zhaijiong.stock.scheduler;

import com.zhaijiong.stock.common.Context;
import com.zhaijiong.stock.dao.StockDB;
import org.quartz.Job;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static com.zhaijiong.stock.common.Constants.DATABASE_POOL_SIZE;

/**
 * author: xuqi.xq
 * mail: xuqi.xq@alibaba-inc.com
 * date: 15-8-12.
 */
public abstract class JobBase implements Job {
    protected static final Logger LOG = LoggerFactory.getLogger(JobBase.class);

    Context context = new Context();
    StockDB stockDB = new StockDB(context);
    ExecutorService executorService;

    public JobBase(){
        executorService = Executors.newFixedThreadPool(context.getInt(DATABASE_POOL_SIZE,1));
    }

    public List<String> getSymbolList(){
        return stockDB.getTradingStockSymbols();
    }

    public void close(){
        executorService.shutdown();
    }
}
