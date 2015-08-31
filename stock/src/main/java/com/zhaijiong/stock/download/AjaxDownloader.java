package com.zhaijiong.stock.download;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.firefox.FirefoxDriver;

import java.io.InputStream;

/**
 * author: xuqi.xq
 * mail: xuqi.xq@alibaba-inc.com
 * date: 15-8-31.
 */
public class AjaxDownloader{

    public static String download(String url) {
        WebDriver driver = new FirefoxDriver();
        driver.get(url);
        String source = driver.getPageSource();
        driver.close();
        return source;
    }

}
