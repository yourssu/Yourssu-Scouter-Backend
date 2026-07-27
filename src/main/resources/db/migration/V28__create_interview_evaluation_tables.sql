-- 1. Interview Rubric (루브릭 헤더)
CREATE TABLE interview_rubric (
                                  id                BIGINT AUTO_INCREMENT PRIMARY KEY,
                                  semester          VARCHAR(255) NOT NULL,
                                  deadline          DATETIME(6)  NOT NULL,
                                  part_id           BIGINT       NOT NULL,
                                  is_locked         BOOLEAN      NOT NULL DEFAULT FALSE,

                                  CONSTRAINT fk_interview_rubric_part FOREIGN KEY (part_id) REFERENCES part (id),
                                  CONSTRAINT uk_interview_rubric_part_semester UNIQUE (part_id, semester)
);

-- 2. Interview Rubric Item (루브릭 항목)
CREATE TABLE interview_evaluation_item (
                                       id                      BIGINT AUTO_INCREMENT PRIMARY KEY,
                                       interview_rubric_id     BIGINT       NOT NULL,
                                       keyword                 VARCHAR(255) NOT NULL,
                                       rubric_type             VARCHAR(50)  NOT NULL,
                                       max_score               INT          NOT NULL,

                                       CONSTRAINT fk_interview_rubric_item_rubric FOREIGN KEY (interview_rubric_id) REFERENCES interview_rubric (id) ON DELETE CASCADE
);

-- 3. Interview Evaluation (평가 결과)
CREATE TABLE interview_evaluation (
                                      id                       BIGINT AUTO_INCREMENT PRIMARY KEY,
                                      applicant_id             BIGINT      NOT NULL,
                                      interview_evaluation_item_id BIGINT  NOT NULL,
                                      member_id                BIGINT      NOT NULL,
                                      score                    INT         NOT NULL,

                                      CONSTRAINT fk_interview_evaluation_applicant FOREIGN KEY (applicant_id) REFERENCES applicant (id),
                                      CONSTRAINT fk_interview_evaluation_item FOREIGN KEY (interview_evaluation_item_id) REFERENCES interview_evaluation_item (id)
);

-- 4. Final Evaluation (최종 평가 결과)
CREATE TABLE final_evaluation (
                                  id                  BIGINT AUTO_INCREMENT PRIMARY KEY,
                                  member_id           BIGINT      NOT NULL,
                                  interview_rubric_id BIGINT      NOT NULL,
                                  applicant_id        BIGINT      NOT NULL,
                                  overall_comment     TEXT        NULL,
                                  interview_result    VARCHAR(30) NOT NULL DEFAULT 'PENDING',
                                  score               INT         NOT NULL,
                                  submit              BOOLEAN     NOT NULL DEFAULT FALSE,
                                  submitted_at        DATETIME(6) NULL,

                                  CONSTRAINT fk_final_evaluation_member FOREIGN KEY (member_id) REFERENCES member (id),
                                  CONSTRAINT fk_final_evaluation_rubric FOREIGN KEY (interview_rubric_id) REFERENCES interview_rubric (id),
                                  CONSTRAINT fk_final_evaluation_applicant FOREIGN KEY (applicant_id) REFERENCES applicant (id)
);

