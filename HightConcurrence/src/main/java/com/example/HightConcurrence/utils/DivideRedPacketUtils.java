package com.example.HightConcurrence.utils;

import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
@Slf4j
public class DivideRedPacketUtils {
    /**
     * 初始化红包
     * @param totalAcount
     * @param totalPeopleNum
     * @return
     */
    public static List<Integer> divideRedPacket(Integer totalAcount, Integer totalPeopleNum){
        List<Integer> amountList = new ArrayList<>();
        Integer restAmount = totalAcount;
        Integer restPeopleNum = totalPeopleNum;
        Random random = new Random();
        for(int i = 0 ; i < totalPeopleNum-1 ; i ++){
            //二倍均值法
            int amount = random.nextInt(restAmount/restPeopleNum * 2 - 1) + 1;
            restAmount -= amount;
            restPeopleNum -- ;
            amountList.add(amount);
        }
        amountList.add(restAmount);
        log.info("红包信息："+amountList);
        return amountList;
    }

    public static void main(String[] args){
        List<Integer> amountList = divideRedPacket(1000, 50);
        BigDecimal total = new BigDecimal(0);
        int i = 1;
        for(Integer amount : amountList){
            BigDecimal bg = new BigDecimal(amount).divide(new BigDecimal(100));
            System.out.println(i+"抢到红包金额："+bg);
            total = total.add(bg);
            i = i + 1;
        }
        System.out.println("红包总金额："+total);
    }
}
