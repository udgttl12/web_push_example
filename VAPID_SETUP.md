# VAPID 키 생성 및 설정 가이드

## VAPID란?

VAPID (Voluntary Application Server Identification)은 웹 푸시 알림 서버를 식별하기 위한 표준입니다. 푸시 서비스 제공자가 누가 알림을 보내는지 확인할 수 있게 해줍니다.

## 1. VAPID 키 생성 방법

### 방법 1: 온라인 도구 사용 (가장 간단)

1. https://vapidkeys.com/ 접속
2. "Generate VAPID Keys" 버튼 클릭
3. Public Key와 Private Key 복사

### 방법 2: Node.js 사용

```bash
# web-push 설치
npm install -g web-push

# VAPID 키 생성
web-push generate-vapid-keys
```

출력 예시:
```
=======================================

Public Key:
BOxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx

Private Key:
yyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyy

=======================================
```

### 방법 3: Java 코드 사용

```java
import nl.martijndwars.webpush.Utils;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

import java.security.KeyPair;
import java.security.Security;

public class VapidKeyGenerator {
    public static void main(String[] args) throws Exception {
        Security.addProvider(new BouncyCastleProvider());

        KeyPair keyPair = Utils.generateKeyPair();
        byte[] publicKey = Utils.savePublicKey(keyPair.getPublic());
        byte[] privateKey = Utils.savePrivateKey(keyPair.getPrivate());

        System.out.println("Public Key: " + java.util.Base64.getUrlEncoder()
            .withoutPadding()
            .encodeToString(publicKey));
        System.out.println("Private Key: " + java.util.Base64.getUrlEncoder()
            .withoutPadding()
            .encodeToString(privateKey));
    }
}
```

## 2. application.yml에 설정

생성한 키를 `src/main/resources/application.yml`에 추가:

```yaml
web-push:
  vapid:
    public-key: BOxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx
    private-key: yyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyy
    subject: mailto:your-email@example.com
```

⚠️ **중요:**
- `subject`는 `mailto:` 또는 `https://`로 시작해야 합니다
- Private Key는 절대 외부에 노출하면 안 됩니다
- `.gitignore`에 설정 파일이 포함되어 있는지 확인하세요

## 3. 환경 변수로 관리 (권장)

보안을 위해 환경 변수로 관리하는 것을 권장합니다:

**application.yml:**
```yaml
web-push:
  vapid:
    public-key: ${VAPID_PUBLIC_KEY}
    private-key: ${VAPID_PRIVATE_KEY}
    subject: ${VAPID_SUBJECT:mailto:admin@example.com}
```

**실행 시 환경 변수 전달:**
```bash
export VAPID_PUBLIC_KEY="BOxxxxxx..."
export VAPID_PRIVATE_KEY="yyyyyyyy..."
export VAPID_SUBJECT="mailto:your-email@example.com"

./gradlew bootRun
```

**또는 .env 파일 사용:**
```bash
# .env 파일 생성
VAPID_PUBLIC_KEY=BOxxxxxx...
VAPID_PRIVATE_KEY=yyyyyyyy...
VAPID_SUBJECT=mailto:your-email@example.com

# .gitignore에 추가
echo ".env" >> .gitignore
```

## 4. 검증

애플리케이션 실행 후 Public Key 확인:

```bash
curl http://localhost:8080/api/push/subscription/vapid-public-key
```

응답:
```json
{
  "publicKey": "BOxxxxxx..."
}
```

## 문제 해결

### "Invalid VAPID Key" 에러
- Base64 URL-safe 인코딩인지 확인 (`+` 대신 `-`, `/` 대신 `_` 사용)
- Padding(`=`)이 제거되었는지 확인

### "VAPID subject must be a URL or mailto:" 에러
- subject가 `mailto:` 또는 `https://`로 시작하는지 확인

### 키 형식 변환

만약 다른 형식의 키를 받았다면:

```bash
# PEM to Base64 URL-safe
openssl ec -in private.pem -pubout -outform DER | tail -c 65 | base64 | tr -d '=' | tr '/+' '_-'
```

## 보안 권장사항

1. ✅ Private Key는 절대 클라이언트에 노출하지 않기
2. ✅ 환경 변수나 비밀 관리 시스템 사용
3. ✅ `.gitignore`에 설정 파일 추가
4. ✅ 프로덕션에서는 키 교체 전략 수립
5. ✅ 키가 유출되면 즉시 새 키 생성 및 교체
