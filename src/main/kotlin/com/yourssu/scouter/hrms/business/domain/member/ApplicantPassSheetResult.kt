package com.yourssu.scouter.hrms.business.domain.member

/**
 * 지원자 합격시트/인포시트 업로드 결과.
 * - Success: 처리 완료 (에러 없음)
 * - MappingRequired: 학과·수료 학기·가입일·비액티브 예정복귀 등 웹 매핑 입력 필요
 *   - unknownBySheet: 시트별 미등록 학과명 (키: "액티브"|"비액티브"|"졸업"|"탈퇴"|"지원자 합격시트")
 *   - completionSemesterMappingHints: 수료 시트 11열을 DB 학기로 못 푼 distinct raw마다, 해당 행 멤버 이름·닉네임 목록
 *   - joinDateMappingHints: 가입일 보정이 필요한 (시트, raw)별 이름·닉네임
 *   - expectedReturnMappingHints: 비액티브 예정복귀 raw가 DB 학기로 안 풀릴 때
 *   - inactiveActivitySemesterMappingHints: 비액티브 활동학기 raw가 DB 학기로 안 풀릴 때
 * - Errors: 파싱/저장 중 오류 발생
 */
sealed class ApplicantPassSheetResult {
    data object Success : ApplicantPassSheetResult()

    data class MappingRequired(
        /** 시트 라벨 -> 해당 시트에서 발견된 미등록 학과명(raw) 목록. 동일 시트 내 중복 제거됨. */
        val unknownBySheet: Map<String, List<String>>,
        val completionSemesterMappingHints: List<CompletionSemesterMappingHint> = emptyList(),
        val joinDateMappingHints: List<JoinDateMappingHint> = emptyList(),
        val expectedReturnMappingHints: List<ExpectedReturnMappingHint> = emptyList(),
        val inactiveActivitySemesterMappingHints: List<InactiveActivitySemesterMappingHint> = emptyList(),
    ) : ApplicantPassSheetResult() {
        /** 폼에서 사용할 고유 학과명 목록 (중복 제거, 정렬). */
        fun uniqueUnknownDepartments(): List<String> =
            unknownBySheet.values.flatten().distinct().sorted()
    }

    data class Errors(
        val messages: List<String>,
    ) : ApplicantPassSheetResult()
}
