-- 구글 프로필 이미지 URL이 기존 컬럼 길이(VARCHAR(255) 기본값)를 넘는 계정이 있어,
-- 신규 유저 첫 로그인 시 INSERT가 'Data too long for column profile_image_url'로 실패했다.
-- 재발을 막기 위해 컬럼 길이를 넉넉하게 늘린다.
ALTER TABLE users
    MODIFY COLUMN profile_image_url VARCHAR(512) NOT NULL;
