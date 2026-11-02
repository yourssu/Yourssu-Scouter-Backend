#!/usr/bin/env bash
# Flyway 마이그레이션 버전 충돌을 머지 전에 잡는다.
#
# 검사 항목
#   1. 파일명이 Flyway 규칙(V<버전>__<설명>.sql)을 따르는지
#   2. 같은 버전 번호를 가진 파일이 둘 이상 있는지
#   3. 이 브랜치가 새로 추가한 마이그레이션의 버전이 base 브랜치의 최대 버전보다 큰지
#
# 2번은 Flyway가 파일을 resolve하는 단계의 문제라서 DB 없이 검사된다.
#
# 사용법
#   ./scripts/check-migration-versions.sh            # 1, 2번만
#   ./scripts/check-migration-versions.sh develop    # 1, 2, 3번
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
	# 숫자와 구분자만 허용한다.
	case "$raw" in
	'' | *[!0-9._]*) return 1 ;;
	esac
	printf '%s' "$(printf '%s' "$raw" | tr '_' '.')"
}

# 통과했을 때만 요약을 남기고 종료한다.
finish() {
	if [ "$failed" -eq 0 ]; then
		echo "마이그레이션 버전 검사 통과 (${total:-0}개 파일)."
	fi
	exit "$failed"
}

# 버전 문자열 두 개 중 큰 쪽을 출력한다.
max_version() {
	printf '%s\n%s\n' "$1" "$2" | sort -V | tail -n 1
}

cd "$(dirname "$0")/.."

if [ ! -d "$MIGRATION_DIR" ]; then
	report "마이그레이션 디렉터리가 없습니다: $MIGRATION_DIR"
	exit 1
fi

pairs="$(mktemp)"
trap 'rm -f "$pairs"' EXIT

# ---------------------------------------------------------------- 1. 파일명 형식

while IFS= read -r path; do
	name="${path##*/}"

	# R__foo.sql 은 반복 실행 마이그레이션이라 버전이 없다.
	case "$name" in
	R__*.sql) continue ;;
	esac

	if version="$(version_of "$name")"; then
		printf '%s\t%s\n' "$version" "$path" >>"$pairs"
	else
		report "$path 의 파일명이 Flyway 규칙에 맞지 않습니다. V<버전>__<설명>.sql 형식이어야 합니다."
	fi
done < <(find "$MIGRATION_DIR" -maxdepth 1 -type f -name '*.sql' | sort)

total="$(wc -l <"$pairs" | tr -d ' ')"
if [ "$total" -eq 0 ]; then
	echo "검사할 마이그레이션 파일이 없습니다."
	exit "$failed"
fi

# ------------------------------------------------------------------ 2. 버전 중복

while IFS= read -r version; do
	[ -n "$version" ] || continue
	owners="$(awk -F'\t' -v v="$version" '$1 == v { printf "%s%s", sep, $2; sep = ", " }' "$pairs")"
	report "버전 $version 이 중복됩니다: $owners . 한쪽의 버전 번호를 올려주세요. 배포 시 Flyway가 'Found more than one migration with version $version' 으로 실패합니다."
done < <(cut -f1 "$pairs" | sort | uniq -d)

# --------------------------------------------------- 3. base 브랜치 최대 버전 비교

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

base_max=""
while IFS= read -r path; do
	name="${path##*/}"
	case "$name" in
	R__*.sql) continue ;;
	esac
	version="$(version_of "$name")" || continue
	if [ -z "$base_max" ]; then
		base_max="$version"
	else
		base_max="$(max_version "$base_max" "$version")"
	fi
done < <(git ls-tree -r --name-only "$base_sha" -- "$MIGRATION_DIR")

if [ -z "$base_max" ]; then
	echo "base 브랜치에 마이그레이션이 없어 순서 검사를 건너뜁니다."
	finish
fi

echo "base($BASE_REF) 최대 버전: $base_max"

while IFS= read -r path; do
	name="${path##*/}"
	case "$name" in
	R__*.sql) continue ;;
	esac
	version="$(version_of "$name")" || continue
	if [ "$(max_version "$version" "$base_max")" = "$base_max" ] && [ "$version" != "$base_max" ]; then
		report "$path 의 버전 $version 이 base($BASE_REF) 최대 버전 $base_max 보다 작습니다. base에 이미 적용된 마이그레이션보다 앞 번호라서 배포 시 Flyway가 순서 검증으로 실패합니다. 버전 번호를 $base_max 보다 크게 올려주세요."
	fi
done < <(git diff --diff-filter=AR --name-only "$base_sha...HEAD" -- "$MIGRATION_DIR")

finish
