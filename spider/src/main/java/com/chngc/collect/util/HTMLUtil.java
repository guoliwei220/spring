package com.chngc.collect.util;


import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.htmlcleaner.HtmlCleaner;
import org.htmlcleaner.TagNode;
import org.htmlcleaner.XPatherException;
import org.apache.http.HttpEntity;
/**
 * 爬取页面信息
 */
public class HTMLUtil {

    // User-Agent: Mozilla/5.0 (Windows NT 6.2; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/76.0.3809.100 Safari/537.36
    private final static String user_agent = "Mozilla/5.0 (Windows NT 6.2; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/76.0.3809.100 Safari/537.36";
    // 获取 下载 html页面内容
    public static String downLoad(String url){
        String content = null;

        //HttpClientBuilder builder = HttpClients.custom();
        CloseableHttpClient client = HttpClients.createDefault();
        RequestConfig requestConfig = RequestConfig.custom()
                .setConnectTimeout(60000).setConnectionRequestTimeout(10000)
                .setSocketTimeout(60000).build();
        HttpGet request = new HttpGet(url);
        request.setConfig(requestConfig);
        try {
            request.setHeader("User-Agent", user_agent);// 模拟浏览器访问，防止被网站阻止
            CloseableHttpResponse response = client.execute(request);
            HttpEntity entity = response.getEntity();
            content = EntityUtils.toString(entity,"utf-8");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return content;
    }

    // 根据 xPath 获取页面对应数据
    public static String getDetailByXpath(String content, String xPath){
        HtmlCleaner htmlCleaner = new HtmlCleaner();
        TagNode rootNode = htmlCleaner.clean(content);

        try {
            Object[] evaluateXPath = rootNode.evaluateXPath(xPath);
            if(evaluateXPath.length > 0){
                TagNode tagNode = (TagNode) evaluateXPath[0];
                //System.out.println(tagNode.getText().toString());
                return tagNode.getText().toString();
            }
        } catch (XPatherException e) {
            e.printStackTrace();
        }

        return null;
    }
}
