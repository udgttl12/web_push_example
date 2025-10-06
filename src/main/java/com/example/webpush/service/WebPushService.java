package com.example.webpush.service;

import com.example.webpush.dto.PushMessageRequest;
import com.example.webpush.entity.PushMessage;
import com.example.webpush.entity.PushSendLog;
import com.example.webpush.entity.PushSubscription;
import com.example.webpush.repository.PushMessageRepository;
import com.example.webpush.repository.PushSendLogRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import nl.martijndwars.webpush.Notification;
import nl.martijndwars.webpush.PushService;
import org.jose4j.lang.JoseException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

@Service
@RequiredArgsConstructor
public class WebPushService {

    private final PushService pushService;
    private final PushSubscriptionService subscriptionService;
    private final PushMessageRepository messageRepository;
    private final PushSendLogRepository sendLogRepository;
    private final ObjectMapper objectMapper;

    @Transactional
    public PushMessage createMessage(PushMessageRequest request) {
        PushMessage message = PushMessage.builder()
                .title(request.getTitle())
                .body(request.getBody())
                .icon(request.getIcon())
                .badge(request.getBadge())
                .image(request.getImage())
                .url(request.getUrl())
                .tag(request.getTag())
                .requireInteraction(request.getRequireInteraction() != null ? request.getRequireInteraction() : false)
                .ttl(request.getTtl() != null ? request.getTtl() : 86400)
                .urgency(request.getUrgency() != null ? request.getUrgency() : "normal")
                .topicId(request.getTopicId())
                .status("pending")
                .build();

        return messageRepository.save(message);
    }

    @Transactional
    public void sendPushNotification(Long messageId, String deviceType) {
        PushMessage message = messageRepository.findById(messageId)
                .orElseThrow(() -> new RuntimeException("Message not found"));

        List<PushSubscription> subscriptions = deviceType != null
                ? subscriptionService.getActiveSubscriptionsByDeviceType(deviceType)
                : subscriptionService.getActiveSubscriptions();

        message.setStatus("sending");
        message.setSentAt(LocalDateTime.now());
        messageRepository.save(message);

        for (PushSubscription subscription : subscriptions) {
            sendToSubscription(message, subscription);
        }

        message.setStatus("sent");
        messageRepository.save(message);
    }

    private void sendToSubscription(PushMessage message, PushSubscription subscription) {
        try {
            // Prepare notification payload
            Map<String, Object> payload = new HashMap<>();
            payload.put("title", message.getTitle());
            payload.put("body", message.getBody());
            if (message.getIcon() != null) payload.put("icon", message.getIcon());
            if (message.getBadge() != null) payload.put("badge", message.getBadge());
            if (message.getImage() != null) payload.put("image", message.getImage());
            if (message.getUrl() != null) payload.put("url", message.getUrl());
            if (message.getTag() != null) payload.put("tag", message.getTag());
            payload.put("requireInteraction", message.getRequireInteraction());

            String payloadJson = objectMapper.writeValueAsString(payload);

            // Create notification
            Notification notification = new Notification(
                    subscription.getEndpoint(),
                    subscription.getP256dhKey(),
                    subscription.getAuthKey(),
                    payloadJson.getBytes()
            );

            // Send notification
            pushService.send(notification);

            // Log success
            PushSendLog log = PushSendLog.builder()
                    .messageId(message.getId())
                    .subscriptionId(subscription.getId())
                    .status("success")
                    .statusCode(201)
                    .build();
            sendLogRepository.save(log);

            // Update last sent time
            subscription.setLastSentAt(LocalDateTime.now());

        } catch (GeneralSecurityException | IOException | JoseException | ExecutionException | InterruptedException e) {
            // Log failure
            PushSendLog log = PushSendLog.builder()
                    .messageId(message.getId())
                    .subscriptionId(subscription.getId())
                    .status("failed")
                    .errorMessage(e.getMessage())
                    .build();
            sendLogRepository.save(log);

            // Deactivate subscription if it's expired (410 Gone)
            if (e.getMessage() != null && e.getMessage().contains("410")) {
                subscription.setIsActive(false);
            }
        }
    }
}
