package com.guessverse.websocket;

import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class WebSocketEventPublisher {

    private final SimpMessagingTemplate messagingTemplate;

    public void publish(String roomCode, RoomMessage message) {

        messagingTemplate.convertAndSend(
                "/topic/room/" + roomCode,
                message
        );
    }
}