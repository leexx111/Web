package com.cms.electricityproject;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;


//@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class})
@SpringBootApplication
@EnableScheduling
@EnableAsync // 开启异步支持
/**
 * 系统启动入口，启用 Spring Boot、定时调度与异步任务。
 */
public class ElectricityProjectApplication {

    public static void main(String[] args) {
        SpringApplication.run(ElectricityProjectApplication.class, args);
    }

}
