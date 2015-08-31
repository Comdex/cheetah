package com.zhaijiong.stock.download;

/**
 * author: xuqi.xq
 * mail: xuqi.xq@alibaba-inc.com
 * date: 15-8-15.
 */
public class Downloader {

    public static String download(String url){
        return BasicDownloader.download(url);
    }

    public static String downloadAjax(String url){
        return AjaxDownloader.download(url);
    }
}
