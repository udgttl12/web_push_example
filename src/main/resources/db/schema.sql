-- 웹 푸시 MVP 데이터베이스 스키마
CREATE DATABASE IF NOT EXISTS push_mvp CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE push_mvp;

-- 1. 푸시 구독 정보 테이블
CREATE TABLE push_subscription (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '구독 ID',
    endpoint VARCHAR(512) NOT NULL COMMENT 'Push API endpoint URL',
    endpoint_sha256 CHAR(64) NOT NULL COMMENT 'endpoint SHA256 해시 (인덱싱용)',
    p256dh_key VARCHAR(255) NOT NULL COMMENT 'P-256 ECDH 공개키 (base64)',
    auth_key VARCHAR(255) NOT NULL COMMENT 'Auth secret (base64)',
    user_agent VARCHAR(512) COMMENT 'User-Agent 정보',
    device_type VARCHAR(50) COMMENT '디바이스 타입 (android, ios, desktop 등)',
    is_active BOOLEAN DEFAULT TRUE COMMENT '활성화 여부',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '생성 시각',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정 시각',
    last_sent_at DATETIME COMMENT '마지막 발송 시각',

    UNIQUE KEY uk_endpoint_sha256 (endpoint_sha256),
    INDEX idx_is_active (is_active),
    INDEX idx_device_type (device_type),
    INDEX idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='푸시 구독 정보';

-- 2. 푸시 메시지 테이블
CREATE TABLE push_message (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '메시지 ID',
    title VARCHAR(255) NOT NULL COMMENT '알림 제목',
    body TEXT NOT NULL COMMENT '알림 본문',
    icon VARCHAR(512) COMMENT '알림 아이콘 URL',
    badge VARCHAR(512) COMMENT '배지 아이콘 URL',
    image VARCHAR(512) COMMENT '알림 이미지 URL',
    url VARCHAR(512) COMMENT '클릭 시 이동할 URL',
    tag VARCHAR(100) COMMENT '알림 태그 (중복 방지용)',
    require_interaction BOOLEAN DEFAULT FALSE COMMENT '사용자 액션 필요 여부',
    ttl INT DEFAULT 86400 COMMENT 'TTL (초 단위, 기본 24시간)',
    urgency VARCHAR(20) DEFAULT 'normal' COMMENT '긴급도 (very-low, low, normal, high)',
    topic_id BIGINT COMMENT '토픽 ID (옵션)',
    scheduled_at DATETIME COMMENT '예약 발송 시각',
    sent_at DATETIME COMMENT '실제 발송 시각',
    status VARCHAR(20) DEFAULT 'pending' COMMENT '상태 (pending, sending, sent, failed)',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '생성 시각',

    INDEX idx_status (status),
    INDEX idx_scheduled_at (scheduled_at),
    INDEX idx_topic_id (topic_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='푸시 메시지';

-- 3. 푸시 발송 로그 테이블
CREATE TABLE push_send_log (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '로그 ID',
    message_id BIGINT NOT NULL COMMENT '메시지 ID',
    subscription_id BIGINT NOT NULL COMMENT '구독 ID',
    status VARCHAR(20) NOT NULL COMMENT '발송 상태 (success, failed, expired)',
    status_code INT COMMENT 'HTTP 응답 코드',
    error_message TEXT COMMENT '에러 메시지',
    sent_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '발송 시각',

    INDEX idx_message_id (message_id),
    INDEX idx_subscription_id (subscription_id),
    INDEX idx_status (status),
    INDEX idx_sent_at (sent_at),

    FOREIGN KEY (message_id) REFERENCES push_message(id) ON DELETE CASCADE,
    FOREIGN KEY (subscription_id) REFERENCES push_subscription(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='푸시 발송 로그';

-- 4. 푸시 열람/클릭 이벤트 테이블
CREATE TABLE push_open_event (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '이벤트 ID',
    send_log_id BIGINT NOT NULL COMMENT '발송 로그 ID',
    event_type VARCHAR(20) NOT NULL COMMENT '이벤트 타입 (notification_click, notification_close)',
    opened_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '이벤트 발생 시각',

    INDEX idx_send_log_id (send_log_id),
    INDEX idx_event_type (event_type),
    INDEX idx_opened_at (opened_at),

    FOREIGN KEY (send_log_id) REFERENCES push_send_log(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='푸시 열람/클릭 이벤트';

-- 5. 토픽/세그먼트 테이블 (옵션)
CREATE TABLE push_topic (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '토픽 ID',
    name VARCHAR(100) NOT NULL COMMENT '토픽 이름',
    description TEXT COMMENT '토픽 설명',
    is_active BOOLEAN DEFAULT TRUE COMMENT '활성화 여부',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '생성 시각',

    UNIQUE KEY uk_name (name),
    INDEX idx_is_active (is_active)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='토픽/세그먼트';

-- 6. 구독-토픽 매핑 테이블 (옵션)
CREATE TABLE push_subscription_topic (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '매핑 ID',
    subscription_id BIGINT NOT NULL COMMENT '구독 ID',
    topic_id BIGINT NOT NULL COMMENT '토픽 ID',
    subscribed_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '구독 시각',

    UNIQUE KEY uk_subscription_topic (subscription_id, topic_id),
    INDEX idx_topic_id (topic_id),

    FOREIGN KEY (subscription_id) REFERENCES push_subscription(id) ON DELETE CASCADE,
    FOREIGN KEY (topic_id) REFERENCES push_topic(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='구독-토픽 매핑';

-- 7. 알림 동의/철회 이력 테이블 (옵션)
CREATE TABLE push_consent_history (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '이력 ID',
    subscription_id BIGINT NOT NULL COMMENT '구독 ID',
    action VARCHAR(20) NOT NULL COMMENT '액션 (granted, denied, revoked)',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '발생 시각',

    INDEX idx_subscription_id (subscription_id),
    INDEX idx_action (action),
    INDEX idx_created_at (created_at),

    FOREIGN KEY (subscription_id) REFERENCES push_subscription(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='알림 동의/철회 이력';
