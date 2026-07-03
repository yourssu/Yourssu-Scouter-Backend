CREATE TABLE applicant_answer (
    id           BIGINT AUTO_INCREMENT PRIMARY KEY,
    applicant_id BIGINT NOT NULL,
    question     TEXT   NOT NULL,
    answer       TEXT   NOT NULL,
    CONSTRAINT fk_applicant_answer_applicant
        FOREIGN KEY (applicant_id) REFERENCES applicant (id) ON DELETE CASCADE
);
