package com.chngc.collect.common;

import com.chngc.collect.entity.BusiCollectPriceSpider;

import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedDeque;

/**
 * 赵涌在线 相关配置
 */
public class ZhaoonlineConfig {

    // 登录与cookie部分
    public final static String USER_AGENT = "Mozilla/5.0 (Windows NT 6.2; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/76.0.3809.100 Safari/537.36";// 模拟浏览器访问
    public final static String URL_LOGIN = "http://home.zhaoonline.com/login/submit.shtml";// 登录提交路径
    public final static String HOST = "home.zhaoonline.com";// 赵涌在线网址
    public final static String REFERER = "http://home.zhaoonline.com/login.shtml?back=http%3A%2F%2Fwww.zhaoonline.com%2Ftrade%2Fzhongguodangdaijinyinbi%2F8-8-N-N-00-N-0-N-1-N-N-N-N-0-N-N-1.htm";//登陆后重定向

    public static Map<String, String> cookies;//登录cookie

    // 列表页第一页地址
    public final static String PAGE_URL = "http://www.zhaoonline.com/trade/zhongguodangdaijinyinbi/8-8-N-N-00-N-0-N-1-N-N-N-N-0-N-N-";
    public final static String FIRST_URL = "http://www.zhaoonline.com/trade/zhongguodangdaijinyinbi/8-8-N-N-00-N-0-N-1-N-N-N-N-0-N-N-1.htm";
    public final static String FIVEHUND_URL = "http://www.zhaoonline.com/trade/zhongguodangdaijinyinbi/8-8-N-N-00-N-0-N-1-N-N-N-N-0-N-N-500.htm";
    // 定义线程池  默认线程池 5 个
    public final static int THREAD_NUM = 5;

    //
    public static final String REGEX_YEAR = "(19\\d\\d|20\\d\\d|2100)年"; // 匹配年份
    public static final String REGEX_TO_YEAR ="(19\\d\\d|20\\d\\d|2100)(年-)(19\\d\\d|20\\d\\d|2100)年";//  匹配年份
    public static final String REGEX_TOTAL_AMOUNT = "(?<=共).*?(?=枚)"; // 匹配枚数  "\\共(.*?)\\枚"
    public static final String REGEX_EVERY_AMOUNT = "(?<=各).*?(?=枚)"; // 匹配枚数  "\\各(.*?)\\枚"
    public static final String REGEX_AMOUNT = ".*?(?=枚)";// 匹配枚数 x枚

    // 赵涌在线 抓取地址 队列
    public static Queue<String> URL_QUEUE_FUll = new ConcurrentLinkedDeque<String>();// 全量
    public static Queue<String> URL_QUEUE_INC = new ConcurrentLinkedDeque<String>();// 增量
    public static Queue<String> URL_QUEUE_INTERFACE = new ConcurrentLinkedDeque<String>();// 手动
    public static String endPageUrl;// 结束页
    public static boolean lock = false;// 手动爬取锁
    // 列表页 元素
    public final static String zhaoyongURL = "http://www.zhaoonline.com";// 赵涌在线
    public final static String listUrlPre = "http://www.zhaoonline.com/trade/zhongguodangdaijinyinbi"; // 列表页前缀
    public final static String domNextPage = "a#nextPage";// 下一页
    public final static String domEachGoods = "div.list-item";// 每个商品
    public final static String domDetailPage = "div.list-item a.item-title";// 详情页url
    public final static String domImageUrl="div.item-media";// 图片路径


    //二  详情页 元素
    public static String domGoodsId="div.Id";// 竞拍id
    public final static String domDealNo="span.No";// 成交编号
    public final static String domGoodsName="span.name";// 商品名称
    public final static String domCharacter="span#character";// 评级
    public final static String domDescription="div.description";// 描述
    public final static String domDealDate="p.time span:first-child";// 成交时间
    public final static String domTotalPrice="p#currentPrice";// 成交总价
    public final static String bidRecordUrl="http://www.zhaoonline.com/bid-record.shtml?id=";// 竞拍排行
    public final static String xPathDealPrice = "//*[@id=\"pop_pagination_form\"]/div[1]/div/table/tbody/tr[2]/td[2]";// 成交价
    public final static Integer sourceType = 2 ;// 来源 赵涌为2

    public static BusiCollectPriceSpider lastRecord;// 数据库最新成交的一条记录
}
