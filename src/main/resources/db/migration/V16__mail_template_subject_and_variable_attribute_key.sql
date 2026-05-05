-- mail_template: 메일 제목 컬럼 추가 (기존 행은 빈 문자열로 초기화)
ALTER TABLE mail_template
    ADD COLUMN subject VARCHAR(255) NOT NULL DEFAULT '';

-- template_variable: 지원자 속성 키 컬럼 추가 (APPLICANT 타입 변수에만 사용)
ALTER TABLE mail_template_variable
    ADD COLUMN attribute_key VARCHAR(255) NULL;
