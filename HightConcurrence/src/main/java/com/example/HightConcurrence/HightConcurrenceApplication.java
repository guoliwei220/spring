package com.example.HightConcurrence;

import lombok.extern.slf4j.Slf4j;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
@Slf4j
@MapperScan(basePackages = {"com.example.HightConcurrence.dao"})
@SpringBootApplication
public class HightConcurrenceApplication {

	public static void main(String[] args) {
		SpringApplication.run(HightConcurrenceApplication.class, args);
		log.info("项目启动完成");
	}

}
