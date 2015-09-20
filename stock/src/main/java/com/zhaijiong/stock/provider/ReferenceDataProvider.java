package com.zhaijiong.stock.provider;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.gson.Gson;
import com.zhaijiong.stock.common.Utils;
import com.zhaijiong.stock.download.AjaxDownloader;
import com.zhaijiong.stock.download.BasicDownloader;
import com.zhaijiong.stock.download.Downloader;
import com.zhaijiong.stock.model.StockData;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * author: xuqi.xq
 * mail: xuqi.xq@alibaba-inc.com
 * date: 15-9-12.
 */
public class ReferenceDataProvider {

    /**
     * 分配预案数据源
     */
    private static String FPYA_URL = "http://quotes.money.163.com/data/caibao/fpyg.html?reportdate=%s&sort=declaredate&order=desc&page=%s";

    private static String MARGINTRADE_SH_URL = "http://data.eastmoney.com/rzrq/sh.html";

    private static String MARGINTRADE_SZ_URL = "http://data.eastmoney.com/rzrq/sz.html";

    //http://data.eastmoney.com/rzrq/total.html
    private static String MARGINTRADE_TOTAL_URL = "http://datainterface.eastmoney.com/EM_DataCenter/JS.aspx?type=FD&sty=SHSZHSSUM&st=0&sr=1&p=1&ps=10000&rt=48069399";

    /**
     * 盈利预期数据 http://data.eastmoney.com/report/ylyc.html
     * param1=版块，全部=_A
     */
    private static String EXPECT_EARNINGS_URL = "http://nufm.dfcfw.com/EM_Finance2014NumericApplication/JS.aspx?type=CT&cmd=C._A&sty=GEMCPF&st=(AllNum)&sr=-1&p=2&ps=5000&cb=&token=3a965a43f705cf1d9ad7e1a3e429d622&rt=48090871";

    private static String EXPECT_EARNINGS_PAGE_URL = "http://data.eastmoney.com/report/ylyc.html";

    /**
     * 获取盈利预期数据，全部版块=_A
     */
    public static List<StockData> getExpectEarnings(){
        List<StockData> stockDataList = Lists.newLinkedList();

        String data = Downloader.download(EXPECT_EARNINGS_URL);

        Gson gson = new Gson();
        List<String> records = gson.fromJson(data.substring(1, data.length() - 1), List.class);
        for(String record:records){
            String[] fields = record.split(",",21);
            double yanbaoCount = Double.parseDouble(fields[5]);
            if(yanbaoCount<=0){
                break;
            }
            StockData stockData = new StockData(fields[1]);
            stockData.name = fields[2];
            stockData.put("close",checkValueIsDouble(fields[3]));
            stockData.put("change",checkValueIsDouble(fields[4].replace("%","")));
            stockData.put("研报数",yanbaoCount);
            stockData.put("机构投资评级(近六个月)_买入",checkValueIsDouble(fields[6]));
            stockData.put("机构投资评级(近六个月)_增持",checkValueIsDouble(fields[7]));
            stockData.put("机构投资评级(近六个月)_中性",checkValueIsDouble(fields[8]));
            stockData.put("机构投资评级(近六个月)_减持",checkValueIsDouble(fields[9]));
            stockData.put("机构投资评级(近六个月)_卖出",checkValueIsDouble(fields[10]));
            stockData.put("2014实际_收益",checkValueIsDouble(fields[11]));
            stockData.put("2015预测_收益",checkValueIsDouble(fields[12]));
            stockData.put("2015预测_市盈率",checkValueIsDouble(fields[13]));
            stockData.put("2016预测_收益",checkValueIsDouble(fields[14]));
            stockData.put("2016预测_市盈率",checkValueIsDouble(fields[15]));
            stockData.put("2017预测_收益",checkValueIsDouble(fields[16]));
            stockData.put("2017预测_市盈率",checkValueIsDouble(fields[17]));
            stockDataList.add(stockData);
        }
        return stockDataList;
    }
    
    private static Double checkValueIsDouble(String field){
        if(Utils.isDouble(field)){
            return Double.parseDouble(field);
        }
        return Double.MIN_VALUE;
    }

    /**
     * 获取分配预案数据
     *
     * @param year 年份
     * @return
     */
    public static List<StockData> getFPYA(String year) {
        Integer pageCount = getPageCount(year);
        List<StockData> stockDataList = Lists.newLinkedList();
        for (int i = 0; i < pageCount; i++) {
            String url = String.format(FPYA_URL, year, i);
            stockDataList.addAll(collectFPYA(url));
        }
        return stockDataList;
    }

    private static Integer getPageCount(String year) {
        String url = String.format(FPYA_URL, year, 0);
        String data = BasicDownloader.download(url);
        Elements pageURLs = Jsoup.parse(data).select("div[class=mod_pages] a");
        Element lastPageURL = pageURLs.get(pageURLs.size() - 2);
        int pageCount = Integer.parseInt(lastPageURL.text());
        return pageCount;
    }

    public static List<StockData> collectFPYA(String url) {
        List<StockData> stockDataList = Lists.newLinkedList();

        String data = BasicDownloader.download(url);
        Elements table = Jsoup.parse(data).getElementById("plate_performance").getElementsByTag("tbody").get(0).getElementsByTag("tr");
        for(Element tr:table){
            Elements td = tr.getElementsByTag("td");
            StockData stockData = new StockData(td.get(1).text());
            stockData.name = td.get(2).text();
            stockData.date = Utils.str2Date(td.get(5).text(),"yyyy-MM-dd");

            stockData.put("dividend",getDividend(td.get(4).text().trim()));//分红
            stockData.put("shares",getShares(td.get(4).text().trim()));//转增和送股
            stockDataList.add(stockData);
        }
        return stockDataList;
    }

    /**
     * 获取分红数据
     * @param plan
     * @return
     */
    private static Double getDividend(String plan) {
        Pattern pattern = Pattern.compile("分红(.*?)元");
        Matcher matcher = pattern.matcher(plan);
        if(matcher.find()){
            String group = matcher.group(1);
            return Double.parseDouble(group);
        }
        return 0d;
    }

    /**
     * 获取转增和送股数
     * @return
     */
    private static Double getShares(String plan){
        Pattern pattern = Pattern.compile("[增|股](.*?)股");
        Matcher matcher = pattern.matcher(plan);
        Double count = 0d;
        while(matcher.find()){
            String group = matcher.group(1);
            count += Double.parseDouble(group);
        }
        return count;
    }

    public static List<StockData> getTotalMarginTrade(){
        List<StockData> stockDataList = Lists.newLinkedList();

        String data = BasicDownloader.download(MARGINTRADE_TOTAL_URL);
        System.out.println(data);
        Gson gson = new Gson();
        List<String> records = gson.fromJson(data.substring(1, data.length() - 1), List.class);

        for(String record:records){
            String[] fields = record.split(",");
            StockData stockData = new StockData();
            stockData.date = Utils.str2Date(fields[0].replaceAll("\"","").trim(),"yyyy-MM-dd");
            stockData.put("rzye_sh",getYI(fields[1]));
            stockData.put("rzye_sz",getYI(fields[2]));
            stockData.put("rzye_total",getYI(fields[3]));
            stockData.put("rzmre_sh",getYI(fields[4]));
            stockData.put("rzmre_sz",getYI(fields[5]));
            stockData.put("rzmre_total",getYI(fields[6]));
            stockData.put("rqylye_sh",getYI(fields[7]));
            stockData.put("rqylye_sz",getYI(fields[8]));
            stockData.put("rqylye_total",getYI(fields[9]));
            stockData.put("rzrqye_sh",getYI(fields[10]));
            stockData.put("rzrqye_sz",getYI(fields[11]));
            stockData.put("rzrqye_total",getYI(fields[12]));

            stockDataList.add(stockData);
        }
        return stockDataList;
    }

    public static Double getYI(String text){
        if(text.trim().equals("-")){
            return 0d;
        }
        return Double.parseDouble(text.trim());
    }

    /**
     * 获取两市融资融券数据
     * 市场融资融券交易总量＝本日融资余额＋本日融券余量金额
     * 本日融资余额＝前日融资余额＋本日融资买入额－本日融资偿还额；
     * 本日融资偿还额＝本日直接还款额＋本日卖券还款额＋本日融资强制平仓额＋本日融资正权益调整－本日融资负权益调整；
     * 本日融券余量=前日融券余量+本日融券卖出数量-本日融券偿还量；
     * 本日融券偿还量＝本日买券还券量＋本日直接还券量＋本日融券强制平仓量＋本日融券正权益调整－本日融券负权益调整－本日余券应划转量；
     * 融券单位：股（标的证券为股票）/份（标的证券为基金）/手（标的证券为债券）。
     * @return
     */
    public static List<StockData> getMarginTrade(){
        List<StockData> stockDataList = Lists.newArrayListWithCapacity(1000);
        stockDataList.addAll(getMarginTrade(MARGINTRADE_SH_URL));
        stockDataList.addAll(getMarginTrade(MARGINTRADE_SZ_URL));
        return stockDataList;
    }

    /**
     * 获取沪市融资融券数据
     * @return
     */
    public static List<StockData> getMarginTrade(String url){
        List<StockData> stockDataList = Lists.newLinkedList();

        String data = AjaxDownloader.download(url);
        Elements table = Jsoup.parse(data).getElementById("dt_1").getElementsByTag("tbody").get(0).getElementsByTag("tr");
        for(Element tr:table){
            Elements td = tr.getElementsByTag("td");
            StockData stockData = new StockData(td.get(0).text());
            stockData.name = td.get(1).text();
            //融资余额(元)
            stockData.put("rzye", getYuan(td.get(3).text()));
            //融券余额(元)
            stockData.put("rqye",getYuan(td.get(4).text()));
            //融资买入额(元)
            stockData.put("rzmre",getYuan(td.get(5).text()));
            //融资偿还额(元)
            stockData.put("rzche",getYuan(td.get(6).text()));
            //融资净买额(元)
            stockData.put("rzjme",getYuan(td.get(7).text()));
            //融券余量
            stockData.put("rqyl",getYuan(td.get(8).text()));
            //融券卖出量
            stockData.put("rqmcl",getYuan(td.get(9).text()));
            //融券偿还量
            stockData.put("rqchl",getYuan(td.get(10).text()));
            //融资融券余额(元)
            stockData.put("rzrqye",getYuan(td.get(11).text()));
            stockDataList.add(stockData);
        }
        return stockDataList;
    }

    private static double getYuan(String text) {
        if(text.trim().contains("万")){
            return Double.parseDouble(text.replaceAll("万","").trim()) * 10000;
        }else if(text.trim().contains("亿")){
            return Double.parseDouble(text.replaceAll("亿","").trim()) * 100000000;
        }else{
            return Double.parseDouble(text.trim());
        }
    }

    public static void main(String[] args) {
//        List<StockData> fpya = ReferenceDataProvider.getFPYA("2014");
//        fpya.forEach((StockData stockData) ->
//            System.out.println(stockData.toString() + "\t" +
//                    stockData.get("dividend") + "\t" +
//                    stockData.get("shares")
//            ));

//        List<StockData> margintrade = ReferenceDataProvider.getMarginTrade();
//        margintrade.forEach(stockData ->{
//            System.out.println(stockData.toString());
//            Utils.printMap(stockData);
//        });

        List<StockData> margintrade = ReferenceDataProvider.getTotalMarginTrade();
        margintrade.forEach(stockData ->{
            System.out.println(stockData.toString());
            Utils.printMap(stockData);
        });
    }
}
