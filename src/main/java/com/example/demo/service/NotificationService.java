package com.example.demo.service;

import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;

import java.time.Duration;
import java.util.Random;

@Service
public class NotificationService {

    private final Sinks.Many<String> sink;
    private final Random random = new Random();

    public NotificationService() {
        this.sink = Sinks.many().multicast().onBackpressureBuffer();

        // Simulate random notifications
        Flux.interval(Duration.ofSeconds(1)) // Generate ticks every second
                .map(tick -> generateRandomNotification())
                .doOnNext(sink::tryEmitNext) // Push each notification into the sink
                .subscribe();
    }

    public Flux<String> streamNotifications() {
        return sink.asFlux();
    }

    private String generateRandomNotification() {
        String[] messages = { "System alert", "Friend request", "Task update" };
        return messages[random.nextInt(messages.length)];
    }

    public Mono<String> updateString(String input) {
        return Mono.just(String.format("Updated string: %s", input));
    }
}