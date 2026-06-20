-- APPLICANT 타입 변수 중 attribute_key 가 누락된 레코드를 applicant.name 으로 채움
-- V16 마이그레이션에서 attribute_key 컬럼 추가 시 기존 데이터가 채워지지 않아 발생한 문제 수정
UPDATE mail_template_variable
SET attribute_key = 'applicant.name'
WHERE variable_type = 'APPLICANT'
  AND attribute_key IS NULL;
