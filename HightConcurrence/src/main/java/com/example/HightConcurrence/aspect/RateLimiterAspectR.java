package com.example.HightConcurrence.aspect;

import antlr.StringUtils;
import com.example.HightConcurrence.annotation.DlRateLimiter;
import com.example.HightConcurrence.annotation.DlRateLimiterR;
import com.google.common.collect.Lists;
import com.google.common.util.concurrent.RateLimiter;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.scripting.support.ResourceScriptSource;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.concurrent.TimeUnit;
@Slf4j
@Aspect
@Component
public class RateLimiterAspectR {
    @Autowired
    private HttpServletResponse response;
    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    private DefaultRedisScript<Long> redisScript;
    @PostConstruct
    public void init(){
        redisScript = new DefaultRedisScript<>();
        redisScript.setResultType(Long.class);
        redisScript.setScriptSource(new ResourceScriptSource(new ClassPathResource("limiter.lua")));
    }
    //创建一个令牌桶的限流器
    //private RateLimiter rateLimiter = RateLimiter.create(700);
    @Pointcut("execution(public * com.example.HightConcurrence.web.*.*(..))")
    public void pointcut(){
    }
    @Around("pointcut()")
    public Object process(ProceedingJoinPoint proceedingJoinPoint) throws Throwable {
        //获取方法签名
        MethodSignature signature = (MethodSignature) proceedingJoinPoint.getSignature();
        //使用java反射获取方法上是否有DlRateLimiter注解
        DlRateLimiterR dlRateLimiterR = signature.getMethod().getDeclaredAnnotation(DlRateLimiterR.class);
        if(dlRateLimiterR == null){
            return proceedingJoinPoint.proceed();
        }
        //获取注解上的参数，获取配置速率
        double value = dlRateLimiterR.value();
        //list设置lua的key
        String key = "ip:"+System.currentTimeMillis();
        List<String> keyList = Lists.newArrayList(key);
        Long execute = stringRedisTemplate.execute(redisScript, keyList, String.valueOf(value));
        log.info("分布式限流执行脚本返回结果："+execute);
        //lua脚本返回0表示超出限流数量，返回1表示没有超过限流数
        if("0".equals(execute.toString())){
            fullback();
            return  null;
        }
        return proceedingJoinPoint.proceed();
    }
    //服务降级
    public void fullback(){
        response.setHeader("context-type","text/html;chartset = UTF-8");
        PrintWriter printWriter =null;
        try {
            printWriter = response.getWriter();
            printWriter.print("服务开小差，请稍后重试！");
            printWriter.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            if(null != printWriter){
                printWriter.close();
            }
        }

    }
}
