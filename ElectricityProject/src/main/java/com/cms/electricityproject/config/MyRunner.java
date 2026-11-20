package com.cms.electricityproject.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(1) // 可选，控制执行顺序
/**
 * 应用启动后的初始化任务入口。
 */
public class MyRunner implements CommandLineRunner {
    @Override
    public void run(String... args) throws Exception {
        // 调用工具类方法，例如：
    }
}