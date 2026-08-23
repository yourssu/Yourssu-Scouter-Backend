-- formId는 폼 하나(=팀+학기 하나)를 가리켜야 하는데, DB 제약이 없어 같은 formId가
-- 여러 학기/파트에 중복 등록될 수 있었다(웹훅이 formId 단건 조회에 의존하는데,
-- 중복 등록 시 서버 에러로 이어짐). 재발 방지를 위해 유니크 제약을 추가한다.
ALTER TABLE applicant_sync_mapping
    ADD CONSTRAINT uk_applicant_sync_mapping_form_id
        UNIQUE (form_id);
