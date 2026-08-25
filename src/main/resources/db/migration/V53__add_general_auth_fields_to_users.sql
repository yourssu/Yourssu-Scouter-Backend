-- 일반 이메일/비밀번호 로그인(#405)을 추가하기 위해 users 테이블에 인증 방식 구분 컬럼과
-- 비밀번호 컬럼을 추가한다. 기존 OAuth 유저는 auth_type='OAUTH2'로 채워 이전 데이터를 보존하고,
-- oauth_id/oauth2_type/token 관련 컬럼은 GENERAL 유저에게는 값이 없을 수 있으므로 nullable로 완화한다.
--
-- email 유니크 제약은 기존 데이터에 따라 실패할 수 있어 V54로 분리했다. 한 파일에 두면
-- 제약 추가에서 실패했을 때 (MySQL DDL은 롤백되지 않으므로) 컬럼만 추가된 채로 V53이
-- failed 상태로 남아 flywayRepair가 필요해진다.
ALTER TABLE users
    ADD COLUMN auth_type VARCHAR(20) NOT NULL DEFAULT 'OAUTH2' AFTER profile_image_url,
    ADD COLUMN password VARCHAR(255) NULL AFTER oauth2_type,
    MODIFY COLUMN oauth_id VARCHAR(255) NULL,
    MODIFY COLUMN oauth2_type VARCHAR(20) NULL,
    MODIFY COLUMN token_prefix VARCHAR(255) NULL,
    MODIFY COLUMN access_token VARCHAR(511) NULL,
    MODIFY COLUMN refresh_token VARCHAR(255) NULL,
    MODIFY COLUMN access_token_expiration_date_time DATETIME(6) NULL;

-- auth_type의 DEFAULT는 기존 row 백필용이다. 신규 row는 항상 애플리케이션이 값을 채우므로
-- 백필이 끝난 뒤에는 DEFAULT를 제거해 auth_type 누락이 조용히 OAUTH2로 저장되지 않게 한다.
ALTER TABLE users
    ALTER COLUMN auth_type DROP DEFAULT;
