-- 메일 예약 DB 확인용 SQL (mail_reservation / mail 테이블)

-- 1. 예약 전체 목록 (예약 시각 기준 최신순)
SELECT
    r.id AS reservation_id,
    r.mail_id,
    r.reservation_time,
    m.sender_email_address,
    m.mail_subject,
    m.body_format
FROM mail_reservation r
LEFT JOIN mail m ON m.id = r.mail_id
ORDER BY r.reservation_time DESC;

-- 2. 예약 건수만 확인
SELECT COUNT(*) AS reservation_count FROM mail_reservation;

-- 3. 아직 발송 시점이 안 된 예약 (미래 시각) — 스케줄러가 아직 안 건드림
SELECT
    r.id,
    r.mail_id,
    r.reservation_time,
    m.mail_subject
FROM mail_reservation r
LEFT JOIN mail m ON m.id = r.mail_id
WHERE r.reservation_time > NOW()
ORDER BY r.reservation_time;

-- 4. 발송 대상이어야 하는데 남아 있는 예약 (과거 시각) — 스케줄러가 처리 못 한 것
SELECT
    r.id,
    r.mail_id,
    r.reservation_time,
    m.mail_subject,
    m.sender_email_address
FROM mail_reservation r
LEFT JOIN mail m ON m.id = r.mail_id
WHERE r.reservation_time <= NOW()
ORDER BY r.reservation_time;

-- 5. 예약은 있는데 메일이 삭제된 경우 (스케줄러에서 warn 나는 경우)
SELECT r.id, r.mail_id, r.reservation_time
FROM mail_reservation r
LEFT JOIN mail m ON m.id = r.mail_id
WHERE m.id IS NULL;
