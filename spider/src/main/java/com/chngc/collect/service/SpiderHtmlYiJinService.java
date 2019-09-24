package com.chngc.collect.service;

import com.chngc.collect.common.Constants;
import com.chngc.collect.dao.BusiCollectPriceSpiderMapper;
import com.chngc.collect.entity.BusiCollectPriceSpider;
import com.chngc.collect.entity.PageInfo;
import com.chngc.collect.util.DateUtils;
import com.chngc.collect.util.HTMLUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedDeque;

import org.jsoup.nodes.Element;

import javax.annotation.Resource;


@Slf4j
@Service
public class SpiderHtmlYiJinService{
    @Resource
    private BusiCollectPriceSpiderMapper busiCollectPriceSpiderMapper;
    // 队列
    private static Queue<String> urlQueue = new ConcurrentLinkedDeque<String>();
    private static Queue<String> urlQueueAuto = new ConcurrentLinkedDeque<String>();
    private static BusiCollectPriceSpider lastCollectTime = null;
    private static String endPageUrl = "";
    private static Boolean lockFlag = false;

    /**
     * 定时爬取易金在线数据
     */
    @Scheduled(cron = "0 12 03 * * ? ")
    public void spiderHtmlYiJinAuto() {
        String urltime = "https://www.yjzx.com/History/tradeList/p/1.html";
        lastCollectTime = busiCollectPriceSpiderMapper.getLastCollect(1);
        //加入队列
        urlQueueAuto.add(urltime);
    }

    /**
     * 手动执行爬取易金在线数据
     * @param startPageNo
     * @param endPageNo
     * @return
     */
    public Boolean spiderHtmlYiJin(int startPageNo,int endPageNo) {
        if(lockFlag){
            return true;
        }
        String spiderurl = "https://www.yjzx.com/History/tradeList/p/" + startPageNo + ".html";
        endPageUrl = "https://www.yjzx.com/History/tradeList/p/" + endPageNo + ".html";
        //加入队列
        urlQueue.add(spiderurl);
        return lockFlag;
    }
    public void startConsumQueue() {
        try {
            while (true) {
                // 从队列里边拉取要爬的页面
                String url = urlQueue.poll();
                String urlAuto = urlQueueAuto.poll();
                Thread.sleep(1000);
                //手动执行
                if (StringUtils.isNotBlank(url)) {
                    lockFlag = true;
                    log.info("++++++++++++++++++++++++++++【易金在线】手动爬取开始++++++++++++++++++++++++");
                    log.info("【易金在线】手动解析url:"+url);
                    //  获取列表页数据
                    PageInfo pageInfo = getListInfoDetail(url, endPageUrl);
                    List<String> urlList = pageInfo.getUrlList();
                    for (String eachUrl : urlList) {
                        urlQueue.add(eachUrl);
                    }

                }
                //降低频率
                Thread.sleep(Long.parseLong("1000"));
                if (StringUtils.isNotBlank(urlAuto)) {
                    log.info("++++++++++++++++++++++++++++【易金在线】自动爬取开始++++++++++++++++++++++++++++");
                    log.info("【易金在线】自动解析url:"+urlAuto);
                    //  获取列表页数据
                    PageInfo pageInfo = getListInfoDetail(urlAuto, "");
                    List<String> urlList = pageInfo.getUrlList();
                    for (String eachUrl : urlList) {
                        urlQueueAuto.add(eachUrl);
                    }
                }

            }
        }catch (Exception e){
            e.printStackTrace();
            log.info("++++++++++++++++++++++++++++【易金在线】爬取数据异常++++++++++++++++++++++++++++");
        }
    }
    /**
     * 获取列表页信息
     *
     * @param
     * @return
     */
    public PageInfo getListInfoDetail(String url, String endPageUrl) {
        PageInfo info = new PageInfo();
        Elements elements;
        List<Map<String, String>> goodsList = new ArrayList<>();
        if (url.startsWith(Constants.yijinListUrlPre)) {
            try {
				String content = HTMLUtil.downLoad(url);// 下载html
				if(StringUtils.isBlank(content)){
					log.info("============【易金在线】获取列表失败重试");
					content = HTMLUtil.downLoad(url);
				}
				Document document = Jsoup.parse(content);
                // 获取详情页url
                elements = document.select(Constants.yijinDomGoodsBox);
                Map<String, String> goodsMap = null;
                for (Element element : elements) {
                    goodsMap = new HashMap<>();
                    // 详情页地址
                    String goodsDtailUrl = Constants.yijinURL + element.select(Constants.yijinDomDetailPage).attr("href");
                    log.info("【易金在线】列表页" + url + "解析详情页url:" + goodsDtailUrl);
                    //详情页页面内容
                    String goodsDeailcontent = HTMLUtil.downLoad(goodsDtailUrl);
					if(StringUtils.isBlank(goodsDeailcontent)){
                        log.info("============【易金在线】获取详情页失败重试");
                        goodsDeailcontent = HTMLUtil.downLoad(goodsDtailUrl);
                    }
                    //交易类型
                    String saleType = element.select(Constants.yijinDomGoodsSaleType).attr("class");
                    goodsMap.put("goodsDtailUrl", goodsDtailUrl);
                    goodsMap.put("saleType", saleType);
                    goodsList.add(goodsMap);
                    BusiCollectPriceSpider detailInfoFromPage = null;
                    if (Constants.yijinChengjiao.equals(saleType)) {
                        //获取拍卖详情页数据
                        detailInfoFromPage = getDetailInfoFromPageAuc(goodsDeailcontent, goodsMap, endPageUrl);
                    } else if (Constants.yijinShouqing.equals(saleType)) {
                        //获取售罄详情页数据
                        detailInfoFromPage = getDetailInfoFromPageSale(goodsDeailcontent, goodsMap, endPageUrl);
                    }
                    if (detailInfoFromPage != null && detailInfoFromPage.getFlag() != null && detailInfoFromPage.getFlag()) {
                        log.info("++++++++++++++++++++++++++++【易金在线】自动爬取最后一页数据完成，最后一页url:"+url);
                        return info;
                    }
                }
                // 下一页地址
                elements = document.select(Constants.yijinDomNextPage);
                String href = elements.attr("href");
                if (url.indexOf(href) < 0 && !url.equals(endPageUrl)) {
                    log.info("【易金在线】下一页地址：" + Constants.yijinURL + href);
                    info.addUrlList(Constants.yijinURL + href);
                } else {
                    if (StringUtils.isNotBlank(endPageUrl)) {
                        //手动执行结束释放锁
                        lockFlag = false;
                    }
                    log.info("++++++++++++++++++++++++++++【易金在线】爬取最后一页数据完成，最后一页url:"+url);

                    return info;
                }
            } catch (Exception e) {
                e.printStackTrace();
                log.info("【易金在线】获取列表页异常，列表页url:"+url);
            }
        }
        return info;
    }


    /**
     * 获取售罄页面的信息
     *
     * @param content
     * @param goodsMap
     * @return
     */
    public BusiCollectPriceSpider getDetailInfoFromPageSale(String content, Map goodsMap, String endPageUrl) throws InterruptedException {
        Document document = Jsoup.parse(content);
        BusiCollectPriceSpider busiCollectPriceSpider = new BusiCollectPriceSpider();
        Elements elements;
        Element element;
        try {
            // 商品名称
            elements = document.select(Constants.yijinDomGoodsNameSale);
            if (elements.toString().length() == 0) {
                busiCollectPriceSpider.setGoodsDesc("【易金在线】未查询到售罄交易详情页url:" + goodsMap.get("goodsDtailUrl"));
                busiCollectPriceSpider.setTradeUrl(goodsMap.get("goodsDtailUrl").toString());
                log.info("【易金在线】" + busiCollectPriceSpider.getGoodsDesc());
                return busiCollectPriceSpider;
            }
            busiCollectPriceSpider.setGoodsName(elements.text());
            // 商品描述
            element = document.select(Constants.yijinDomGoodsDetailSale).get(1);
            busiCollectPriceSpider.setGoodsDesc(element.text());
            // 成交时间
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String element1 = document.select(Constants.yijinDomGoodsDetailSale).get(2).text();
            if(StringUtils.isBlank(endPageUrl) && lastCollectTime != null){
                Boolean endTimeB = getEndTime(lastCollectTime.getDealEndDate(), sdf.parse(element1));
                if(endTimeB){
                    busiCollectPriceSpider.setFlag(true);
                    return busiCollectPriceSpider;
                }
            }
            busiCollectPriceSpider.setDealEndDate(sdf.parse(element1));
            //创建时间
            busiCollectPriceSpider.setCreatDate(new Date());
            // 成交价格
            elements = document.select(Constants.yijinDealPriceSale);
            busiCollectPriceSpider.setDealPrice(new BigDecimal(elements.text()));
            //合计价格
            element = document.select(Constants.yijinTotalPriceSale).get(0);
            if(element.text().indexOf("元") > 0){
                busiCollectPriceSpider.setTotalPrice(new BigDecimal(element.text().substring(0,element.text().length()-1)));
            }else {
                busiCollectPriceSpider.setTotalPrice(new BigDecimal(element.text()));
            }
            //来源单号
            String dealNo = goodsMap.get("goodsDtailUrl").toString().split("/")[4];
            busiCollectPriceSpider.setSourceId(dealNo.substring(0, dealNo.length() - 5));
            //来源数据
            busiCollectPriceSpider.setSourceType(1);
            //交易类型
            String saleType = goodsMap.get("saleType").toString();
            if (Constants.yijinChengjiao.equals(saleType)) {
                busiCollectPriceSpider.setTradeType(1);
            } else if (Constants.yijinShouqing.equals(saleType)) {
                busiCollectPriceSpider.setTradeType(2);
            }
            //商品图片url
            element = document.select(Constants.yijinDomGoodsimageSale).get(0);
            busiCollectPriceSpider.setGoodsImageUrl(element.attr("src"));
            //交易详情
            busiCollectPriceSpider.setTradeUrl(goodsMap.get("goodsDtailUrl").toString());
            busiCollectPriceSpiderMapper.insert(busiCollectPriceSpider);
        } catch (Exception e) {
            e.printStackTrace();
            log.info("++++++++++++++++++++++++++++【易金在线】售罄详情页信息插入数据库异常，重复交易号："+busiCollectPriceSpider.getSourceId()+"++++++++++++++++++++++++++++");
        }finally {
            Thread.sleep(Long.parseLong("1000"));
        }
        return busiCollectPriceSpider;
    }
    /**
     * 拍卖详情页获取
     *
     * @param content
     * @return
     */
    public BusiCollectPriceSpider getDetailInfoFromPageAuc(String content, Map goodsMap, String endPageUrl) throws InterruptedException {
        Document document = Jsoup.parse(content);
        BusiCollectPriceSpider busiCollectPriceSpider = new BusiCollectPriceSpider();
        Elements elements;
        Element element;
        try {
            // 商品名称
            elements = document.select(Constants.yijinDomGoodsName);
            if (elements.toString().length() == 0) {
                busiCollectPriceSpider.setGoodsDesc("【易金在线】未查询到拍卖交易详情页url:" + goodsMap.get("goodsDtailUrl"));
                busiCollectPriceSpider.setTradeUrl(goodsMap.get("goodsDtailUrl").toString());
                log.info("【易金在线】" + busiCollectPriceSpider.getGoodsDesc());
                return busiCollectPriceSpider;
            }
            busiCollectPriceSpider.setGoodsName(elements.text());
            // 商品描述
            elements = document.select(Constants.yijinDomGoodsDetail);
            busiCollectPriceSpider.setGoodsDesc(elements.text());
            // 成交时间
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            element = document.select(Constants.yijinDomTime).get(1);
            Date endTime = sdf.parse(element.text().substring(5, element.text().length()));
            if(StringUtils.isBlank(endPageUrl) && lastCollectTime != null){
                Boolean endTimeB = getEndTime(lastCollectTime.getDealEndDate(), endTime);
                if(endTimeB){
                    busiCollectPriceSpider.setFlag(true);
                    return busiCollectPriceSpider;
                }
            }
            busiCollectPriceSpider.setDealEndDate(endTime);
            //创建时间
            busiCollectPriceSpider.setCreatDate(new Date());
            // 成交价格
            element = document.select(Constants.yijinDealPrice).get(0);
            busiCollectPriceSpider.setDealPrice(new BigDecimal(element.text()));
            //合计价格
            element = document.select(Constants.yijinTotalPrice).get(0);
            busiCollectPriceSpider.setTotalPrice(new BigDecimal(element.text()));
            //来源单号
            String dealNo = goodsMap.get("goodsDtailUrl").toString().split("/")[4];
            busiCollectPriceSpider.setSourceId(dealNo.substring(0, dealNo.length() - 5));
            //来源数据
            busiCollectPriceSpider.setSourceType(1);
            //交易类型
            String saleType = goodsMap.get("saleType").toString();
            if (Constants.yijinChengjiao.equals(saleType)) {
                busiCollectPriceSpider.setTradeType(1);
            } else if (Constants.yijinShouqing.equals(saleType)) {
                busiCollectPriceSpider.setTradeType(2);
            }
            //交易详情
            busiCollectPriceSpider.setTradeUrl(goodsMap.get("goodsDtailUrl").toString());
            //商品图片url
            element = document.select(Constants.yijinDomGoodsimage).get(0);
            busiCollectPriceSpider.setGoodsImageUrl(element.attr("src"));
            //插入数据
            busiCollectPriceSpiderMapper.insert(busiCollectPriceSpider);
        } catch (Exception e) {
            e.printStackTrace();
            log.info("++++++++++++++++++++++++++++【易金在线】拍卖详情页信息插入数据库异常，重复交易号："+busiCollectPriceSpider.getSourceId()+"++++++++++++++++++++++++++++");
        }finally {
            Thread.sleep(Long.parseLong("1000"));//降低频率
        }
        return busiCollectPriceSpider;
    }

    public Boolean getEndTime(Date lastCollectTime, Date dealEndTime){
        String dayOfMonth = DateUtils.getDayOfMonth(lastCollectTime);
        String dateDay = DateUtils.getDateDay(dealEndTime);
        if(DateUtils.getDate(dateDay).getTime() < DateUtils.getDate(dayOfMonth).getTime()){
            return true;
        }else {
            return false;
        }
    }

}
