-- 프론트와의 계약 변경으로, 같은 파트 안에서 동일한 시작 시간의 면접 스케줄과 시간이 겹치는
-- 면접 스케줄을 더 이상 막지 않는다. 이에 따라 interview_schedule의 (part_id, start_time)
-- 유니크 제약을 제거한다.
--
-- 이 테이블은 V1 baseline 이전에 만들어져 레포에 생성 DDL이 없고, 운영 DB의 인덱스 이름이
-- 엔티티 선언(unique_interview_schedule)과 다를 수 있다. 또 ddl-auto: validate는 유니크
-- 제약을 검사하지 않으므로, 이름이 맞지 않아 드롭에 실패해도 조용히 넘어가면 제약이 그대로
-- 남는다. 그래서 이름 대신 컬럼 조합으로 인덱스를 찾고, 찾지 못하면 마이그레이션을 실패시킨다.
DROP PROCEDURE IF EXISTS drop_interview_schedule_unique_index;

DELIMITER //

CREATE PROCEDURE drop_interview_schedule_unique_index()
BEGIN
    DECLARE target_index VARCHAR(64);

    SELECT s.index_name INTO target_index
    FROM information_schema.statistics s
    WHERE s.table_schema = DATABASE()
      AND s.table_name = 'interview_schedule'
      AND s.non_unique = 0
      AND s.index_name <> 'PRIMARY'
    GROUP BY s.index_name
    HAVING GROUP_CONCAT(s.column_name ORDER BY s.seq_in_index) = 'part_id,start_time'
    LIMIT 1;

    IF target_index IS NULL THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'interview_schedule(part_id, start_time) 유니크 인덱스를 찾지 못했습니다';
    END IF;

    SET @sql = CONCAT('ALTER TABLE interview_schedule DROP INDEX `', target_index, '`');
    PREPARE drop_index FROM @sql;
    EXECUTE drop_index;
    DEALLOCATE PREPARE drop_index;
END //

DELIMITER ;

CALL drop_interview_schedule_unique_index();

DROP PROCEDURE drop_interview_schedule_unique_index;
