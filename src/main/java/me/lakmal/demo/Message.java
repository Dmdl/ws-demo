package me.lakmal.demo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Message {
    private String channel;
    private String message;
    private long created = Instant.now().getEpochSecond();

    public Message(String message) {
        this.message = message;
    }
}
