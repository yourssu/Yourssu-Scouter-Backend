-- 일반 이메일/비밀번호 로그인(#405)을 추가하기 위해 users 테이블에 인증 방식 구분 컬럼과
-- 비밀번호 컬럼을 추가한다. 기존 OAuth 유저는 auth_type='OAUTH2'로 채워 이전 데이터를 보존하고,
-- oauth_id/oauth2_type/token 관련 컬럼은 GENERAL 유저에게는 값이 없을 수 있으므로 nullable로 완화한다.
ALTER TABLE users
    ADD COLUMN auth_type VARCHAR(20) NOT NULL DEFAULT 'OAUTH2' AFTER profile_image_url,
    ADD COLUMN password VARCHAR(255) NULL AFTER oauth2_type,
    MODIFY COLUMN oauth_id VARCHAR(255) NULL,
    MODIFY COLUMN oauth2_type VARCHAR(20) NULL,
    MODIFY COLUMN token_prefix VARCHAR(255) NULL,
    MODIFY COLUMN access_token VARCHAR(511) NULL,
    MODIFY COLUMN refresh_token VARCHAR(255) NULL,
    MODIFY COLUMN access_token_expiration_date_time DATETIME(6) NULL;

ALTER TABLE users
    ADD CONSTRAINT uk_users_email UNIQUE (email);
