package com.chngc.collect.service;

import com.chngc.collect.common.ZhaoonlineConfig;
import com.chngc.collect.dao.BusiCollectPriceSpiderMapper;
import com.chngc.collect.entity.BusiCollectPriceSpider;
import com.chngc.collect.entity.PageInfo;
import com.chngc.collect.util.DateUtils;
import com.chngc.collect.util.HTMLUtil;
import com.chngc.collect.util.ZhaoonlineUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

@Slf4j
@Service
public class SpiderZhaoonlineService {

    @Autowired
    private BusiCollectPriceSpiderMapper busiCollectPriceSpiderMapper;

    @Value("${zhaoonline.login.loginId}")
    private String loginId;
    @Value("${zhaoonline.login.password}")
    private String password;

    /**
     * 定时爬取任务
     */
    @Scheduled(cron = "00 30 02 * * ?")
    public void startSpiderZhaoonlineSch() {
        // 查询最新一条交易数据
        ZhaoonlineConfig.lastRecord = busiCollectPriceSpiderMapper.getLastCollect(ZhaoonlineConfig.sourceType);
        // 模拟登陆
        log.info("定时任务：——————————————————爬取【赵涌在线】任务开始————————————");
        getCookie();
        // 往队列里放入首页
        if(ZhaoonlineConfig.lastRecord == null){
            ZhaoonlineConfig.URL_QUEUE_FUll.add(ZhaoonlineConfig.FIRST_URL);// 全量执行
        } else {
            ZhaoonlineConfig.URL_QUEUE_INC.add(ZhaoonlineConfig.FIRST_URL);// 增量执行
        }
    }

    // 消费队列
    public void startConsumQueue(){
        try {
            while (true) {
                String currentUrlFull = ZhaoonlineConfig.URL_QUEUE_FUll.poll();// 全量：从队列里边拉取要爬的页面
                String currentUrlInc = ZhaoonlineConfig.URL_QUEUE_INC.poll();// 增量：从队列里边拉取要爬的页面
                String startPageUrl = ZhaoonlineConfig.URL_QUEUE_INTERFACE.poll();// 接口：手动设置起止页

                Thread.sleep(1000);
                // 全量爬取所有
                if(StringUtils.isNotBlank(currentUrlFull)){
                    try {
                        //  获取页数据
                        PageInfo pageInfo = getInfoFromList(currentUrlFull, ZhaoonlineConfig.FIVEHUND_URL);
                        //
                        List<String> urlList = pageInfo.getUrlList();
                        for (String eachUrl : urlList) {
                            // 需要爬取的地址 放入队列
                            ZhaoonlineConfig.URL_QUEUE_FUll.add(eachUrl);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                // 增量爬取
                if(StringUtils.isNotBlank(currentUrlInc)){
                    //  获取页数据
                    PageInfo pageInfo = getInfoFromList(currentUrlInc, "");
                    //
                    List<String> urlList = pageInfo.getUrlList();
                    for (String eachUrl : urlList) {
                        // 需要爬取的地址 放入队列
                        ZhaoonlineConfig.URL_QUEUE_INC.add(eachUrl);
                    }
                }

                // 接口根据起止页
                if(StringUtils.isNotBlank(startPageUrl) && StringUtils.isNotBlank(ZhaoonlineConfig.endPageUrl)){
                    //  获取页数据
                    PageInfo pageInfo = getInfoFromList(startPageUrl, ZhaoonlineConfig.endPageUrl);
                    //
                    List<String> urlList = pageInfo.getUrlList();
                    for (String eachUrl : urlList) {
                        // 需要爬取的地址 放入队列
                        ZhaoonlineConfig.URL_QUEUE_INTERFACE.add(eachUrl);
                    }
                }
            }
        }catch (Exception e){
            e.printStackTrace();
            log.error("赵涌爬虫监控异常", e);
        }
    }

    /**
     * 列表页
     * @param url
     * @return
     */
    public PageInfo getInfoFromList(String url, String endPageUrl) throws InterruptedException {
        PageInfo info = new PageInfo();
        Elements elements;
        if (url.startsWith(ZhaoonlineConfig.listUrlPre)){// 如果是列表地址
            try {
                Document document = ZhaoonlineUtil.getDocument(url);
                if(document == null){
                    log.info("失败重试");
                    document = ZhaoonlineUtil.getDocument(url);
                }
                log.info("#########【赵涌在线】将要处理的列表页面："  + url);

                // 获取详情页url
                elements = document.select(ZhaoonlineConfig.domEachGoods);
                for (Element element : elements) {
                    // 每个成交记录信息
                    String detailPageUrl = ZhaoonlineConfig.zhaoyongURL + element.select(ZhaoonlineConfig.domDetailPage).attr("href");// 详情页地址
                    String imageUrl = element.select(ZhaoonlineConfig.domImageUrl).attr("rel");//图片地址
                    if (imageUrl.indexOf("!") > 0){
                        imageUrl = imageUrl.substring(0,imageUrl.indexOf("!"));
                    }
                    // 循环解析该页 每个详情页
                    boolean b = this.getGoodsInfoFromPage(detailPageUrl, imageUrl, endPageUrl);
                    if (b){
                        log.info("———————————————————【赵涌在线】 已解析到历史数据，清空队列，本次抓取任务结束！————————————————");
                        ZhaoonlineConfig.URL_QUEUE_INC.clear();// 清空队列，不再爬取
                        return info;
                    }
                }
                // 下一页地址
                elements = document.select(ZhaoonlineConfig.domNextPage);
                String href = elements.attr("href");
                if(url.indexOf(href) < 0 && !url.equals(endPageUrl)) {// 不是尾页，并且不是设定的最后一页
                    //当前页面不是是末页 把下一页添加到队列里边
                    log.info("#########【赵涌在线】当前页处理完毕，下个列表页面是"  + href );
                    info.addUrlList(ZhaoonlineConfig.zhaoyongURL + href);
                } else {
                    if(StringUtils.isNotBlank(endPageUrl))
                        ZhaoonlineConfig.lock = false;// 手动执行时，结束释放锁
                    log.info("#############【赵涌在线】————————————————> 最后一页 爬取完成 <——————————————————————");
                }
            }catch (Exception e){
                log.info("【赵涌在线】当前 列表页 获取失败，===> " + url);
                if(StringUtils.isNotBlank(endPageUrl))
                    ZhaoonlineConfig.lock = false;//
            }
        }
        return info;
    }

    /**
     * 详情页
     * @param detailPageUrl
     * @param imageUrl
     * @return
     */
    public boolean getGoodsInfoFromPage(String detailPageUrl, String imageUrl, String endPageUrl) throws InterruptedException {
        Document document = ZhaoonlineUtil.getDocument(detailPageUrl);
        if(document == null){
            log.info("失败重试");
            document = ZhaoonlineUtil.getDocument(detailPageUrl);
        }
        BusiCollectPriceSpider info = new BusiCollectPriceSpider();
        Elements elements;
        try {
        // 数据来源
        info.setSourceType(ZhaoonlineConfig.sourceType);
        // 详情url
        info.setTradeUrl(detailPageUrl);
        // 成交单号
        elements = document.select(ZhaoonlineConfig.domDealNo);
        String dealNo = elements.text().replace("＃","");
        if (StringUtils.isBlank(dealNo)){
            return false;
        }
        info.setSourceId(dealNo);
        // 图片路径
        info.setGoodsImageUrl(imageUrl);
        // 商品名称
        String goodsName = document.select(ZhaoonlineConfig.domGoodsName).text();
        info.setGoodsName(goodsName);
        // 数量
        //info.setGoodsQuantity(ZhaoonlineUtil.getAmountTotal(goodsName) == 0 ? null:ZhaoonlineUtil.getAmountTotal(goodsName));
        // 年份
        //info.setCurrencyYear(ZhaoonlineUtil.getCurrencyYear(goodsName));
        // 商品 评级 + 描述
        String character = document.select(ZhaoonlineConfig.domCharacter).text();// 评级
        String description = document.select(ZhaoonlineConfig.domDescription).text();// 描述
        if (StringUtils.isNotBlank(description)){
            character = character + "," + description;
        }
        info.setGoodsDesc(character);
        // 成交时间
        elements = document.select(ZhaoonlineConfig.domDealDate);
        info.setDealEndDate(DateUtils.strCSTPaseDate(elements.text()));
        // 成交总价
        elements = document.select(ZhaoonlineConfig.domTotalPrice);
        info.setTotalPrice(new BigDecimal(elements.text()));
        // 成交价
        String pid = document.select(ZhaoonlineConfig.domGoodsId).text();
        Document documentRecord = ZhaoonlineUtil.getDocument(ZhaoonlineConfig.bidRecordUrl + pid);
        String dealPrice = HTMLUtil.getDetailByXpath(documentRecord.toString(), ZhaoonlineConfig.xPathDealPrice);// 通过页面的xPath获取数据
        info.setDealPrice(new BigDecimal(dealPrice));

        // 交易类型
        info.setTradeType(1);//交易类型(1:拍卖，2:销售)
        // 创建时间
        info.setCreatDate(new Date());

        log.info("【赵涌在线】保存记录====>>交易编号：" + info.getSourceId()  + " 商品名称：" + info.getGoodsName() + " 交易时间：" + info.getDealEndDate() + " 页面路径：" + info.getTradeUrl());
        // 判断该条记录是否已经插入过(定时任务会判断是否是历史数据；只验证是增量是的数据)
        if(ZhaoonlineConfig.lastRecord != null && StringUtils.isBlank(endPageUrl)){

            Date lastRecordTime = DateUtils.changeDate(ZhaoonlineConfig.lastRecord.getDealEndDate());// 最后一条记录 年月日
            if(lastRecordTime.getTime() > info.getDealEndDate().getTime()){// 如果时间是最后一条时间的前一天，则不再获取
                log.info("######上次抓取的交易时间：" + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(lastRecordTime));
                return true;
            }
        }
            // 插入数据库
            try {
                busiCollectPriceSpiderMapper.insert(info);
            }catch (Exception e){
                log.info("#############【赵涌在线】交易单号：[ " + info.getSourceId() + " ]插入失败！重复的交易单号!##########");
            }
        }catch (Exception e){
            //e.printStackTrace();
            log.info("#############【赵涌在线】解析详情页数据出现异常，详情页地址：" + detailPageUrl +"  #######################");
            // 出现异常时的详情页？？？？
            BusiCollectPriceSpider esceptionRecord = new BusiCollectPriceSpider();
            esceptionRecord.setGoodsDesc("该详情页面未成功抓取");
            esceptionRecord.setTradeUrl(detailPageUrl);
            esceptionRecord.setGoodsImageUrl(imageUrl);
            esceptionRecord.setCreatDate(new Date());
            esceptionRecord.setSourceType(2);
            busiCollectPriceSpiderMapper.insert(esceptionRecord);
            log.info("异常数据存入数据库");
        }finally {
            Thread.sleep(Long.parseLong("1000"));// 降低频率防止被封
        }
        return false;
    }

    /**
     * 模拟登录获取cookie和sessionid
     */
    public void getCookie() {
        Connection connect = Jsoup.connect(ZhaoonlineConfig.URL_LOGIN);
        // 伪造请求头
        connect.header("Accept", "application/json, text/javascript, */*; q=0.01").header("Accept-Encoding",
                "gzip, deflate");
        connect.header("Accept-Language", "zh-CN,zh;q=0.9").header("Connection", "keep-alive");
        connect.header("Content-Length", "143").header("Content-Type",
                "application/x-www-form-urlencoded; charset=UTF-8");
        connect.header("Host", ZhaoonlineConfig.HOST).header("Referer", ZhaoonlineConfig.REFERER);
        connect.header("User-Agent", ZhaoonlineConfig.USER_AGENT)
                .header("X-Requested-With", "XMLHttpRequest");

        // 携带登陆信息
        connect.data("loginId", loginId).data("password", password);

        //请求url获取响应信息
        Connection.Response res = null;// 执行请求
        try {
            res = connect.ignoreContentType(true).timeout(30000).method(Connection.Method.POST).execute();
            if(res == null){
                log.info("cookie获取失败，重新获取");
                res = connect.ignoreContentType(true).timeout(30000).method(Connection.Method.POST).execute();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        // 获取返回的cookie
        ZhaoonlineConfig.cookies = res.cookies();
        log.info("#######【赵涌在线】模拟登陆完成######");
    }
}
