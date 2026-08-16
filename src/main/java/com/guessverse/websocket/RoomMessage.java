package com.guessverse.websocket;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RoomMessage {

    private String type;

    private Object payload;
}