package com.example.webpush.repository;

import com.example.webpush.entity.PushSendLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PushSendLogRepository extends JpaRepository<PushSendLog, Long> {

    List<PushSendLog> findByMessageId(Long messageId);

    List<PushSendLog> findBySubscriptionId(Long subscriptionId);

    List<PushSendLog> findByStatus(String status);
}
