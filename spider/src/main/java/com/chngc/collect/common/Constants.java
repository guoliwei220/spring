package com.chngc.collect.common;

import com.chngc.core.util.EncryptUtil;

public final class Constants {
    public static final String SUCCESS_CODE = "00";
    public static final String NOLOGIN_CODE = "99";

    public static final String yijinURL = "https://www.yjzx.com";// 易金在线
    // 列表页数据
    public static final String yijinListUrlPre = "https://www.yjzx.com/History/tradeList"; // 列表页前缀
    public static final String yijinDomNextPage = "div.pages li.next a";// 下一页
    public static final String yijinDomDetailPage = "div.prod-list-small-box p.title-word a";// 详情页url 和名称
    public static final String yijinDomGoodsSaleType = "div.prod-list-small-box em"; //商品销售类型
    public static final String yijinShouqing = "historyshouqing";//正常销售
    public static final String yijinChengjiao = "chengjiaoem";//拍卖
    public static final String yijinDeailTime = "div.prod-list-small-box div.text-list p";//列表页获取最后交易时间

    // 从列表页获取数据
    public static final String yijinDomGoodsBox = "div.prod-list-small-box";// 商品列表模块

    //拍卖详情页数据
    public static final String yijinDomGoodsName = "div.info h2";// 商品名称
    public static final String yijinDomGoodsDetail = "div.prodInfo div.commodity-details span";// 详情
    public static final String yijinDomTime = "div.auction-end-inner p";// 交易时间
    public static final String yijinDealPrice = "p.the-last-price em";// 交易价格
    public static final String yijinTotalPrice = "p.the-last-price b";// 成交合计价格
    public static final String yijinDomGoodsimage = "div.details-wrap div.control_show_hide img";//商品图片url

    //销售的详情页数据
    public static final String yijinDomGoodsNameSale = "div .xm";//商品名称
    public static final String yijinDomGoodsDetailSale = "div.beijing div.you p";//商品详情
    public static final String yijinDealPriceSale = "div.price em";//成交价格
    public static final String yijinTotalPriceSale = "div.beijing div.you p:last-child em";//成交合计价格
    public static final String yijinDomGoodsimageSale = "div.top a img";//商品图片url

}
