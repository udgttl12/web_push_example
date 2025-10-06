package com.example.webpush.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "push_open_event")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PushOpenEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "send_log_id", nullable = false)
    private Long sendLogId;

    @Column(name = "event_type", nullable = false, length = 20)
    private String eventType;

    @Column(name = "opened_at", nullable = false)
    private LocalDateTime openedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "send_log_id", insertable = false, updatable = false)
    private PushSendLog pushSendLog;

    @PrePersist
    protected void onCreate() {
        openedAt = LocalDateTime.now();
    }
}
