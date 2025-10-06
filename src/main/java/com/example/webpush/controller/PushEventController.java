package com.example.webpush.controller;

import com.example.webpush.entity.PushOpenEvent;
import com.example.webpush.repository.PushOpenEventRepository;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/push/event")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class PushEventController {

    private final PushOpenEventRepository openEventRepository;

    @PostMapping("/track")
    public ResponseEntity<Void> trackEvent(@RequestBody EventTrackRequest request) {
        PushOpenEvent event = PushOpenEvent.builder()
                .sendLogId(request.getSendLogId())
                .eventType(request.getEventType())
                .build();

        openEventRepository.save(event);
        return ResponseEntity.ok().build();
    }

    @Data
    public static class EventTrackRequest {
        private Long sendLogId;
        private String eventType; // notification_click, notification_close
    }
}
