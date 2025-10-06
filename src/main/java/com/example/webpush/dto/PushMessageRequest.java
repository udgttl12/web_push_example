package com.example.webpush.dto;

import lombok.Data;

@Data
public class PushMessageRequest {
    private String title;
    private String body;
    private String icon;
    private String badge;
    private String image;
    private String url;
    private String tag;
    private Boolean requireInteraction;
    private Integer ttl;
    private String urgency;
    private Long topicId;
    private String deviceType; // for filtering subscriptions
}
