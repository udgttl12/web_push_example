# 웹 푸시 MVP 테이블 설계서

## 1. 개요

이 문서는 **안드로이드 우선 웹 푸시 MVP**를 위한 MariaDB 테이블
설계서입니다.

------------------------------------------------------------------------

## 2. 테이블 구조 요약

  테이블명                  설명
  ------------------------- ------------------------------------
  push_subscription         구독 정보 (브라우저/디바이스 단위)
  push_message              발송 메시지(캠페인/테스트 단위)
  push_send_log             메시지 발송 로그
  push_open_event           알림 클릭/열람 이벤트
  push_topic                토픽/세그먼트 정의 (옵션)
  push_subscription_topic   구독-토픽 매핑 (옵션)
  push_consent_history      알림 동의/철회 이력 (옵션)

------------------------------------------------------------------------

## 3. DDL 정의

``` sql
CREATE DATABASE IF NOT EXISTS push_mvp CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE push_mvp;

-- (이하 동일: push_subscription, push_message, push_send_log, push_open_event,
-- push_topic, push_subscription_topic, push_consent_history 정의)
```

------------------------------------------------------------------------

## 4. 설계 포인트

-   긴 endpoint URL을 대비해 `endpoint_sha256` 컬럼으로 인덱싱 최적화
-   `p256dh_key` / `auth_key` 길이 여유 확보
-   `is_active`로 구독 상태 관리
-   전송/클릭 로그 분리로 성능 확보
-   토픽/동의 이력으로 향후 기능 확장 대비

------------------------------------------------------------------------

## 5. ER 다이어그램 개요

    push_message 1 ───< push_send_log >─── 1 push_subscription
                                       │
                                       └──< push_open_event
    push_subscription 1 ───< push_subscription_topic >─── 1 push_topic
    push_subscription 1 ───< push_consent_history

------------------------------------------------------------------------

## 6. 작성 정보

-   작성일: 2025-10-06
-   작성자: 시스템 설계 초안 (ChatGPT)
