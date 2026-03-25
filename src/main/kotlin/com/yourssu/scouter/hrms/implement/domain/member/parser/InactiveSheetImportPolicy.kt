package com.yourssu.scouter.hrms.implement.domain.member.parser

import com.yourssu.scouter.common.implement.domain.semester.Semester
import com.yourssu.scouter.common.implement.domain.semester.SemesterRepository
import org.springframework.stereotype.Component

/**
 * 비액티브 시트의 예정복귀 문자열을 DB 학기·reason 메모 규칙으로 해석한다.
 */
@Component
class InactiveSheetImportPolicy(
    private val semesterRepository: SemesterRepository,
) {

    fun resolveStoredSemester(str: String?): Semester? {
        if (str.isNullOrBlank()) return null
        return runCatching {
            semesterRepository.find(Semester.of(str.trim()))
        }.getOrNull()
    }

    /**
     * 시트 raw에 사용자 매핑이 있으면 매핑 값을 우선한다.
     * [overrides]에 해당 raw 키가 있고 값이 비어 있으면 null(예정복귀·활동학기 없음)으로 본다.
     */
    fun effectiveExpectedReturnRaw(sheetRaw: String?, overrides: Map<String, String>): String? =
        effectiveSemesterCellRaw(sheetRaw, overrides)

    fun effectiveInactiveActivitySemesterRaw(sheetRaw: String?, overrides: Map<String, String>): String? =
        effectiveSemesterCellRaw(sheetRaw, overrides)

    private fun effectiveSemesterCellRaw(sheetRaw: String?, overrides: Map<String, String>): String? {
        val raw = sheetRaw?.trim()?.takeIf { it.isNotBlank() } ?: return null
        if (overrides.containsKey(raw)) {
            return overrides[raw]?.trim()?.takeIf { it.isNotBlank() }
        }
        return raw
    }

    fun mergeExpectedReturnIntoReason(reason: String?, expectedReturnStr: String?): String? {
        if (expectedReturnStr.isNullOrBlank()) return reason?.takeIf { it.isNotBlank() }
        if (resolveStoredSemester(expectedReturnStr) != null) return reason?.takeIf { it.isNotBlank() }
        val memo = "예정복귀: $expectedReturnStr"
        return if (reason.isNullOrBlank()) memo else "$reason [${memo}]"
    }
}
