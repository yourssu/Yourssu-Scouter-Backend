CREATE TABLE questionnaire (
    applicant_id                 BIGINT PRIMARY KEY,
    assigned_interviewer_user_id BIGINT NOT NULL,
    CONSTRAINT fk_questionnaire_applicant
        FOREIGN KEY (applicant_id) REFERENCES applicant (id) ON DELETE CASCADE,
    CONSTRAINT fk_questionnaire_assigned_interviewer
        FOREIGN KEY (assigned_interviewer_user_id) REFERENCES users (id)
);

CREATE TABLE questionnaire_question (
    id                 BIGINT AUTO_INCREMENT PRIMARY KEY,
    questionnaire_id   BIGINT      NOT NULL,
    question_group     VARCHAR(20) NOT NULL,
    source_question_id BIGINT      NULL,
    content            TEXT        NULL,
    sort_order         INT         NOT NULL,
    CONSTRAINT fk_questionnaire_question_questionnaire
        FOREIGN KEY (questionnaire_id) REFERENCES questionnaire (applicant_id) ON DELETE CASCADE,
    CONSTRAINT fk_questionnaire_question_source_question
        FOREIGN KEY (source_question_id) REFERENCES fixed_question (id)
);
