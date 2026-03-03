-- Add status column to mail_reservation
-- 사전조건: mail_reservation 테이블이 존재해야 함 (baseline 또는 V1에서 생성)
-- Hibernate @Enumerated(STRING) + @Column(length=20)과 호환되도록 VARCHAR(20) 사용
ALTER TABLE mail_reservation
    ADD COLUMN status VARCHAR(20) NOT NULL DEFAULT 'PENDING_SEND';

-- 기존 데이터 보정: 예약 시간이 아직 지나지 않은 행은 SCHEDULED로 설정
-- reservation_time은 JPA Instant로 UTC 저장됨. UTC_TIMESTAMP()와 비교
UPDATE mail_reservation
SET status = 'SCHEDULED'
WHERE reservation_time > UTC_TIMESTAMP();
