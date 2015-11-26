package com.zhaijiong.stock.strategy;

import com.google.common.base.Stopwatch;
import com.zhaijiong.stock.BackTestTrader;
import com.zhaijiong.stock.common.Conditions;
import com.zhaijiong.stock.common.Context;
import com.zhaijiong.stock.model.PeriodType;
import com.zhaijiong.stock.provider.Provider;
import com.zhaijiong.stock.strategy.buy.BuyStrategy;
import com.zhaijiong.stock.strategy.buy.GoldenSpiderBuyStrategy;
import com.zhaijiong.stock.strategy.buy.MACDBuyStrategy;
import com.zhaijiong.stock.strategy.sell.GoldenSpiderSellStrategy;
import com.zhaijiong.stock.strategy.sell.MACDSellStrategy;
import com.zhaijiong.stock.strategy.sell.MASellStrategy;
import com.zhaijiong.stock.strategy.sell.SellStrategy;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.List;
import java.util.concurrent.TimeUnit;

public class BackTestTraderTest {

    private BackTestTrader backTestTrader;
//    private BuyStrategy buyStrategy = new MACDBuyStrategy(1, PeriodType.DAY);
//    private SellStrategy sellStrategy = new MACDSellStrategy(1, PeriodType.DAY);
    private BuyStrategy buyStrategy = new GoldenSpiderBuyStrategy();
    private SellStrategy sellStrategy = new GoldenSpiderSellStrategy();

    @Before
    public void setUp() throws Exception {
        Context context = new Context();
        DefaultStrategy strategy = new DefaultStrategy(buyStrategy,sellStrategy);
        backTestTrader = new BackTestTrader(context,strategy);
    }

    @After
    public void tearDown() throws Exception {
        backTestTrader.cleanup();
    }

    @Test
    public void testTest() throws Exception {
        backTestTrader.test("002271");
        backTestTrader.print();
    }

    @Test
    public void testTestList() throws Exception {
        Stopwatch stopwatch = Stopwatch.createStarted();
        Conditions conditions = new Conditions();
        conditions.addCondition("close", Conditions.Operation.LT,20d);
        conditions.addCondition("PE",Conditions.Operation.LT,200d);
        conditions.addCondition("marketValue",Conditions.Operation.LT,100d);
        List<String> stockDatalist = Provider.tradingStockList(conditions);
        System.out.println("stockDataList:"+stockDatalist.size());
        backTestTrader.test(stockDatalist);
        System.out.println("cost:"+stopwatch.elapsed(TimeUnit.SECONDS)+"s");
    }
}