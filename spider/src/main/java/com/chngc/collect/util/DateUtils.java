package com.chngc.collect.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * 日期工具类
 *
 */
public class DateUtils {
    private static SimpleDateFormat yyyyMMdd = new SimpleDateFormat("yyyyMMdd");
    private static SimpleDateFormat hhmmss = new SimpleDateFormat("hhmmss");
    private static SimpleDateFormat yyyyMMddhhmm = new SimpleDateFormat("yyyyMMddhhmm");
    private static SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
    private static SimpleDateFormat CST = new SimpleDateFormat("EEE MMM dd hh:mm:ss z yyyy", Locale.ENGLISH);// 中国标准时间格式


    public static String getCDate() {
        return yyyyMMdd.format(new Date());
    }

    public static String getCTime() {
        return hhmmss.format(new Date());
    }

    public static String getCMinute() {
        return yyyyMMddhhmm.format(new Date());
    }

    public static String getDateStr(Date date) {
        return yyyyMMdd.format(date);
    }

    public static String getDayOfMonth(Date date) {
        Calendar rightNow = Calendar.getInstance();
        rightNow.setTime(date);
        rightNow.add(Calendar.DAY_OF_MONTH, -1);
        Date dt1 = rightNow.getTime();
        String reStr = sdf.format(dt1);
        return reStr;
    }


    // 字符串转化为时间格式  "Sat Aug 17 16:59:00 GMT+08:00 2019";（多线程时，线程不安全）
    public static Date strCSTPaseDate(String strDate) {
        Date date = null;
        try {
            date = CST.parse(strDate);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return date;
    }

    public static Date changeDate(Date date) {
        String formatDate = yyyyMMdd.format(date);
        Calendar cal=Calendar.getInstance();
        try {
            Date newDate = yyyyMMdd.parse(formatDate);
            cal.setTime(newDate);
            cal.add(Calendar.DATE, -1); //减1天
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return cal.getTime();
    }
    public static Date getDate(String date){
        Date date1 = null;
        try {
             date1 =  sdf.parse(date);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return date1;
    }

    public static String getDateDay(Date date){
        return sdf.format(date);
    }

}
