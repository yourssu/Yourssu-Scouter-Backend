package com.yourssu.scouter.hrms.business.domain.member

import com.yourssu.scouter.common.implement.domain.department.Department
import com.yourssu.scouter.common.implement.domain.semester.Semester
import com.yourssu.scouter.common.implement.domain.semester.SemesterRepository
import com.yourssu.scouter.hrms.business.support.utils.MemberStateConverter
import com.yourssu.scouter.hrms.implement.domain.member.MemberState
import com.yourssu.scouter.hrms.implement.domain.member.parser.BasicMemberExcelProcessor
import com.yourssu.scouter.hrms.implement.domain.member.parser.ColumnNumberMapping
import com.yourssu.scouter.hrms.implement.domain.member.parser.InactiveSheetImportPolicy
import com.yourssu.scouter.hrms.implement.domain.member.parser.InactiveSheetRowRules
import com.yourssu.scouter.hrms.implement.support.getFlexibleLocalDateSafe
import com.yourssu.scouter.hrms.implement.support.getFormattedStringSafe
import com.yourssu.scouter.hrms.implement.support.isNullOrBlank
import com.yourssu.scouter.hrms.implement.support.isStrikethrough
import org.apache.poi.ss.usermodel.Row
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import org.springframework.stereotype.Component
import java.time.LocalDate

/**
 * 인포시트 업로드 1단계: 학과·수료 학기·가입일·비액티브 활동학기·예정복귀 등 사용자 매핑이 필요한지 수집한다.
 */
@Component
class InfoSheetImportPreflightOrchestrator(
    private val basicMemberExcelProcessor: BasicMemberExcelProcessor,
    private val semesterRepository: SemesterRepository,
    private val inactiveSheetImportPolicy: InactiveSheetImportPolicy,
) {

    data class Result(
        val unknownBySheet: Map<String, List<String>>,
        val completionSemesterMappingHints: List<CompletionSemesterMappingHint>,
        val joinDateMappingHints: List<JoinDateMappingHint>,
        val expectedReturnMappingHints: List<ExpectedReturnMappingHint>,
        val inactiveActivitySemesterMappingHints: List<InactiveActivitySemesterMappingHint>,
    ) {
        fun needsMapping(): Boolean =
            unknownBySheet.isNotEmpty() ||
                completionSemesterMappingHints.isNotEmpty() ||
                joinDateMappingHints.isNotEmpty() ||
                expectedReturnMappingHints.isNotEmpty() ||
                inactiveActivitySemesterMappingHints.isNotEmpty()
    }

    fun run(
        workbook: XSSFWorkbook,
        departments: Map<String, Department>,
        overrides: MemberExcelImportOverrides,
    ): Result {
        val unknownBySheet = collectUnknownDepartments(workbook, departments, overrides.departmentOverrides)
        val completionHints = collectCompletionHints(workbook, overrides.completionSemesterOverrides)
        val joinHints = collectJoinDateHints(workbook, overrides.joinDateOverrides)
        val expectedHints = collectExpectedReturnHints(workbook, overrides.expectedReturnOverrides)
        val inactiveActivityHints =
            collectInactiveActivitySemesterHints(workbook, overrides.inactiveActivitySemesterOverrides)
        return Result(unknownBySheet, completionHints, joinHints, expectedHints, inactiveActivityHints)
    }

    private fun collectUnknownDepartments(
        workbook: XSSFWorkbook,
        departments: Map<String, Department>,
        departmentOverrides: Map<String, String>,
    ): Map<String, List<String>> {
        if (departmentOverrides.isNotEmpty()) return emptyMap()
        val map = mutableMapOf<String, List<String>>()
        for (state: MemberState in MemberState.entries) {
            if (state == MemberState.WITHDRAWN) continue
            val sheet = workbook.getSheet(MemberStateConverter.convertToString(state)) ?: continue
            val list = basicMemberExcelProcessor.collectUnknownDepartments(
                sheet,
                departments,
                ColumnNumberMapping.forState(state),
            )
            if (list.isNotEmpty()) {
                map[sheetDisplayName(state)] = list
            }
        }
        return map
    }

    private fun collectCompletionHints(
        workbook: XSSFWorkbook,
        completionOverrides: Map<String, String>,
    ): List<CompletionSemesterMappingHint> {
        val sheet = workbook.getSheet(MemberStateConverter.convertToString(MemberState.COMPLETED)) ?: return emptyList()
        val base = CompletionSemesterMappingPreflight.collectHints(sheet, ::resolveLabelToStoredSemester)
        return filterCompletionHints(base, completionOverrides)
    }

    private fun filterCompletionHints(
        hints: List<CompletionSemesterMappingHint>,
        completionOverrides: Map<String, String>,
    ): List<CompletionSemesterMappingHint> {
        if (completionOverrides.isEmpty()) return hints
        return hints.filter { h ->
            val key = h.rawKey.trim()
            val mapped = completionOverrides[key]?.trim()
            when {
                mapped.isNullOrBlank() -> true
                resolveLabelToStoredSemester(mapped) == null -> true
                else -> false
            }
        }
    }

    private fun joinScanShouldBreak(state: MemberState, row: Row): Boolean =
        when (state) {
            // 비액티브는 구간 사이 빈 행에서 끊지 않는다(이름 비어 있으면 isNonDataRow로 스킵).
            MemberState.INACTIVE -> false
            else -> row.getCell(0).isNullOrBlank()
        }

    private fun collectJoinDateHints(
        workbook: XSSFWorkbook,
        joinDateOverrides: Map<String, String>,
    ): List<JoinDateMappingHint> {
        val bySheetAndRaw = linkedMapOf<Pair<String, String>, LinkedHashSet<Pair<String, String>>>()
        for (state in listOf(MemberState.ACTIVE, MemberState.INACTIVE, MemberState.COMPLETED, MemberState.GRADUATED)) {
            val sheet = workbook.getSheet(MemberStateConverter.convertToString(state)) ?: continue
            val cols = ColumnNumberMapping.forState(state)
            val label = sheetDisplayName(state)
            val rows = sheet.iterator().asSequence().drop(1)
            for (row in rows) {
                if (joinScanShouldBreak(state, row)) break
                if (state == MemberState.INACTIVE) {
                    if (InactiveSheetRowRules.isNonDataRow(row)) continue
                    if (InactiveSheetRowRules.isDuplicateHeaderRow(row)) continue
                }
                if (state == MemberState.ACTIVE) {
                    val nameCell = row.getCell(ColumnNumberMapping.ACTIVE_MEMBER.name)
                    if (nameCell.isStrikethrough()) continue
                }
                if (joinCellNeedsMapping(row, cols.joinDate, joinDateOverrides, label)) {
                    val rawKey = row.getCell(cols.joinDate).getFormattedStringSafe().trim()
                    val nameRaw = row.getCell(cols.name).getFormattedStringSafe().trim()
                    val nickRaw = row.getCell(cols.nickname).getFormattedStringSafe().trim()
                    val name = nameRaw.ifBlank { "(이름 없음)" }
                    val nickname = nickRaw.ifBlank { "(닉 없음)" }
                    val key = label to rawKey
                    bySheetAndRaw.getOrPut(key) { linkedSetOf() }.add(name to nickname)
                }
            }
        }
        return bySheetAndRaw.entries
            .sortedWith(compareBy({ it.key.first }, { it.key.second.isEmpty() }, { it.key.second }))
            .map { (key, pairs) ->
                JoinDateMappingHint(
                    sheetLabel = key.first,
                    rawKey = key.second,
                    memberLabels = pairs.map { (n, nk) -> CompletionSemesterMemberLabel(name = n, nickname = nk) },
                )
            }
    }

    private fun collectExpectedReturnHints(
        workbook: XSSFWorkbook,
        expectedReturnOverrides: Map<String, String>,
    ): List<ExpectedReturnMappingHint> {
        val sheet = workbook.getSheet(MemberStateConverter.convertToString(MemberState.INACTIVE)) ?: return emptyList()
        val expectedCol = ColumnNumberMapping.INACTIVE_COL_EXPECTED_RETURN
        val byRaw = linkedMapOf<String, LinkedHashSet<Pair<String, String>>>()
        val rows = sheet.iterator().asSequence().drop(1)
        for (row in rows) {
            if (InactiveSheetRowRules.isFullyBlankRow(row)) continue
            if (InactiveSheetRowRules.isNonDataRow(row)) continue
            if (InactiveSheetRowRules.isDuplicateHeaderRow(row)) continue
            val raw = row.getCell(expectedCol).getFormattedStringSafe().trim()
            if (raw.isBlank()) continue
            val effective = inactiveSheetImportPolicy.effectiveExpectedReturnRaw(raw, expectedReturnOverrides)
                ?: continue
            if (inactiveSheetImportPolicy.resolveStoredSemester(effective) != null) continue
            val nameRaw = row.getCell(ColumnNumberMapping.INACTIVE_MEMBER.name).getFormattedStringSafe().trim()
            val nickRaw = row.getCell(ColumnNumberMapping.INACTIVE_MEMBER.nickname).getFormattedStringSafe().trim()
            val name = nameRaw.ifBlank { "(이름 없음)" }
            val nickname = nickRaw.ifBlank { "(닉 없음)" }
            byRaw.getOrPut(raw) { linkedSetOf() }.add(name to nickname)
        }
        return byRaw.entries
            .sortedWith(compareBy<Map.Entry<String, *>> { it.key.isEmpty() }.thenBy { it.key })
            .map { (rawKey, pairs) ->
                ExpectedReturnMappingHint(
                    rawKey = rawKey,
                    memberLabels = pairs.map { (n, nk) -> CompletionSemesterMemberLabel(name = n, nickname = nk) },
                )
            }
    }

    private fun collectInactiveActivitySemesterHints(
        workbook: XSSFWorkbook,
        inactiveActivitySemesterOverrides: Map<String, String>,
    ): List<InactiveActivitySemesterMappingHint> {
        val sheet = workbook.getSheet(MemberStateConverter.convertToString(MemberState.INACTIVE)) ?: return emptyList()
        val activityCol = ColumnNumberMapping.INACTIVE_COL_ACTIVITY_SEMESTER
        val byRaw = linkedMapOf<String, LinkedHashSet<Pair<String, String>>>()
        val rows = sheet.iterator().asSequence().drop(1)
        for (row in rows) {
            if (InactiveSheetRowRules.isFullyBlankRow(row)) continue
            if (InactiveSheetRowRules.isNonDataRow(row)) continue
            if (InactiveSheetRowRules.isDuplicateHeaderRow(row)) continue
            val raw = row.getCell(activityCol).getFormattedStringSafe().trim()
            if (raw.isBlank()) continue
            val effective =
                inactiveSheetImportPolicy.effectiveInactiveActivitySemesterRaw(raw, inactiveActivitySemesterOverrides)
                    ?: continue
            if (inactiveSheetImportPolicy.resolveStoredSemester(effective) != null) continue
            val nameRaw = row.getCell(ColumnNumberMapping.INACTIVE_MEMBER.name).getFormattedStringSafe().trim()
            val nickRaw = row.getCell(ColumnNumberMapping.INACTIVE_MEMBER.nickname).getFormattedStringSafe().trim()
            val name = nameRaw.ifBlank { "(이름 없음)" }
            val nickname = nickRaw.ifBlank { "(닉 없음)" }
            byRaw.getOrPut(raw) { linkedSetOf() }.add(name to nickname)
        }
        return byRaw.entries
            .sortedWith(compareBy<Map.Entry<String, *>> { it.key.isEmpty() }.thenBy { it.key })
            .map { (rawKey, pairs) ->
                InactiveActivitySemesterMappingHint(
                    rawKey = rawKey,
                    memberLabels = pairs.map { (n, nk) -> CompletionSemesterMemberLabel(name = n, nickname = nk) },
                )
            }
    }

    private fun joinCellNeedsMapping(
        row: Row,
        joinCol: Int,
        joinDateOverrides: Map<String, String>,
        sheetLabel: String,
    ): Boolean {
        val cell = row.getCell(joinCol)
        val rawKey = cell.getFormattedStringSafe().trim()
        val composite = "$sheetLabel|||$rawKey"
        val mapped = joinDateOverrides[composite]?.trim()?.takeIf { it.isNotBlank() }
            ?: joinDateOverrides[rawKey]?.trim()?.takeIf { it.isNotBlank() }
        if (mapped != null) {
            val d = runCatching { LocalDate.parse(mapped) }.getOrNull() ?: return true
            return d.year < MIN_REASONABLE_JOIN_YEAR
        }
        val parsed = cell.getFlexibleLocalDateSafe(null)
        return parsed == null || parsed.year < MIN_REASONABLE_JOIN_YEAR
    }

    private fun resolveLabelToStoredSemester(label: String): Semester? {
        val t = label.trim()
        if (t.isBlank()) return null
        val parsed = runCatching { Semester.of(t) }.getOrNull() ?: return null
        return semesterRepository.find(parsed)
    }

    private fun sheetDisplayName(state: MemberState): String =
        when (state) {
            MemberState.ACTIVE -> "액티브"
            MemberState.INACTIVE -> "비액티브"
            MemberState.COMPLETED -> "수료"
            MemberState.GRADUATED -> "졸업"
            MemberState.WITHDRAWN -> "탈퇴"
        }

    companion object {
        private const val MIN_REASONABLE_JOIN_YEAR = 1950
    }
}
