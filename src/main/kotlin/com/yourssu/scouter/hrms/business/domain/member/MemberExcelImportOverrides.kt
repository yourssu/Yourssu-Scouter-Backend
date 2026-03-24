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
    /** 키: 비액티브 활동학기 셀 raw(11열), 값: 활동 학기 수(예: 3). null이면 명시적으로 비움. */
    val inactiveActivityTotalSemestersOverrides: Map<String, Int?> = emptyMap(),
    /** 키: 비액티브 활동학기 셀 raw(11열), 값: 비액티브 학기 수(예: 2). null이면 명시적으로 비움. */
    val inactiveInactiveTotalSemestersOverrides: Map<String, Int?> = emptyMap(),
) {
    companion object {
        val EMPTY = MemberExcelImportOverrides()
    }
}
