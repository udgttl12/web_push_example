package com.example.webpush.repository;

import com.example.webpush.entity.PushMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface PushMessageRepository extends JpaRepository<PushMessage, Long> {

    List<PushMessage> findByStatus(String status);

    List<PushMessage> findByScheduledAtBefore(LocalDateTime dateTime);

    List<PushMessage> findByTopicId(Long topicId);
}
