CREATE TABLE fixed_question (
    id         BIGINT AUTO_INCREMENT PRIMARY KEY,
    category   VARCHAR(20) NOT NULL,
    content    TEXT        NOT NULL,
    sort_order INT         NOT NULL
);

INSERT INTO fixed_question (category, content, sort_order) VALUES
    ('REQUIRED', '자기소개를 해주세요.', 1),
    ('REQUIRED', '지원 동기를 말씀해주세요.', 2),
    ('CULTURE_FIT', '유어슈에서 가장 기대되는 활동은 무엇인가요?', 1),
    ('CULTURE_FIT', '팀 활동 중 갈등을 해결했던 경험이 있나요?', 2),
    ('CLOSING', '마지막으로 하고 싶은 말이 있나요?', 1);
