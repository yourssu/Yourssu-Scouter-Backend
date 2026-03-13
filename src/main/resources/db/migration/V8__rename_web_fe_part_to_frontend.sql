-- Rename part name 'Web FE' to 'Frontend' for consistency with application code and member-parse-mapping-data.yml
UPDATE part
SET name = 'Frontend'
WHERE name = 'Web FE';

