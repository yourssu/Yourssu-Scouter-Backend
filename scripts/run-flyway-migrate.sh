#!/usr/bin/env bash
# Flyway 마이그레이션 실행 (Spring Boot 기동 시 Flyway가 자동 실행됨)
# .env.local 에 DB_URL, DB_USERNAME, DB_PASSWORD 설정 필요.
# 사용법: ./scripts/run-flyway-migrate.sh
#
# 애플리케이션이 기동되면 Flyway가 먼저 마이그레이션을 적용합니다.
# "Successfully applied" 로그 확인 후 Ctrl+C로 종료하거나, 그대로 서버를 사용하세요.

set -e
cd "$(dirname "$0")/.."

if [ -f .env.local ]; then
  set -a
  source .env.local
  set +a
fi

for var in DB_URL DB_USERNAME DB_PASSWORD; do
  if [ -z "${!var}" ]; then
    echo "환경변수 $var 이 비어 있습니다. .env.local 에 설정하거나 export 해 주세요."
    exit 1
  fi
done

echo "Flyway 마이그레이션 실행 (애플리케이션 기동)..."
./gradlew bootRun --args='--spring.profiles.active=local,local-dev-db' --no-daemon
