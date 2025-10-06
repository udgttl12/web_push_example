# 웹 푸시 알림 MVP (Spring Boot)

안드로이드 우선 웹 푸시 알림 시스템 - Spring Boot + MariaDB 기반

## 🚀 주요 기능

- ✅ 웹 푸시 알림 구독/해지
- ✅ 푸시 메시지 발송
- ✅ 발송 로그 및 이벤트 트래킹
- ✅ 디바이스 타입별 필터링 (Android 우선)
- ✅ VAPID 인증 지원
- ✅ 토픽/세그먼트 기능 (확장 가능)

## 📋 사전 요구사항

- Java 17 이상
- MariaDB 10.x 이상
- Gradle 7.x 이상

## 🛠 설치 및 실행

### 1. 데이터베이스 설정

MariaDB에 데이터베이스와 테이블을 생성합니다:

```bash
mysql -u root -p < src/main/resources/db/schema.sql
```

또는 MariaDB 클라이언트에서:

```sql
source src/main/resources/db/schema.sql
```

### 2. VAPID 키 생성

웹 푸시를 위한 VAPID 키를 생성합니다:

**온라인 도구 사용:**
- https://vapidkeys.com/ 접속
- "Generate VAPID Keys" 클릭
- Public Key와 Private Key 복사

**또는 Node.js 사용:**
```bash
npm install -g web-push
web-push generate-vapid-keys
```

### 3. 환경 설정

`src/main/resources/application.yml` 파일을 수정합니다:

```yaml
spring:
  datasource:
    url: jdbc:mariadb://localhost:3306/push_mvp
    username: your_username
    password: your_password

web-push:
  vapid:
    public-key: YOUR_VAPID_PUBLIC_KEY
    private-key: YOUR_VAPID_PRIVATE_KEY
    subject: mailto:your-email@example.com
```

### 4. 애플리케이션 실행

```bash
# Gradle을 사용하여 실행
./gradlew bootRun

# 또는 빌드 후 실행
./gradlew build
java -jar build/libs/web-push-mvp-0.0.1-SNAPSHOT.jar
```

### 5. 웹 브라우저에서 테스트

```
http://localhost:8080
```

안드로이드 Chrome 브라우저에서 접속하여 테스트하는 것을 권장합니다.

## 📡 API 엔드포인트

### 구독 관련

**VAPID Public Key 조회**
```http
GET /api/push/subscription/vapid-public-key
```

**푸시 구독**
```http
POST /api/push/subscription/subscribe
Content-Type: application/json

{
  "endpoint": "https://...",
  "keys": {
    "p256dh": "...",
    "auth": "..."
  },
  "userAgent": "...",
  "deviceType": "android"
}
```

**구독 해지**
```http
POST /api/push/subscription/unsubscribe?endpointSha256={sha256}
```

**활성 구독 목록 조회**
```http
GET /api/push/subscription/active
```

### 메시지 발송

**푸시 메시지 전송**
```http
POST /api/push/message/send
Content-Type: application/json

{
  "title": "알림 제목",
  "body": "알림 내용",
  "icon": "/icon-192x192.png",
  "url": "https://example.com",
  "deviceType": "android",
  "ttl": 86400,
  "urgency": "normal"
}
```

**기존 메시지 재전송**
```http
POST /api/push/message/send/{messageId}?deviceType=android
```

### 이벤트 트래킹

**이벤트 기록**
```http
POST /api/push/event/track
Content-Type: application/json

{
  "sendLogId": 1,
  "eventType": "notification_click"
}
```

## 🏗 프로젝트 구조

```
src/
├── main/
│   ├── java/com/example/webpush/
│   │   ├── config/          # 설정 클래스
│   │   ├── controller/      # REST API 컨트롤러
│   │   ├── dto/            # 데이터 전송 객체
│   │   ├── entity/         # JPA 엔티티
│   │   ├── repository/     # JPA 리포지토리
│   │   └── service/        # 비즈니스 로직
│   └── resources/
│       ├── db/
│       │   └── schema.sql  # 데이터베이스 스키마
│       ├── static/         # 정적 파일 (HTML, JS)
│       └── application.yml # 애플리케이션 설정
└── test/                   # 테스트 코드
```

## 📊 데이터베이스 스키마

주요 테이블:
- `push_subscription` - 구독 정보
- `push_message` - 발송 메시지
- `push_send_log` - 발송 로그
- `push_open_event` - 클릭/열람 이벤트
- `push_topic` - 토픽/세그먼트 (옵션)
- `push_subscription_topic` - 구독-토픽 매핑 (옵션)
- `push_consent_history` - 동의 이력 (옵션)

상세 스키마는 `src/main/resources/db/schema.sql` 참조

## 🔧 개발 팁

### HTTPS 환경에서 테스트

웹 푸시는 HTTPS 또는 localhost에서만 동작합니다. 실제 디바이스에서 테스트하려면:

1. **ngrok 사용:**
```bash
ngrok http 8080
```

2. **또는 Spring Boot에 SSL 설정:**
```yaml
server:
  ssl:
    enabled: true
    key-store: classpath:keystore.p12
    key-store-password: your_password
    key-store-type: PKCS12
```

### 안드로이드 Chrome 디버깅

1. Chrome 개발자 도구 열기
2. Application > Service Workers 확인
3. Console에서 에러 로그 확인

## 🐛 문제 해결

### 구독이 안 되는 경우
- VAPID 키가 올바른지 확인
- HTTPS 환경인지 확인 (localhost 제외)
- 브라우저 알림 권한 확인

### 푸시가 도착하지 않는 경우
- Service Worker가 등록되었는지 확인
- 구독 정보가 DB에 저장되었는지 확인
- 발송 로그 테이블에서 에러 메시지 확인

### CORS 에러
- `@CrossOrigin` 어노테이션 설정 확인
- 프론트엔드와 백엔드 도메인 확인

## 📝 라이선스

MIT License

## 👤 작성자

- 웹 푸시 MVP 프로젝트
- 작성일: 2025-10-06
