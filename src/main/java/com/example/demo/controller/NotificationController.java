package com.example.demo.controller;

import com.example.demo.service.NotificationService;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.SignalType;

@RestController
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

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
