package com.example.webpush.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "push_message")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PushMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String body;

    @Column(length = 512)
    private String icon;

    @Column(length = 512)
    private String badge;

    @Column(length = 512)
    private String image;

    @Column(length = 512)
    private String url;

    @Column(length = 100)
    private String tag;

    @Column(name = "require_interaction")
    @Builder.Default
    private Boolean requireInteraction = false;

    @Column(nullable = false)
    @Builder.Default
    private Integer ttl = 86400; // 24 hours

    @Column(length = 20)
    @Builder.Default
    private String urgency = "normal";

    @Column(name = "topic_id")
    private Long topicId;

    @Column(name = "scheduled_at")
    private LocalDateTime scheduledAt;

    @Column(name = "sent_at")
    private LocalDateTime sentAt;

    @Column(length = 20, nullable = false)
    @Builder.Default
    private String status = "pending";

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
