-- 지원자별 면접 질문지의 "파트 잠금" 판정이 지원자/평가 테이블 조회에서 interview_rubric.is_locked
-- 컬럼으로 바뀌었다. is_locked 를 세우는 코드(평가 저장 시 rubric.lock())는 나중에 추가되었기 때문에,
-- 그 이전에 저장된 면접 평가가 있는 루브릭은 평가가 존재하는데도 is_locked = false 로 남아 있을 수 있다.
-- 그 상태면 잠금이 조용히 열려, 이미 제출된 평가가 근거한 파트 공유 질문이 수정될 수 있다.
-- 평가가 하나라도 달려 있는 루브릭을 잠긴 상태로 맞춰 준다.
UPDATE interview_rubric r
SET r.is_locked = TRUE
WHERE r.is_locked = FALSE
  AND EXISTS (
    SELECT 1
    FROM interview_evaluation e
             JOIN interview_evaluation_item i ON i.id = e.interview_evaluation_item_id
    WHERE i.interview_rubric_id = r.id
);
