package com.example.HightConcurrence.web;

import com.example.HightConcurrence.annotation.DlRateLimiter;
import com.example.HightConcurrence.annotation.DlRateLimiterR;
import com.example.HightConcurrence.service.RedPacketService;
import com.google.common.util.concurrent.RateLimiter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.concurrent.TimeUnit;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Slf4j
@RestController
public class LimiterController {

    RateLimiter rateLimiter = RateLimiter.create(2);
    @Autowired
    private RedPacketService redPacketService;
    /**
     * 令牌算法底层实现
     */
//    public static void main(String[] args){
//        //一定的速度往桶里面放令牌
//        RateLimiter rateLimiter = RateLimiter.create(5);
//        //返回获取令牌的时间
//        log.info(String.valueOf(rateLimiter.acquire(1)));
//        log.info(String.valueOf(rateLimiter.acquire(1)));
//        log.info(String.valueOf(rateLimiter.acquire(1)));
//        log.info(String.valueOf(rateLimiter.acquire(1)));
//        log.info(String.valueOf(rateLimiter.acquire(1)));
//        log.info(String.valueOf(rateLimiter.acquire(1)));
//        log.info(String.valueOf(rateLimiter.acquire(1)));
//        log.info(String.valueOf(rateLimiter.acquire(1)));
//        log.info(String.valueOf(rateLimiter.acquire(1)));
//        log.info(String.valueOf(rateLimiter.acquire(1)));
//
//
//        //允许突发请求，当第一个请求获取的令牌过多时，那么第二个请求需要弥补上次的令牌
//        log.info(String.valueOf(rateLimiter.acquire(50)));
//        log.info(String.valueOf(rateLimiter.acquire(5)));
//        log.info(String.valueOf(rateLimiter.acquire(5)));
//        log.info(String.valueOf(rateLimiter.acquire(5)));
//        log.info(String.valueOf(rateLimiter.acquire(5)));
//    }

    /**
     * 使用令牌方法实现限流
     * 这样实现有缺点：会出现冗余代码，应该面向切面编程
     * @return
     */
    @RequestMapping("limiter")
    public String limiter(){
        boolean rateFlag = rateLimiter.tryAcquire(500, TimeUnit.MILLISECONDS);
        String message = "";
        if(!rateFlag){
            message = "服务器太挤了，没有挤进去，稍后重试!";
            log.info(message);
            return message;
        }
        int limiter = redPacketService.limiter();
        if(limiter > 0){
            message = "恭喜你，下单成功===========";
            return message;
        }else {
            message = "下单异常，请稍后重试！";
            return message;
        }
    }
    @DlRateLimiter(rate = 16, timeout = 500)
    @RequestMapping("limiter1")
    public String limiter1(){
        String message = "";
        int limiter = redPacketService.limiter();
        if(limiter > 0){
            message = "恭喜你，下单成功";
            return message;
        }else {
            message = "下单异常，请稍后重试！";
            return message;
        }
    }
    //上面的限流适合单机的限流，分布式限流才有redis+lua，下面采用分布式限流

    @DlRateLimiterR(value=2)
    @RequestMapping("limiter2")
    public String limiter2(){
        String message = "";
        int limiter = redPacketService.limiter();
        if(limiter > 0){
            message = "恭喜你，下单成功";
            return message;
        }else {
            message = "下单异常，请稍后重试！";
            return message;
        }
    }
}
