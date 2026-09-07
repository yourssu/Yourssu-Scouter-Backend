-- 프론트와의 계약 변경으로, 같은 파트 안에서 동일한 시작 시간의 면접 스케줄과 시간이 겹치는
-- 면접 스케줄을 더 이상 막지 않는다. 이에 따라 interview_schedule의 (part_id, start_time)
-- 유니크 제약을 제거한다.
--
-- 이 테이블은 V1 baseline 이전에 만들어져 레포에 생성 DDL이 없고, 운영 DB의 인덱스 이름이
-- 엔티티 선언(unique_interview_schedule)과 다를 수 있다. 또 ddl-auto: validate는 유니크
-- 제약을 검사하지 않으므로, 드롭에 실패해도 조용히 넘어가면 제약이 그대로 남는다.
-- 그래서 이름이 아니라 컬럼 구성으로 인덱스를 찾고(컬럼 순서에도 의존하지 않는다),
-- 찾지 못하면 존재하지 않는 테이블을 조회해 마이그레이션을 실패시킨다.
-- 배포 순서상 dev가 prod보다 먼저이므로 문제가 있으면 dev에서 먼저 드러난다.
-- (CREATE ROUTINE 권한이 필요한 프로시저 대신 프리페어드 스테이트먼트를 쓴다.)
SET @index_name := (
    SELECT s.index_name
    FROM information_schema.statistics s
    WHERE s.table_schema = DATABASE()
      AND s.table_name = 'interview_schedule'
      AND s.non_unique = 0
      AND s.index_name <> 'PRIMARY'
    GROUP BY s.index_name
    HAVING COUNT(*) = 2
       AND SUM(s.column_name IN ('part_id', 'start_time')) = 2
    LIMIT 1
);

SET @sql := IF(
    @index_name IS NULL,
    'SELECT * FROM `V56_ERROR_interview_schedule_part_id_start_time_unique_index_not_found`',
    CONCAT('ALTER TABLE interview_schedule DROP INDEX `', @index_name, '`')
);

PREPARE drop_unique_index FROM @sql;
EXECUTE drop_unique_index;
DEALLOCATE PREPARE drop_unique_index;
