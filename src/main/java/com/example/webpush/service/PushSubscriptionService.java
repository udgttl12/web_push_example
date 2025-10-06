package com.example.webpush.service;

import com.example.webpush.dto.SubscriptionRequest;
import com.example.webpush.entity.PushSubscription;
import com.example.webpush.repository.PushSubscriptionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PushSubscriptionService {

    private final PushSubscriptionRepository subscriptionRepository;

    @Transactional
    public PushSubscription subscribe(SubscriptionRequest request) {
        String endpointSha256 = generateSha256(request.getEndpoint());

        // Check if subscription already exists
        Optional<PushSubscription> existing = subscriptionRepository.findByEndpointSha256(endpointSha256);
        if (existing.isPresent()) {
            PushSubscription subscription = existing.get();
            subscription.setP256dhKey(request.getKeys().getP256dh());
            subscription.setAuthKey(request.getKeys().getAuth());
            subscription.setUserAgent(request.getUserAgent());
            subscription.setDeviceType(request.getDeviceType());
            subscription.setIsActive(true);
            return subscriptionRepository.save(subscription);
        }

        // Create new subscription
        PushSubscription subscription = PushSubscription.builder()
                .endpoint(request.getEndpoint())
                .endpointSha256(endpointSha256)
                .p256dhKey(request.getKeys().getP256dh())
                .authKey(request.getKeys().getAuth())
                .userAgent(request.getUserAgent())
                .deviceType(request.getDeviceType())
                .isActive(true)
                .build();

        return subscriptionRepository.save(subscription);
    }

    @Transactional
    public void unsubscribe(String endpointSha256) {
        subscriptionRepository.findByEndpointSha256(endpointSha256)
                .ifPresent(subscription -> {
                    subscription.setIsActive(false);
                    subscriptionRepository.save(subscription);
                });
    }

    public List<PushSubscription> getActiveSubscriptions() {
        return subscriptionRepository.findByIsActive(true);
    }

    public List<PushSubscription> getActiveSubscriptionsByDeviceType(String deviceType) {
        return subscriptionRepository.findByIsActiveAndDeviceType(true, deviceType);
    }

    private String generateSha256(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 algorithm not found", e);
        }
    }
}
