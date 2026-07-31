CREATE TABLE questionnaire_question_requirement (
    id                             BIGINT AUTO_INCREMENT PRIMARY KEY,
    questionnaire_question_id     BIGINT NOT NULL,
    part_interview_requirement_id  BIGINT NOT NULL,
    CONSTRAINT fk_qqr_questionnaire_question 
        FOREIGN KEY (questionnaire_question_id) REFERENCES questionnaire_question (id) ON DELETE CASCADE,
    CONSTRAINT fk_qqr_part_interview_requirement 
        FOREIGN KEY (part_interview_requirement_id) REFERENCES part_interview_requirement (id) ON DELETE CASCADE,
    CONSTRAINT uq_qqr_question_requirement 
        UNIQUE (questionnaire_question_id, part_interview_requirement_id)
);
