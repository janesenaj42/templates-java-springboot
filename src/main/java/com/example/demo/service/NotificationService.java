package com.example.demo.service;

import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.time.Duration;
import java.util.Random;

@Service
public class NotificationService {

    private final Random random = new Random();

    public Flux<String> streamNotifications() {
        return Flux.interval(Duration.ofSeconds(5))
                .map(tick -> "Notification: " + generateRandomMessage())
                .delayElements(Duration.ofMillis(random.nextInt(2000)));
    }

    private String generateRandomMessage() {
        String[] messages = { "New message", "System alert", "Friend request", "Task update" };
        return messages[random.nextInt(messages.length)];
    }
}