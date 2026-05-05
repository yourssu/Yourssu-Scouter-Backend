INSERT INTO department (college_id, name)
SELECT c.id, 'AI소프트웨어학부'
FROM college c
WHERE c.name = 'IT대학'
  AND NOT EXISTS (
      SELECT 1
      FROM department d
      WHERE d.name = 'AI소프트웨어학부'
  );

INSERT INTO department (college_id, name)
SELECT c.id, '디지털미디어학과'
FROM college c
WHERE c.name = 'IT대학'
  AND NOT EXISTS (
      SELECT 1
      FROM department d
      WHERE d.name = '디지털미디어학과'
  );
