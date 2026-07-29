CREATE TABLE interview_memo (
    id                         BIGINT AUTO_INCREMENT PRIMARY KEY,
    questionnaire_question_id  BIGINT NOT NULL,
    memo                       TEXT   NOT NULL,
    CONSTRAINT uq_interview_memo_questionnaire_question
        UNIQUE (questionnaire_question_id),
    CONSTRAINT fk_interview_memo_questionnaire_question
        FOREIGN KEY (questionnaire_question_id) REFERENCES questionnaire_question (id) ON DELETE CASCADE
);
