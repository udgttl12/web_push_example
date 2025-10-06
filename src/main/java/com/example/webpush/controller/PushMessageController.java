package com.example.webpush.controller;

import com.example.webpush.dto.PushMessageRequest;
import com.example.webpush.entity.PushMessage;
import com.example.webpush.service.WebPushService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/push/message")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class PushMessageController {

    private final WebPushService webPushService;

    @PostMapping("/send")
    public ResponseEntity<PushMessage> sendPushNotification(@RequestBody PushMessageRequest request) {
        // Create message
        PushMessage message = webPushService.createMessage(request);

        // Send to all active subscriptions (or filtered by deviceType)
        webPushService.sendPushNotification(message.getId(), request.getDeviceType());

        return ResponseEntity.ok(message);
    }

    @PostMapping("/send/{messageId}")
    public ResponseEntity<Void> resendMessage(@PathVariable Long messageId, @RequestParam(required = false) String deviceType) {
        webPushService.sendPushNotification(messageId, deviceType);
        return ResponseEntity.ok().build();
    }
}
