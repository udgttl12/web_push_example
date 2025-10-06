# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

**중요: 모든 응답은 한국어로 작성해주세요.**

## Project Overview

Spring Boot web push notification system targeting Android devices, using VAPID authentication and MariaDB for persistence.

**개발 환경: Windows**

## Build & Run

```bash
# Run application
gradlew.bat bootRun

# Build JAR
gradlew.bat build

# Run tests
gradlew.bat test
```

**Prerequisites:**
- Java 17+
- MariaDB 10.x+
- Database schema initialized from `src/main/resources/db/schema.sql`

## Database Setup

```bash
# Initialize database (run in MySQL/MariaDB client)
source src/main/resources/db/schema.sql
```

Schema creates `push_mvp` database with tables:
- `push_subscription` - subscription endpoints with SHA-256 indexed endpoint URLs
- `push_message` - push notification messages/campaigns
- `push_send_log` - delivery logs linked to messages and subscriptions
- `push_open_event` - click/interaction tracking
- `push_topic`, `push_subscription_topic`, `push_consent_history` - optional topic/consent features

## Configuration

VAPID keys required in `src/main/resources/application.yml`:

```yaml
web-push:
  vapid:
    public-key: <base64-url-encoded-public-key>
    private-key: <base64-url-encoded-private-key>
    subject: mailto:your-email@example.com
```

Generate keys at https://vapidkeys.com/ or with `web-push generate-vapid-keys` (Node.js).

Environment variable overrides supported:
- `VAPID_PUBLIC_KEY`
- `VAPID_PRIVATE_KEY`
- `VAPID_SUBJECT`

Database configuration in `application.yml` or via:
- `DB_URL`
- `DB_USERNAME`
- `DB_PASSWORD`

## Architecture

**Core Service Flow:**

1. **Subscription Management** (`PushSubscriptionService`):
   - Generates SHA-256 hash of endpoint URLs for efficient indexing
   - Stores VAPID keys (p256dh, auth) from browser subscription objects
   - Soft-delete pattern via `is_active` flag

2. **Push Delivery** (`WebPushService`):
   - Uses `nl.martijndwars:web-push` library with BouncyCastle crypto
   - Constructs JSON payload with notification properties
   - Handles 410 Gone responses by auto-deactivating expired subscriptions
   - Transactional logging of all send attempts in `push_send_log`

3. **Configuration** (`WebPushConfig`):
   - Initializes BouncyCastle provider in static block (required for VAPID)
   - Creates singleton `PushService` bean with VAPID credentials

**Key Design Patterns:**
- Endpoint URLs stored with SHA-256 hash for efficient lookups (endpoints can be >500 chars)
- Subscription updates reactivate existing records rather than creating duplicates
- Device type filtering (e.g., "android") for targeted push campaigns
- TTL and urgency configurable per message (defaults: 86400s, "normal")

## API Endpoints

**Subscription:**
- `GET /api/push/subscription/vapid-public-key` - retrieve public key for client-side subscription
- `POST /api/push/subscription/subscribe` - register push subscription
- `POST /api/push/subscription/unsubscribe?endpointSha256={hash}` - deactivate subscription
- `GET /api/push/subscription/active` - list active subscriptions

**Messaging:**
- `POST /api/push/message/send` - create and send push notification
- `POST /api/push/message/send/{messageId}?deviceType={type}` - resend existing message

**Events:**
- `POST /api/push/event/track` - track notification click/interaction

## Testing

Web push requires HTTPS or localhost. For remote device testing:
- Use ngrok: `ngrok http 8080`
- Or configure SSL in `application.yml` (see VAPID_SETUP.md)

Test with Android Chrome recommended. Debug via Chrome DevTools > Application > Service Workers.

## Dependencies

- Spring Boot 3.4.1 (Web, JPA)
- MariaDB JDBC 3.1.4
- `nl.martijndwars:web-push:5.1.1` - VAPID push protocol
- `org.bouncycastle:bcprov-jdk15on:1.70` - elliptic curve cryptography
- Lombok for boilerplate reduction
