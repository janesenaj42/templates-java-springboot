package com.example.demo.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.service.NotificationService;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.SignalType;

@Slf4j
@RestController
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @MessageMapping("/request-response")
    public Mono<String> currentMarketData(String input) {
        log.info("Request and response. Received input {}", input);
        return notificationService.updateString(input);
    }

    @MessageMapping("/fire-forget")
    public Mono<Void> collectMarketData(String input) {
        log.info("Fire and forget. Received input {}", input);
        return Mono.empty();
    }

    /**
     * Any authenticated users can access.
     * @param input Input data.
     * @return Flux of notification string.
     */
    @MessageMapping("/request-stream")
    public Flux<String> feedMarketData(String input) {
        log.info("Request and stream. Received input {}", input);
        return notificationService.streamNotifications();
    }

    /**
     * Authenticated users with 'normaluser' client role can access.
     * @param input Input data.
     * @return Flux of notification string.
     */
    @MessageMapping("/normal.request-stream")
    public Flux<String> feedMarketDataUser(String input) {
        log.info("Request and stream for normal user. Received input {}", input);
        return notificationService.streamNotifications();
    }

    /**
     * Authenticated users with 'superuser' client role can access.
     * @param input Input data.
     * @return Flux of notification string.
     */
    @MessageMapping("/admin.request-stream")
    public Flux<String> feedMarketDataAdmin(String input) {
        log.info("Request and stream for admin. Received input {}", input);
        return notificationService.streamNotifications();
    }

    /**
     * Streaming JSON example.
     * @return Flux of notification string.
     */
    @GetMapping(value = "/notifications", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> getNotifications() {
        return notificationService.streamNotifications()
                .doOnCancel(() -> System.out.println("Client canceled the request. Stopping stream..."))
                .doOnError(error -> System.out.println("Error occurred: " + error.getMessage()))
                .onErrorResume(error -> Flux.empty())
                .doFinally(signalType -> {
                    if (signalType == SignalType.CANCEL) {
                        System.out.println("Flux Completed due to Cancelation");
                    } else {
                        System.out.println("Flux Completed due to Completion or Error");
                    }
                });
    }

}
