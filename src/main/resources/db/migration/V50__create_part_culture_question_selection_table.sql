CREATE TABLE part_culture_question_selection (
    part_id BIGINT NOT NULL,
    semester_id BIGINT NOT NULL,
    question_id BIGINT NOT NULL,
    PRIMARY KEY (part_id, semester_id, question_id),
    CONSTRAINT fk_part_culture_question_selection_part
        FOREIGN KEY (part_id) REFERENCES part (id),
    CONSTRAINT fk_part_culture_question_selection_semester
        FOREIGN KEY (semester_id) REFERENCES semester (id),
    CONSTRAINT fk_part_culture_question_selection_question
        FOREIGN KEY (question_id) REFERENCES question (id) ON DELETE CASCADE
);
