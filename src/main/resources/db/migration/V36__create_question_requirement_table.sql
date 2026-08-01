CREATE TABLE question_requirement (
    question_id BIGINT NOT NULL,
    part_interview_requirement_id BIGINT NOT NULL,
    PRIMARY KEY (question_id, part_interview_requirement_id),
    CONSTRAINT fk_question_requirement_question
        FOREIGN KEY (question_id) REFERENCES question (id) ON DELETE CASCADE,
    CONSTRAINT fk_question_requirement_part_interview_requirement
        FOREIGN KEY (part_interview_requirement_id) REFERENCES part_interview_requirement (id) ON DELETE CASCADE
);
