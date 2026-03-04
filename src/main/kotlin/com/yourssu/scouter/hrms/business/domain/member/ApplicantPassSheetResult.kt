package com.yourssu.scouter.hrms.business.domain.member

/**
 * 지원자 합격시트/인포시트 업로드 결과.
 * - Success: 처리 완료 (에러 없음)
 * - MappingRequired: 시트에 DB에 없는 학과명이 있어, [오타난 raw 학부이름] -> [실제 학부] 매핑 입력 필요
 *   - unknownBySheet: 시트별 미등록 학과명 (키: "액티브"|"비액티브"|"졸업"|"탈퇴"|"지원자 합격시트")
 * - Errors: 파싱/저장 중 오류 발생
 */
sealed class ApplicantPassSheetResult {
    data object Success : ApplicantPassSheetResult()

    data class MappingRequired(
        /** 시트 라벨 -> 해당 시트에서 발견된 미등록 학과명(raw) 목록. 동일 시트 내 중복 제거됨. */
        val unknownBySheet: Map<String, List<String>>,
    ) : ApplicantPassSheetResult() {
        /** 폼에서 사용할 고유 학과명 목록 (중복 제거, 정렬). */
        fun uniqueUnknownDepartments(): List<String> =
            unknownBySheet.values.flatten().distinct().sorted()
    }

    data class Errors(
        val messages: List<String>,
    ) : ApplicantPassSheetResult()
}
