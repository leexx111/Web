package com.cms.electricityproject.websoket;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

// WebSocket 配置类
@Configuration
@EnableWebSocket
/**
 * 注册系统 WebSocket 端点及消息处理器。
 */
public class WebSocketConfig implements WebSocketConfigurer {
    
    @Override
    /**
     * 注册报警推送端点及跨域来源规则。
     */
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(myHandler(), "/ws")
                .setAllowedOrigins("*"); // 允许跨域，生产环境建议限制具体域名
    }
    
    @Bean
    public WebSocketHandler myHandler() {
        return new MyWebSocketHandler();
    }
}
