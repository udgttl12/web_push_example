package com.example.webpush.dto;

import lombok.Data;

@Data
public class SubscriptionRequest {
    private String endpoint;
    private Keys keys;
    private String userAgent;
    private String deviceType;

    @Data
    public static class Keys {
        private String p256dh;
        private String auth;
    }
}
