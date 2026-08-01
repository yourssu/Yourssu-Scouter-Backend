ALTER TABLE questionnaire_question
    DROP FOREIGN KEY fk_questionnaire_question_questionnaire;

ALTER TABLE questionnaire_question
    DROP FOREIGN KEY fk_questionnaire_question_source_question;

ALTER TABLE questionnaire_question_requirement
    DROP FOREIGN KEY fk_qqr_questionnaire_question;

ALTER TABLE questionnaire_question_requirement
    DROP FOREIGN KEY fk_qqr_part_interview_requirement;

ALTER TABLE interview_memo
    DROP FOREIGN KEY fk_interview_memo_questionnaire_question;

ALTER TABLE interview_memo
    DROP INDEX uq_interview_memo_questionnaire_question;

RENAME TABLE fixed_question TO question;

ALTER TABLE question
    ADD COLUMN part_id BIGINT NULL AFTER id;

UPDATE question
SET category = CASE category
    WHEN 'CULTURE_FIT' THEN 'CULTURE'
    ELSE 'GLOBAL'
END;

INSERT INTO question (part_id, category, content, sort_order) VALUES
    (NULL, 'CULTURE', '유어슈의 핵심 가치 중 본인과 가장 맞는 것은 무엇인가요?', 3),
    (NULL, 'CULTURE', '새로운 환경에서 빠르게 적응했던 경험이 있나요?', 4);

RENAME TABLE questionnaire_question TO assigned_question;

ALTER TABLE assigned_question
    CHANGE COLUMN questionnaire_id applicant_id BIGINT NOT NULL,
    CHANGE COLUMN question_group category VARCHAR(20) NOT NULL,
    ADD COLUMN assigned_interviewer_user_id BIGINT NULL AFTER id,
    ADD COLUMN is_selected BOOLEAN NULL AFTER sort_order;

UPDATE assigned_question aq
JOIN questionnaire q ON aq.applicant_id = q.applicant_id
SET aq.assigned_interviewer_user_id = q.assigned_interviewer_user_id;

UPDATE assigned_question
SET category = CASE category
    WHEN 'CULTURE_FIT' THEN 'CULTURE'
    WHEN 'REQUIRED' THEN 'GLOBAL'
    WHEN 'CLOSING' THEN 'GLOBAL'
    WHEN 'TEAM_FIT' THEN 'PERSONAL'
    WHEN 'JOB_FIT' THEN 'PERSONAL'
    ELSE category
END;

ALTER TABLE assigned_question
    MODIFY COLUMN assigned_interviewer_user_id BIGINT NOT NULL;

RENAME TABLE questionnaire_question_requirement TO assigned_question_requirement;

ALTER TABLE assigned_question_requirement
    CHANGE COLUMN questionnaire_question_id assigned_question_id BIGINT NOT NULL;

ALTER TABLE assigned_question
    ADD CONSTRAINT fk_assigned_question_applicant
        FOREIGN KEY (applicant_id) REFERENCES applicant (id) ON DELETE CASCADE,
    ADD CONSTRAINT fk_assigned_question_assigned_interviewer
        FOREIGN KEY (assigned_interviewer_user_id) REFERENCES users (id),
    ADD CONSTRAINT fk_assigned_question_source_question
        FOREIGN KEY (source_question_id) REFERENCES question (id);

ALTER TABLE interview_memo
    CHANGE COLUMN questionnaire_question_id assigned_question_id BIGINT NOT NULL,
    ADD CONSTRAINT uq_interview_memo_assigned_question UNIQUE (assigned_question_id),
    ADD CONSTRAINT fk_interview_memo_assigned_question
        FOREIGN KEY (assigned_question_id) REFERENCES assigned_question (id) ON DELETE CASCADE;

ALTER TABLE assigned_question_requirement
    ADD CONSTRAINT fk_assigned_question_requirement_assigned_question
        FOREIGN KEY (assigned_question_id) REFERENCES assigned_question (id) ON DELETE CASCADE,
    ADD CONSTRAINT fk_assigned_question_requirement_part_interview_requirement
        FOREIGN KEY (part_interview_requirement_id) REFERENCES part_interview_requirement (id) ON DELETE CASCADE;

DROP TABLE questionnaire;
