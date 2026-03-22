# Yourssu-Scouter-Backend

## 로컬 H2에 dev/prod DB 데이터 넣기

기본 `local` 프로필은 빈 H2에서 시작합니다. MySQL에 있는 데이터를 로컬에서 쓰려면:

1. `.env.local`에 `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`를 넣은 뒤  
   `set -a && source .env.local && set +a` 후 `./scripts/mysql-dump-for-local-h2.sh` 실행  
   → `.local/h2-init.sql` 생성 (`.local/`은 gitignore).
2. 앱 기동 시  
   `SCOUTER_LOCAL_H2_INIT_SCRIPT=.local/h2-init.sql ./gradlew bootRun`  
   또는 `application-local.yml`의 `scouter.local-h2-data.init-script`에 동일 경로 설정.

스크립트는 JPA가 만든 테이블에 맞추기 위해 **`--no-create-info`(데이터만)** 덤프를 사용합니다. MySQL 전용 문법이 남으면 H2에서 실패할 수 있어, 오류 메시지를 보고 SQL을 일부 수동 수정해야 할 수 있습니다. 덤프에 시드 데이터와 겹치는 PK가 있으면 `CommandLineRunner` 초기화 이후에 스크립트가 실행되므로 **INSERT 충돌**이 날 수 있습니다. 그 경우 덤프에서 해당 테이블을 빼거나 SQL을 조정하세요.