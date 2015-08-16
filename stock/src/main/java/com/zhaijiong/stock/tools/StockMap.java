package com.zhaijiong.stock.tools;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.apache.hadoop.hbase.util.Sleeper;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * Created by eryk on 15-4-8.
 */
public class StockMap {
    private static final Logger LOG = LoggerFactory.getLogger(StockMap.class);

    private static String stockURL = "http://quote.eastmoney.com/stocklist.html";

    private static int RETRY_TIMES = 3;
    private static int SLEEP_INTERVAL_MS = 3000;

    public static Map<String, String> getMap() {
        int zxb = 0;
        int sh = 0;
        int sz = 0;
        int cyb = 0;
        int other = 0;
        //key:code,val:name
        Map<String,String> stockMap = Maps.newTreeMap();
        int retryTimes = 0;
        while (retryTimes < RETRY_TIMES){
            try {
                Document doc = Jsoup.connect(stockURL).get();
                Elements stocks = doc.select("div[id=quotesearch] li a");
                for(Element stock :stocks){
                    String url = stock.attr("href");
                    if(url.contains("sh600")){
                        ++sh;
                    }else if(url.contains("sz000")){
                        ++sz;
                    }else if(url.contains("sz002")){
                        ++zxb;
                    }else if(url.contains("sz300")){
                        ++cyb;
                    }else {
                        ++other;
                        continue;
                    }
                    String[] stockArr = stock.text().split("\\(");
                    //key:name,value:symbol
                    stockMap.put(stockArr[1].replaceAll("\\)",""),stockArr[0]);
                }
                LOG.info("600:"+sh+",000:"+sz+",002:"+zxb+",300:"+cyb+",other:"+other);
                LOG.info("total:"+(sh+sz+zxb+cyb));
                return stockMap;
            } catch (IOException e) {
                LOG.error("fail to get stock list",e);
                retryTimes++;
                try {
                    Thread.sleep(SLEEP_INTERVAL_MS);
                } catch (InterruptedException e1) {
                    LOG.error("fail to sleep "+ SLEEP_INTERVAL_MS + "ms");
                }
            }
        }
        return Maps.newLinkedHashMap();
    }

    public static List<String> getList(){
        return Lists.newArrayList(getMap().keySet());
    }

    public static void main(String[] args) throws IOException {
        Map<String,String> stockMap = StockMap.getMap();
        for(Map.Entry<String, String> stock:stockMap.entrySet()){
            System.out.println(stock.getKey()+":"+stock.getValue());
        }
        System.out.println("stock:"+stockMap.size());


    }

}
