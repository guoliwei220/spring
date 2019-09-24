package com.chngc.collect.util;

import com.chngc.collect.common.ZhaoonlineConfig;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
@Slf4j
public class ZhaoonlineUtil {

    // 根据url地址将网页转化为document对象
    public static Document getDocument(String url){
        // 直接获取DOM树，带着cookies去获取
        Document document = null;
        try {
            document = Jsoup.connect(url).timeout(60000).cookies(ZhaoonlineConfig.cookies).get();// 设置超时时间60s
        } catch (Exception e) {
            e.printStackTrace();
           log.info("【赵涌在线】{ " + url + " }加载页面失败");
        }
        //System.out.println(document.toString());
        return document;
    }

    // 中文数字转化为阿拉伯数字 如：十一枚=11枚
    @SuppressWarnings("unused")
    private static int chineseNumberToInt(String chineseNumber){
        int result = 0;

        boolean flagNo = true;// 非中文类型的数字
        // 如果是中文类型的转换数字
        int temp = 1;//存放一个单位的数字
        int count = 0;//判断是否有chArr
        char[] cnArr = new char[]{'一','二','三','四','五','六','七','八','九'};
        char[] chArr = new char[]{'十','百'};
        for (int i = 0; i < chineseNumber.length(); i++) {
            boolean b = true;//判断是否是chArr
            char c = chineseNumber.charAt(i);
            for (int j = 0; j < cnArr.length; j++) {//非单位，即数字
                if (c == cnArr[j]) {
                    if(0 != count){//添加下一个单位之前，先把上一个单位值添加到结果中
                        result += temp;
                        temp = 1;
                        count = 0;
                    }
                    // 下标+1，就是对应的值
                    temp = j + 1;
                    b = false;
                    flagNo = false;
                    break;
                }
            }
            if(b){//单位{'十','百'}
                for (int j = 0; j < chArr.length; j++) {
                    if (c == chArr[j]) {
                        switch (j) {
                            case 0:
                                temp *= 10;
                                break;
                            case 1:
                                temp *= 100;
                                break;
                            default:
                                break;
                        }
                        flagNo = false;
                        count++;
                    }
                }
            }
            if (i == chineseNumber.length() - 1) {//遍历到最后一个字符
                result += temp;
            }
        }
        if(flagNo) result = 0;
        // 可能会有 两枚 这种情况
        if (result == 0){
            String[] everyChars = chineseNumber.split("");
            for (String everyChar : everyChars) {
                switch(everyChar){
                    case "两":
                        result = 2;break;
                    default:
                        result = 0;break;
                }
            }
        }
        return result;
    }

    // 2017,2018,2019...
    public static String getCurrencyYear(String goodsName){
        String year = "";
        Pattern p1 = Pattern.compile(ZhaoonlineConfig.REGEX_TO_YEAR);
        Matcher matcher1 = p1.matcher(goodsName);
        if(matcher1.find()){
            String yearInterval = matcher1.group();
            year = getYearList(yearInterval);
        }else {
            Pattern p2 = Pattern.compile(ZhaoonlineConfig.REGEX_YEAR);
            Matcher matcher2 = p2.matcher(goodsName);
            while (matcher2.find()) {
                year = year + "," +matcher2.group().replace("年","");
            }
            if(year.length()>0){
                year = year.substring(1);
            }
        }
        return year;
    }

    public static String getYearList(String yearInterval){
        String year = "";
        String[] years = yearInterval.replace("年","").split("-");
        if(years.length == 2){
            int startYear = Integer.parseInt(years[0]);
            int endYear = Integer.parseInt(years[1]);
            for (int i = startYear; i <= endYear; i++) {
                year = year + "," + i;
            }
        }
        if(year.length()>0){
            year = year.substring(1);
        }
        return year;
    }

    /**
     * 根据商品名称获取 枚数 目前支持以下三种，不支持（各X枚）
     *
     * 1、X枚  统计
     * 2、共X枚  统计
     * 3、X枚，X枚  统计
     * @param goodsName
     * @return
     */
    public static Integer getAmountTotal(String goodsName){
        Integer amount;

        Pattern p = Pattern.compile(ZhaoonlineConfig.REGEX_TOTAL_AMOUNT);
        Matcher m = p.matcher(goodsName);
        if (m.find()){//情况一： 是否是 "共几枚" 这种格式
            String number = m.group();
            // 如果是数字类型的
            if(number.length()>3){
                number = number.substring(number.length()-3);
            }
            if(isInteger(number)){
                amount = Integer.parseInt(number);
            }else {
                // 不是数字类型的
                amount = ZhaoonlineUtil.chineseNumberToInt(number);
            }
        } else {// 情况二：（X枚）、 （X枚，X枚）
            Pattern p2 = Pattern.compile(ZhaoonlineConfig.REGEX_EVERY_AMOUNT);
            Matcher m2 = p2.matcher(goodsName);
            if(m2.find()){
                amount = 0;// 如果含有"各几枚"暂不统计
            } else {
                amount = ZhaoonlineUtil.getAmount(goodsName);
            }
        }
        //System.out.println("总数：" + amount);
        return amount;
    }

    public static boolean isInteger(String str) {
        Pattern pattern = Pattern.compile("^[-\\+]?[\\d]*$");
        return pattern.matcher(str).matches();
    }

    /**
     *  目前只获取 "枚" 前三位
     * @param goodsName
     * @return
     */
    public static Integer getAmount(String goodsName){
        Integer amount = 0;
        List<String> resList = new ArrayList<>();
        Pattern p = Pattern.compile(ZhaoonlineConfig.REGEX_AMOUNT);
        Matcher m = p.matcher(goodsName);

        while (m.find()){
            String eachRes = m.group();
            if(StringUtils.isNotEmpty(eachRes)) {
                resList.add(eachRes.substring(eachRes.length()-3));
            }
        }
        for (String eachAmount : resList) {
            amount = amount + ZhaoonlineUtil.chineseNumberToInt(eachAmount);
        }
        return amount;
    }
}
