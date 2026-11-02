#!/usr/bin/env bash
# Flyway 마이그레이션 버전 충돌을 머지 전에 잡는다.
#
# 사용법
#   ./scripts/check-migration-versions.sh            # 파일명 형식 + 버전 중복
#   ./scripts/check-migration-versions.sh develop    # 위에 더해 base 최대 버전과 비교
set -euo pipefail

MIGRATION_DIR="src/main/resources/db/migration"
BASE_REF="${1:-${BASE_REF:-}}"

failed=0

report() {
  if [ -n "${GITHUB_ACTIONS:-}" ]; then
    echo "::error::$1"
  fi
  echo "ERROR: $1" >&2
  failed=1
}

# V57__foo.sql -> 57, V2026_11_02__foo.sql -> 2026.11.02
# Flyway는 버전의 밑줄을 점과 같은 구분자로 취급한다.
version_of() {
  local name="${1##*/}"
  case "$name" in
  V*__*.sql) ;;
  *) return 1 ;;
  esac
  local raw="${name#V}"
  raw="${raw%%__*}"
  case "$raw" in
  '' | *[!0-9._]*) return 1 ;;
  esac
  printf '%s' "${raw//_/.}"
}

# 경로를 stdin 으로 받아 "경로<TAB>버전" 으로 바꾼다.
# R__ 은 반복 실행 마이그레이션이라 버전이 없으므로 걸러낸다.
# 형식이 틀린 이름은 버전 자리를 비운 채 넘겨서 호출자가 보고하게 한다.
# 버전을 뒤에 두는 이유는 IFS 가 탭이면 read 가 선행 탭을 공백으로 보고 지우기 때문이다.
version_pairs() {
  local path name
  while IFS= read -r path; do
    name="${path##*/}"
    case "$name" in
    R__*.sql) continue ;;
    esac
    printf '%s\t%s\n' "$path" "$(version_of "$name" || true)"
  done
}

finish() {
  if [ "$failed" -eq 0 ]; then
    echo "마이그레이션 버전 검사 통과 (${total:-0}개 파일)."
  fi
  exit "$failed"
}

max_version() {
  printf '%s\n%s\n' "$1" "$2" | sort -V | tail -n 1
}

cd "$(dirname "$0")/.."

if [ ! -d "$MIGRATION_DIR" ]; then
  report "마이그레이션 디렉터리가 없습니다: $MIGRATION_DIR"
  exit 1
fi

pairs="$(mktemp)"
added="$(mktemp)"
trap 'rm -f "$pairs" "$added"' EXIT

find "$MIGRATION_DIR" -type f -name '*.sql' | sort | version_pairs >"$pairs"
total="$(cut -f2 "$pairs" | grep -c . || true)"

while IFS=$'\t' read -r path version; do
  if [ -z "$version" ]; then
    report "$path 의 파일명이 Flyway 규칙에 맞지 않습니다. V<버전>__<설명>.sql 형식이어야 합니다."
  fi
done <"$pairs"

while IFS= read -r version; do
  owners="$(awk -F'\t' -v v="$version" '$2 == v { printf "%s%s", sep, $1; sep = ", " }' "$pairs")"
  report "버전 $version 이 중복됩니다: $owners . 한쪽의 버전 번호를 올려주세요. 배포 시 Flyway가 'Found more than one migration with version $version' 으로 실패합니다."
done < <(cut -f2 "$pairs" | grep . | sort | uniq -d)

if [ -z "$BASE_REF" ]; then
  echo "base ref가 지정되지 않아 순서 검사를 건너뜁니다."
  finish
fi

base_sha=""
for candidate in "origin/$BASE_REF" "$BASE_REF"; do
  if base_sha="$(git rev-parse --verify --quiet "${candidate}^{commit}")"; then
    break
  fi
done

if [ -z "$base_sha" ]; then
  report "base ref를 찾을 수 없습니다: $BASE_REF . 전체 히스토리를 받아왔는지(fetch-depth: 0) 확인하세요."
  exit "$failed"
fi

# grep 이 빈 결과를 내면 pipefail 때문에 대입이 실패한다. base 에 마이그레이션이
# 없는 경우는 아래에서 따로 다루므로 여기서 죽지 않게 한다.
base_max="$(git ls-tree -r --name-only "$base_sha" -- "$MIGRATION_DIR" | version_pairs | cut -f2 | grep . | sort -V | tail -n 1 || true)"

if [ -z "$base_max" ]; then
  echo "base 브랜치에 마이그레이션이 없어 순서 검사를 건너뜁니다."
  finish
fi

echo "base($BASE_REF) 최대 버전: $base_max"

# 프로세스 치환 안에서 돌리면 git 이 실패해도 검사가 조용히 통과한다.
git diff --diff-filter=AR --name-only "$base_sha...HEAD" -- "$MIGRATION_DIR" >"$added"

while IFS=$'\t' read -r path version; do
  [ -n "$version" ] || continue
  if [ "$(max_version "$version" "$base_max")" = "$base_max" ]; then
    report "$path 의 버전 $version 이 base($BASE_REF) 최대 버전 $base_max 보다 크지 않습니다. base에 이미 적용된 번호이거나 그보다 앞 번호라서 배포 시 Flyway가 실패합니다. 버전 번호를 $base_max 보다 크게 올려주세요."
  fi
done < <(version_pairs <"$added")

finish
