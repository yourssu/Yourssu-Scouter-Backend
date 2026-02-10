-- ============================================================
-- Instant 타임존 데이터 보정 SQL
-- ============================================================
-- 배경:
--   hibernate.jdbc.time_zone: UTC 설정 추가 전에는
--   Hibernate가 Instant 값을 JVM/서버 로컬 타임존(KST, UTC+9)으로
--   변환하여 DATETIME 컬럼에 저장했을 수 있음.
--
--   설정 추가 후 Hibernate는 DATETIME 값을 UTC로 해석하므로,
--   기존 KST로 저장된 값은 9시간 앞당겨야 정상 동작함.
--
-- 주의: 실행 전 반드시 아래 확인 쿼리로 데이터 상태를 먼저 점검할 것!
-- ============================================================

-- [1단계] 현재 데이터 확인 (실행만 하고 결과 확인)
SELECT id, mail_id, reservation_time,
       DATE_SUB(reservation_time, INTERVAL 9 HOUR) AS corrected_time
FROM mail_reservation;

SELECT id, available_time,
       DATE_SUB(available_time, INTERVAL 9 HOUR) AS corrected_time
FROM applicant_available_time;

SELECT id, start_time, end_time,
       DATE_SUB(start_time, INTERVAL 9 HOUR) AS corrected_start,
       DATE_SUB(end_time, INTERVAL 9 HOUR) AS corrected_end
FROM schedule;

SELECT id, sync_time,
       DATE_SUB(sync_time, INTERVAL 9 HOUR) AS corrected_time
FROM applicant_sync_log;

SELECT id, sync_time,
       DATE_SUB(sync_time, INTERVAL 9 HOUR) AS corrected_time
FROM member_sync_log;

SELECT id, state_updated_time,
       DATE_SUB(state_updated_time, INTERVAL 9 HOUR) AS corrected_time
FROM member;

-- ============================================================
-- [2단계] 데이터 보정 (확인 후 실행)
-- KST(UTC+9)로 저장된 기존 데이터를 UTC로 변환
-- ============================================================

UPDATE mail_reservation
SET reservation_time = DATE_SUB(reservation_time, INTERVAL 9 HOUR);

UPDATE applicant_available_time
SET available_time = DATE_SUB(available_time, INTERVAL 9 HOUR);

UPDATE schedule
SET start_time = DATE_SUB(start_time, INTERVAL 9 HOUR),
    end_time = DATE_SUB(end_time, INTERVAL 9 HOUR);

UPDATE applicant_sync_log
SET sync_time = DATE_SUB(sync_time, INTERVAL 9 HOUR);

UPDATE member_sync_log
SET sync_time = DATE_SUB(sync_time, INTERVAL 9 HOUR);

UPDATE member
SET state_updated_time = DATE_SUB(state_updated_time, INTERVAL 9 HOUR);
