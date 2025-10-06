package com.example.webpush.repository;

import com.example.webpush.entity.PushTopic;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PushTopicRepository extends JpaRepository<PushTopic, Long> {

    Optional<PushTopic> findByName(String name);

    List<PushTopic> findByIsActive(Boolean isActive);
}
