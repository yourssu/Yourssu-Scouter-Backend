-- 발송 claim 시각 (SENDING 고착 복구용)
ALTER TABLE mail_reservation
    ADD COLUMN claimed_at TIMESTAMP(6) NULL;
