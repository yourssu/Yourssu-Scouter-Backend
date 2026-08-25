-- 일반 로그인(#405)은 "이메일당 인증 수단 하나"를 전제로 하므로 users.email에 유니크 제약을 건다.
--
-- 지금까지 users의 유니크 키는 oauth_id였고 email은 아니었다. 한 사람이 서로 다른 OAuth 계정으로
-- 로그인한 이력이 있으면 같은 email을 가진 row가 여러 개 존재할 수 있고, 그 상태에서는 아래 제약
-- 추가가 'Duplicate entry ... for key uk_users_email'로 실패한다. 배포 전에 중복 여부를 확인한다.
--
--   SELECT email, COUNT(*) AS cnt, GROUP_CONCAT(id) AS user_ids
--   FROM users GROUP BY email HAVING cnt > 1;
--
-- applicant_sync_log(V51)와 달리 중복 row를 자동으로 삭제하지 않는다. users.id는 member,
-- user_role, assigned_question, document_evaluation, 메일 예약 등 여러 테이블이 참조하고 있어,
-- 어떤 row를 남기고 참조를 어디로 옮길지는 데이터를 보고 판단해야 한다. 중복이 있다면 참조를
-- 남길 user로 옮긴 뒤 나머지를 지우고 배포한다.
ALTER TABLE users
    ADD CONSTRAINT uk_users_email UNIQUE (email);
