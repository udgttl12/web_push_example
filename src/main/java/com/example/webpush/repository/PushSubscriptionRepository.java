package com.example.webpush.repository;

import com.example.webpush.entity.PushSubscription;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PushSubscriptionRepository extends JpaRepository<PushSubscription, Long> {

    Optional<PushSubscription> findByEndpointSha256(String endpointSha256);

    List<PushSubscription> findByIsActive(Boolean isActive);

    List<PushSubscription> findByDeviceType(String deviceType);

    List<PushSubscription> findByIsActiveAndDeviceType(Boolean isActive, String deviceType);
}
