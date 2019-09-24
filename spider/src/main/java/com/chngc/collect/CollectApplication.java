package com.chngc.collect;

import com.chngc.core.annotation.SqlMapper;
import lombok.extern.slf4j.Slf4j;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.Banner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication(scanBasePackages = {"com.chngc.collect"})
@MapperScan(basePackages = {"com.chngc.collect.dao"}, annotationClass = SqlMapper.class)
@EnableScheduling
@Slf4j
public class CollectApplication {
    /**
     * 启动入口
     * @param args
     * @throws InterruptedException
     */
    public static void main(String[] args) {
        SpringApplication app = new SpringApplication(CollectApplication.class);
        app.setBannerMode(Banner.Mode.OFF);
        app.run(args);
        log.info("项目启动完成!");
    }
}
