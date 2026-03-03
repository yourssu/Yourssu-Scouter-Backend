# 메일 예약 status 컬럼 마이그레이션 가이드

## 개요

`mail_reservation` 테이블에 `status` 컬럼을 추가하여 예약 상태를 명시적으로 관리합니다.

| status | 설명 |
|--------|------|
| SCHEDULED | 예약됨 (발송 시각 전) |
| PENDING_SEND | 예약 시간 지났는데 아직 발송 안 됨 (오류/재시도 대기) |
| SENT | 예약해서 이미 발송 완료 |

## 사전 조건

- MySQL 8.0 이상
- `mail_reservation` 테이블이 이미 존재해야 함 (Hibernate 또는 기존 baseline으로 생성된 스키마)
- 애플리케이션 배포 전 마이그레이션 실행 권장

## 마이그레이션 내용 (V2)

1. `status VARCHAR(20) NOT NULL DEFAULT 'PENDING_SEND'` 컬럼 추가
2. 기존 데이터 보정: `reservation_time > UTC_TIMESTAMP()` 인 행은 `status = 'SCHEDULED'`로 설정

## 실행 방법

### 방법 1: 스크립트 실행

```bash
./scripts/run-flyway-migrate.sh
```

`.env.local`에 `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`가 설정되어 있어야 합니다.

### 방법 2: 애플리케이션 기동 시 자동 실행 (권장)

Flyway가 활성화된 프로필(dev, prod, local-dev-db)로 애플리케이션을 기동하면 마이그레이션이 자동 적용됩니다.

```bash
# dev DB 사용 시
source .env.local   # DB_URL, DB_USERNAME, DB_PASSWORD 로드
./gradlew bootRun --args='--spring.profiles.active=local,local-dev-db'
```

또는:

```bash
./scripts/run-local-with-dev-db.sh
```

### 방법 2: Flyway CLI로 수동 실행

```bash
# Flyway CLI 설치 후
flyway -url="$DB_URL" -user="$DB_USERNAME" -password="$DB_PASSWORD" \
  -locations=filesystem:src/main/resources/db/migration \
  -baselineOnMigrate=true \
  -baselineVersion=1 \
  migrate
```

## 롤백

이 마이그레이션은 스키마 변경만 수행합니다. 롤백이 필요한 경우:

```sql
ALTER TABLE mail_reservation DROP COLUMN status;
```

**주의**: 롤백 후 애플리케이션 코드는 `status` 컬럼을 사용하므로, 롤백 시 애플리케이션도 이전 버전으로 되돌려야 합니다.

## 검증

마이그레이션 후 확인:

```sql
-- status 컬럼 존재 확인
DESCRIBE mail_reservation;

-- 기존 데이터 상태 확인
SELECT id, mail_id, reservation_time, status FROM mail_reservation;
```

## 관련 API 변경

- `GET /api/mails/reservation` - 목록 응답에 `status` 필드 추가
- `GET /api/mails/reservation/{id}` - 상세 응답에 `status` 필드 추가
- `POST /api/mails/reservation/{id}/retry` - PENDING_SEND 상태 재전송 API 추가
