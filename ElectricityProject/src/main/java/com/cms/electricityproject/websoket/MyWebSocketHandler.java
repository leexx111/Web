package com.cms.electricityproject.websoket;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

// WebSocket 处理器
/**
 * 管理 WebSocket 连接并向客户端推送报警事件。
 */
public class MyWebSocketHandler extends TextWebSocketHandler {

    // 使用ConcurrentHashMap存储所有在线的WebSocket会话
    private static final ConcurrentHashMap<String, WebSocketSession> sessions = new ConcurrentHashMap<>();

    // 建立连接后，将会话存入Map
    @Override
    /**
     * 登记新建立的 WebSocket 会话。
     */
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        sessions.put(session.getId(), session);
        System.out.println("新的WebSocket连接已建立: " + session.getId());
    }

    // 连接关闭后，将会话从Map移除
    @Override
    /**
     * 移除已关闭的 WebSocket 会话。
     */
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        sessions.remove(session.getId());
        System.out.println("WebSocket连接已关闭: " + session.getId());
    }

    // 发送消息给指定客户端
    /**
     * 向指定会话发送文本消息。
     */
    public static void sendMessageToClient(String sessionId, String message) {
        WebSocketSession session = sessions.get(sessionId);
        if (session != null && session.isOpen()) {
            try {
                session.sendMessage(new TextMessage(message));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    private static final ObjectMapper mapper = new ObjectMapper(); // JSON序列化工具

    // 广播消息给所有客户端（接收Map类型）
    /**
     * 将报警事件序列化后广播给所有在线会话。
     */
    public static void broadcastMessage(Map<String, Object> messageMap) {
        try {
            // 将Map序列化为JSON字符串
            String jsonMessage = mapper.writeValueAsString(messageMap);

            // 遍历所有会话发送消息
            sessions.forEach((sessionId, session) -> {
                if (session.isOpen()) {
                    try {
                        session.sendMessage(new TextMessage(jsonMessage));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}