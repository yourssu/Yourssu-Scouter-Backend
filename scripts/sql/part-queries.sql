-- 파트 조회용 SQL (part / division 테이블)

-- 1. 전체 파트 목록 조회 (division 정렬 우선, 그 다음 파트 정렬)
SELECT
    p.id AS part_id,
    p.name AS part_name,
    p.sort_priority AS part_sort_priority,
    d.id AS division_id,
    d.name AS division_name,
    d.sort_priority AS division_sort_priority
FROM part p
INNER JOIN division d ON d.id = p.division_id
ORDER BY d.sort_priority ASC, p.sort_priority ASC;

-- 2. 특정 division에 속한 파트만 조회
SELECT
    p.id AS part_id,
    p.name AS part_name,
    p.sort_priority AS part_sort_priority,
    d.id AS division_id,
    d.name AS division_name
FROM part p
INNER JOIN division d ON d.id = p.division_id
WHERE d.id = ? -- division_id 입력
ORDER BY p.sort_priority ASC;

-- 3. 파트 개수만 확인
SELECT COUNT(*) AS part_count FROM part;

-- 4. division별 파트 개수
SELECT
    d.id AS division_id,
    d.name AS division_name,
    COUNT(p.id) AS part_count
FROM division d
LEFT JOIN part p ON p.division_id = d.id
GROUP BY d.id, d.name
ORDER BY d.sort_priority ASC;

-- 5. 특정 파트 상세 정보 조회
SELECT
    p.id AS part_id,
    p.name AS part_name,
    p.sort_priority AS part_sort_priority,
    d.id AS division_id,
    d.name AS division_name,
    d.sort_priority AS division_sort_priority
FROM part p
INNER JOIN division d ON d.id = p.division_id
WHERE p.id = ?; -- part_id 입력

-- 6. [DEV 서버용] finance → Finance, legal → Legal 대소문자 통일 (초기화 스크립트가 스킵되어 수동 업데이트 필요)
-- 주의: 실행 전 백업 권장
UPDATE part SET name = 'Finance' WHERE LOWER(name) = 'finance';
UPDATE part SET name = 'Legal' WHERE LOWER(name) = 'legal';
