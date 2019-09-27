package com.example.HightConcurrence.aspect;

import com.example.HightConcurrence.annotation.DlRateLimiter;
import com.google.common.util.concurrent.RateLimiter;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.concurrent.TimeUnit;

@Aspect
@Component
public class RateLimiterAspect {
    @Autowired
    private HttpServletResponse response;
    //创建一个令牌桶的限流器
    private RateLimiter rateLimiter = RateLimiter.create(700);
    @Pointcut("execution(public * com.example.HightConcurrence.web.*.*(..))")
    public void pointcut(){
    }
    @Around("pointcut()")
    public Object process(ProceedingJoinPoint proceedingJoinPoint) throws Throwable {
        //获取方法签名
        MethodSignature signature = (MethodSignature) proceedingJoinPoint.getSignature();
        //使用java反射获取方法上是否有DlRateLimiter注解
        DlRateLimiter declaredAnnotation = signature.getMethod().getDeclaredAnnotation(DlRateLimiter.class);
        if(declaredAnnotation == null){
            return proceedingJoinPoint.proceed();
        }
        //获取注解上的参数，获取配置速率
        double rate = declaredAnnotation.rate();
        //获取注解上的参数，获取配置超时时间
        long timeout = declaredAnnotation.timeout();
        //设置限流数率
        rateLimiter.setRate(rate);
        //判断令牌的超时时间
        boolean b = rateLimiter.tryAcquire(timeout, TimeUnit.MILLISECONDS);
        if(!b){
            fullback();
            return  null;
        }
        return proceedingJoinPoint.proceed();
    }
    //服务降级
    public void fullback(){
        response.setHeader("context-type","text/html;chartset = UTF-8");
        PrintWriter printWriter = null;
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
