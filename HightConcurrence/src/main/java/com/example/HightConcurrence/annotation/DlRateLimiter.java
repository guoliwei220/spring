package com.example.HightConcurrence.annotation;

import org.springframework.core.annotation.AliasFor;

import java.lang.annotation.*;

@Target(value = ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface DlRateLimiter {
    //往令牌桶中放入令牌速度
    double rate();
    //保存令牌的超时时间
    long timeout() default 0;

}
