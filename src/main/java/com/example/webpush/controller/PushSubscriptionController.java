package com.example.webpush.controller;

import com.example.webpush.dto.SubscriptionRequest;
import com.example.webpush.entity.PushSubscription;
import com.example.webpush.service.PushSubscriptionService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/push/subscription")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class PushSubscriptionController {

    private final PushSubscriptionService subscriptionService;

    @Value("${web-push.vapid.public-key}")
    private String vapidPublicKey;

    @GetMapping("/vapid-public-key")
    public ResponseEntity<Map<String, String>> getVapidPublicKey() {
        return ResponseEntity.ok(Map.of("publicKey", vapidPublicKey));
    }

    @PostMapping("/subscribe")
    public ResponseEntity<PushSubscription> subscribe(@RequestBody SubscriptionRequest request) {
        PushSubscription subscription = subscriptionService.subscribe(request);
        return ResponseEntity.ok(subscription);
    }

    @PostMapping("/unsubscribe")
    public ResponseEntity<Void> unsubscribe(@RequestParam String endpointSha256) {
        subscriptionService.unsubscribe(endpointSha256);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/active")
    public ResponseEntity<List<PushSubscription>> getActiveSubscriptions() {
        return ResponseEntity.ok(subscriptionService.getActiveSubscriptions());
    }
}
