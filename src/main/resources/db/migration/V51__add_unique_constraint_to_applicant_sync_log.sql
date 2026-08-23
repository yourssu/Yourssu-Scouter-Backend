-- 과거 read-then-write 방식의 dedup 레이스로 인해 이미 중복 저장된 row가 있을 수 있어
-- 유니크 제약을 걸기 전에 정리한다. 그룹별로 가장 먼저 생성된(id가 가장 작은) row만 남긴다.
DELETE t1 FROM applicant_sync_log t1
INNER JOIN applicant_sync_log t2
    ON t1.application_semester_id = t2.application_semester_id
    AND t1.form_id = t2.form_id
    AND t1.response_id = t2.response_id
    AND t1.id > t2.id;

ALTER TABLE applicant_sync_log
    ADD CONSTRAINT uk_applicant_sync_log_semester_form_response
        UNIQUE (application_semester_id, form_id, response_id);
