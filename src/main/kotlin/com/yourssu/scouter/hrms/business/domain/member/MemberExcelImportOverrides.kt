package com.yourssu.scouter.hrms.business.domain.member

/**
 * 인포시트 업로드 시 사용자가 웹에서 보정한 매핑을 한 번에 전달한다.
 * [EMPTY]는 파서 기본 동작(시트 값만 사용)과 동일하다.
 */
data class MemberExcelImportOverrides(
    val departmentOverrides: Map<String, String> = emptyMap(),
    val completionSemesterOverrides: Map<String, String> = emptyMap(),
    /** 키: `시트표시명|||날짜셀raw`(예: `탈퇴|||2025.9.1`) 또는 raw 단독. 값: yyyy-MM-dd. */
    val joinDateOverrides: Map<String, String> = emptyMap(),
    val expectedReturnOverrides: Map<String, String> = emptyMap(),
    val inactiveActivitySemesterOverrides: Map<String, String> = emptyMap(),
) {
    companion object {
        val EMPTY = MemberExcelImportOverrides()
    }
}
