package com.example.HightConcurrence.annotation;

import org.springframework.core.annotation.AliasFor;

import java.lang.annotation.*;

@Target(value = ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface DlRateLimiterR {

    //往令牌桶中放入令牌速度
    @AliasFor("limiter")
    double value() default 5;
    //保存令牌的超时时间
    long timeout() default 5;
}
