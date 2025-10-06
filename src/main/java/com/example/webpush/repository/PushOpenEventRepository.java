package com.example.webpush.repository;

import com.example.webpush.entity.PushOpenEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PushOpenEventRepository extends JpaRepository<PushOpenEvent, Long> {

    List<PushOpenEvent> findBySendLogId(Long sendLogId);

    List<PushOpenEvent> findByEventType(String eventType);
}
