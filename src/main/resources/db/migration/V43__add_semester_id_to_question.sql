-- question 테이블에 semester_id 컬럼 추가
-- CULTURE / PART 질문은 학기별로 관리됩니다.
-- INTRO / OUTRO 질문은 학기에 무관하게 공유되므로 NULL 허용입니다.
ALTER TABLE question
    ADD COLUMN semester_id BIGINT NULL,
    ADD CONSTRAINT fk_question_semester
        FOREIGN KEY (semester_id)
            REFERENCES semester (id) ON DELETE SET NULL;