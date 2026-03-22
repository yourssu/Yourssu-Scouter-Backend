package com.yourssu.scouter.hrms.implement.domain.member.parser

import com.yourssu.scouter.common.implement.domain.department.Department
import com.yourssu.scouter.common.implement.domain.part.Part
import com.yourssu.scouter.common.implement.domain.semester.Semester
import com.yourssu.scouter.common.implement.domain.semester.SemesterRepository
import com.yourssu.scouter.hrms.implement.domain.member.InactiveMember
import com.yourssu.scouter.hrms.implement.domain.member.Member
import com.yourssu.scouter.hrms.implement.domain.member.MemberReader
import com.yourssu.scouter.hrms.implement.domain.member.MemberState
import com.yourssu.scouter.hrms.implement.domain.member.MemberWriter
import com.yourssu.scouter.hrms.implement.support.getFormattedStringSafe
import com.yourssu.scouter.hrms.implement.support.isNullOrBlank
import com.yourssu.scouter.hrms.implement.support.AliasMappingUtils
import org.apache.poi.ss.usermodel.Row
import org.apache.poi.ss.usermodel.Sheet
import org.springframework.stereotype.Component
import java.time.Instant
import java.time.LocalDate

@Component
class InactiveMemberExcelProcessor(
    private val basicMemberExcelProcessor: BasicMemberExcelProcessor,
    private val memberReader: MemberReader,
    private val memberWriter: MemberWriter,
    private val semesterRepository: SemesterRepository,
) : MemberExcelProcessor {

    companion object {
        private val TEMP_DATE_FOR_NULL = LocalDate.of(2099, 3, 1)

        private const val DEFAULT_COL_REASON = 10
        private const val DEFAULT_COL_ACTIVITY_SEMESTER = 11
        private const val DEFAULT_COL_EXPECTED_RETURN = 12
        private const val DEFAULT_COL_SMS_REPLIED = 13
        private const val DEFAULT_COL_SMS_REPLY_DESIRED_PERIOD = 14

        private const val MAX_HEADER_SCAN = 25

        private fun findColumnIndex(headerRow: Row, keywords: List<String>, defaultIndex: Int): Int {
            for (col in 0..MAX_HEADER_SCAN) {
                val cell = headerRow.getCell(col) ?: break
                val text = cell.getFormattedStringSafe().trim().replace(" ", "")
                if (keywords.any { keyword -> text.contains(keyword.replace(" ", "")) }) {
                    return col
                }
            }
            return defaultIndex
        }
    }

    override fun supportingState(): MemberState {
        return MemberState.INACTIVE
    }

    override fun parse(
        sheet: Sheet,
        departments: Map<String, Department>,
        parts: Map<String, Part>,
        departmentOverrides: Map<String, String>,
        completionSemesterOverrides: Map<String, String>,
    ): ErrorMessages {
        val errorMessages = mutableListOf<String>()
        val headerRow = sheet.getRow(0)
        val extraCols = if (headerRow != null) {
            InactiveExtraCols(
                reason = findColumnIndex(headerRow, listOf("사유"), DEFAULT_COL_REASON),
                activitySemester = findColumnIndex(headerRow, listOf("활동학기", "활동 학기"), DEFAULT_COL_ACTIVITY_SEMESTER),
                expectedReturn = findColumnIndex(headerRow, listOf("예정복귀"), DEFAULT_COL_EXPECTED_RETURN),
                smsReplied = findColumnIndex(headerRow, listOf("문자회신여부", "문자회신 여부"), DEFAULT_COL_SMS_REPLIED),
                smsReplyDesiredPeriod = findColumnIndex(headerRow, listOf("문자회신희망시기", "문자회신 희망시기"), DEFAULT_COL_SMS_REPLY_DESIRED_PERIOD),
            )
        } else {
            InactiveExtraCols(DEFAULT_COL_REASON, DEFAULT_COL_ACTIVITY_SEMESTER, DEFAULT_COL_EXPECTED_RETURN, DEFAULT_COL_SMS_REPLIED, DEFAULT_COL_SMS_REPLY_DESIRED_PERIOD)
        }
        val normalizedDepartments: Map<String, Department> =
            departments.entries.associate { AliasMappingUtils.normalizeKey(it.key) to it.value }
        val normalizedParts: Map<String, Part> =
            parts.entries.associate { AliasMappingUtils.normalizeKey(it.key) to it.value }

        val rows = sheet.iterator().asSequence().drop(1)
        for ((index, row) in rows.withIndex()) {
            if (row.getCell(0).isNullOrBlank()) {
                break
            }

            runCatching {
                parseRow(row, extraCols, departments, parts, normalizedDepartments, normalizedParts, departmentOverrides)
            }.onFailure { e ->
                errorMessages.add("비액티브 시트 ${index + 2}번째 줄 오류: ${e.message}")
            }
        }

        return ErrorMessages(errorMessages)
    }

    private fun parseInactiveExtraFromRow(row: Row, extraCols: InactiveExtraCols): InactiveExtraRow {
        val reason = row.getCell(extraCols.reason).getFormattedStringSafe().trim().takeIf { it.isNotBlank() }
        val inactiveSemesterStr =
            row.getCell(extraCols.activitySemester).getFormattedStringSafe().trim().takeIf { it.isNotBlank() }
        val expectedReturnStr =
            row.getCell(extraCols.expectedReturn).getFormattedStringSafe().trim().takeIf { it.isNotBlank() }
        val smsReplied = parseSmsReplied(row.getCell(extraCols.smsReplied).getFormattedStringSafe())
        val smsReplyDesiredPeriod =
            row.getCell(extraCols.smsReplyDesiredPeriod).getFormattedStringSafe().trim().takeIf { it.isNotBlank() }
        return InactiveExtraRow(reason, inactiveSemesterStr, expectedReturnStr, smsReplied, smsReplyDesiredPeriod)
    }

    private fun parseSmsReplied(raw: String): Boolean? {
        val s = raw.trim().lowercase()
        if (s.isBlank()) return null
        if (s in listOf("o", "y", "예", "true", "1")) return true
        if (s in listOf("x", "n", "아니오", "false", "0")) return false
        return null
    }

    /** 예정복귀 시기가 학기 형식(예: 2026-1)이면 파싱해 반환, 아니면 null (비학기 문구는 reason 등 메모로 반영). */
    private fun tryParseExpectedReturnSemester(str: String?): Semester? {
        if (str.isNullOrBlank()) return null
        return runCatching {
            semesterRepository.find(Semester.of(str.trim()))
        }.getOrNull()
    }

    /** 예정복귀 시기 문자열이 학기로 파싱되지 않으면 reason에 메모로 붙인 값 반환. */
    private fun mergeExpectedReturnIntoReason(reason: String?, expectedReturnStr: String?): String? {
        if (expectedReturnStr.isNullOrBlank()) return reason?.takeIf { it.isNotBlank() }
        if (tryParseExpectedReturnSemester(expectedReturnStr) != null) return reason?.takeIf { it.isNotBlank() }
        val memo = "예정복귀: $expectedReturnStr"
        return if (reason.isNullOrBlank()) memo else "$reason [${memo}]"
    }

    private fun parseRow(
        row: Row,
        extraCols: InactiveExtraCols,
        departments: Map<String, Department>,
        parts: Map<String, Part>,
        normalizedDepartments: Map<String, Department>,
        normalizedParts: Map<String, Part>,
        departmentOverrides: Map<String, String> = emptyMap(),
    ) {
        val parsedMember: Member = basicMemberExcelProcessor.rowToMember(
            row = row,
            departments = departments,
            parts = parts,
            columnMapping = ColumnNumberMapping.INACTIVE_MEMBER,
            state = MemberState.INACTIVE,
            normalizedDepartments = normalizedDepartments,
            normalizedParts = normalizedParts,
            departmentOverrides = departmentOverrides,
        )

        val extra = parseInactiveExtraFromRow(row, extraCols)
        val reasonToPass = mergeExpectedReturnIntoReason(extra.reason, extra.expectedReturnStr)
        val expectedReturnSemesterStrToPass =
            extra.expectedReturnStr?.takeIf { tryParseExpectedReturnSemester(it) != null }

        val oldMember = memberReader.readByStudentIdOrNull(parsedMember.studentId)
        if (oldMember == null) {
            memberWriter.writeMemberWithInactiveState(
                parsedMember,
                TEMP_DATE_FOR_NULL,
                reason = reasonToPass,
                inactiveSemesterStr = extra.inactiveSemesterStr,
                expectedReturnSemesterStr = expectedReturnSemesterStrToPass,
                smsReplied = extra.smsReplied,
                smsReplyDesiredPeriod = extra.smsReplyDesiredPeriod,
            )
            return
        }

        val patchedMember = basicMemberExcelProcessor.mergeForPatch(oldMember, parsedMember)
        if (oldMember.state == MemberState.INACTIVE) {
            patchedMember.updateState(MemberState.INACTIVE, oldMember.stateUpdatedTime)
            val currentInactiveMember: InactiveMember = memberReader.readInactiveByMemberId(patchedMember.id!!)
            val newExpectedReturn =
                tryParseExpectedReturnSemester(extra.expectedReturnStr) ?: currentInactiveMember.expectedReturnSemester
            val base = if (newExpectedReturn != currentInactiveMember.expectedReturnSemester) {
                val prev = semesterRepository.find(newExpectedReturn.previous())
                    ?: currentInactiveMember.inactivePeriod.endSemester
                currentInactiveMember.updateExpectedReturnSemester(newExpectedReturn, prev)
            } else {
                currentInactiveMember
            }
            val updateInactiveMember = InactiveMember(
                id = base.id,
                member = patchedMember,
                activePeriod = base.activePeriod,
                expectedReturnSemester = base.expectedReturnSemester,
                inactivePeriod = base.inactivePeriod,
                reason = reasonToPass ?: base.reason,
                smsReplied = extra.smsReplied ?: base.smsReplied,
                smsReplyDesiredPeriod = extra.smsReplyDesiredPeriod ?: base.smsReplyDesiredPeriod,
            )
            memberWriter.update(updateInactiveMember)
            return
        }

        patchedMember.updateState(MemberState.INACTIVE, Instant.now())

        if (oldMember.state == MemberState.ACTIVE) {
            memberWriter.deleteFromActiveMember(patchedMember)
        }

        if (oldMember.state == MemberState.GRADUATED) {
            memberWriter.deleteFromGraduatedMember(patchedMember)
        }

        if (oldMember.state == MemberState.COMPLETED) {
            memberWriter.deleteFromCompletedMember(patchedMember)
        }

        if (oldMember.state == MemberState.WITHDRAWN) {
            memberWriter.deleteFromWithdrawnMember(patchedMember)
        }

        memberWriter.writeMemberWithInactiveState(
            patchedMember,
            TEMP_DATE_FOR_NULL,
            reason = reasonToPass,
            inactiveSemesterStr = extra.inactiveSemesterStr,
            expectedReturnSemesterStr = expectedReturnSemesterStrToPass,
            smsReplied = extra.smsReplied,
            smsReplyDesiredPeriod = extra.smsReplyDesiredPeriod,
        )
    }
}

private data class InactiveExtraCols(
    val reason: Int,
    val activitySemester: Int,
    val expectedReturn: Int,
    val smsReplied: Int,
    val smsReplyDesiredPeriod: Int,
)

private data class InactiveExtraRow(
    val reason: String?,
    val inactiveSemesterStr: String?,
    val expectedReturnStr: String?,
    val smsReplied: Boolean?,
    val smsReplyDesiredPeriod: String?,
)
