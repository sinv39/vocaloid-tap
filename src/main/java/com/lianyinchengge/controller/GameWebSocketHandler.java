package com.lianyinchengge.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lianyinchengge.entity.Song;
import com.lianyinchengge.service.SongService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Component
public class GameWebSocketHandler extends TextWebSocketHandler {

    @Autowired
    private SongService songService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        // 连接建立时的处理
        System.out.println("WebSocket连接建立: " + session.getId());
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        try {
            // 解析客户端发送的消息
            String payload = message.getPayload();
            @SuppressWarnings("unchecked")
            Map<String, String> request = objectMapper.readValue(payload, Map.class);
            String melody = request.get("melody");

            if (melody == null || melody.isEmpty()) {
                sendResponse(session, false, null, null);
                return;
            }

            // 查询匹配的歌曲
            Optional<Song> songOpt = songService.searchByMelody(melody);

            if (songOpt.isPresent()) {
                Song song = songOpt.get();
                // 将音频数据转换为 base64
                String audioBase64 = java.util.Base64.getEncoder().encodeToString(song.getAudioData());
                String audioData = "data:audio/mpeg;base64," + audioBase64;
                sendResponse(session, true, song.getTitle(), audioData);
            } else {
                sendResponse(session, false, null, null);
            }
        } catch (Exception e) {
            System.err.println("处理WebSocket消息时出错: " + e.getMessage());
            e.printStackTrace();
            sendResponse(session, false, null, null);
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        // 连接关闭时的处理
        System.out.println("WebSocket连接关闭: " + session.getId());
    }

    private void sendResponse(WebSocketSession session, boolean found, String title, String audioData) {
        try {
            Map<String, Object> response = new HashMap<>();
            response.put("found", found);
            if (found) {
                response.put("title", title);
                response.put("audioData", audioData);
            }
            String jsonResponse = objectMapper.writeValueAsString(response);
            session.sendMessage(new TextMessage(jsonResponse));
        } catch (Exception e) {
            System.err.println("发送WebSocket响应时出错: " + e.getMessage());
            e.printStackTrace();
        }
    }
}

