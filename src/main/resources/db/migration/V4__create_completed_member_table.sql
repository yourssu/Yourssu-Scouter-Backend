-- completed_member 테이블 생성 (graduated_member와 동일 구조)
-- is_advisor_desired는 스키마 통일용, 수료에서는 미사용(false)
CREATE TABLE completed_member (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    member_id BIGINT NOT NULL,
    active_start_semester_id BIGINT NOT NULL,
    active_end_semester_id BIGINT NOT NULL,
    is_advisor_desired BOOLEAN NOT NULL DEFAULT FALSE,
    CONSTRAINT uk_completed_member_member_id UNIQUE (member_id),
    CONSTRAINT fk_completed_member_member FOREIGN KEY (member_id) REFERENCES member (id),
    CONSTRAINT fk_completed_member_active_start_semester FOREIGN KEY (active_start_semester_id) REFERENCES semester (id),
    CONSTRAINT fk_completed_member_active_end_semester FOREIGN KEY (active_end_semester_id) REFERENCES semester (id)
);

-- 기존 graduated_member 중 member.state = 'COMPLETED' 인 행을 completed_member로 이전
INSERT INTO completed_member (member_id, active_start_semester_id, active_end_semester_id, is_advisor_desired)
SELECT gm.member_id, gm.active_start_semester_id, gm.active_end_semester_id, COALESCE(gm.is_advisor_desired, FALSE)
FROM graduated_member gm
INNER JOIN member m ON gm.member_id = m.id
WHERE m.state = 'COMPLETED';

-- 이전한 행을 graduated_member에서 삭제
DELETE gm FROM graduated_member gm
INNER JOIN member m ON gm.member_id = m.id
WHERE m.state = 'COMPLETED';
