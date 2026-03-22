package com.yourssu.scouter.hrms.implement.domain.member.parser

import com.yourssu.scouter.common.implement.domain.department.Department
import com.yourssu.scouter.common.implement.domain.part.Part
import com.yourssu.scouter.common.implement.domain.semester.Semester
import com.yourssu.scouter.common.implement.domain.semester.SemesterRepository
import com.yourssu.scouter.hrms.business.domain.member.MemberExcelImportOverrides
import com.yourssu.scouter.hrms.implement.domain.member.InactiveMember
import com.yourssu.scouter.hrms.implement.domain.member.Member
import com.yourssu.scouter.hrms.implement.domain.member.MemberReader
import com.yourssu.scouter.hrms.implement.domain.member.MemberState
import com.yourssu.scouter.hrms.implement.domain.member.MemberWriter
import com.yourssu.scouter.hrms.implement.support.getFormattedStringSafe
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
    private val inactiveSheetImportPolicy: InactiveSheetImportPolicy,
) : MemberExcelProcessor {

    companion object {
        private val TEMP_DATE_FOR_NULL = LocalDate.of(2099, 3, 1)
    }

    override fun supportingState(): MemberState {
        return MemberState.INACTIVE
    }

    override fun parse(
        sheet: Sheet,
        departments: Map<String, Department>,
        parts: Map<String, Part>,
        overrides: MemberExcelImportOverrides,
    ): ErrorMessages {
        val errorMessages = mutableListOf<String>()
        val normalizedDepartments: Map<String, Department> =
            departments.entries.associate { AliasMappingUtils.normalizeKey(it.key) to it.value }
        val normalizedParts: Map<String, Part> =
            parts.entries.associate { AliasMappingUtils.normalizeKey(it.key) to it.value }

        val rows = sheet.iterator().asSequence().drop(1)
        for ((index, row) in rows.withIndex()) {
            if (InactiveSheetRowRules.isFullyBlankRow(row)) {
                continue
            }
            if (InactiveSheetRowRules.isNonDataRow(row)) {
                continue
            }
            if (InactiveSheetRowRules.isDuplicateHeaderRow(row)) {
                continue
            }

            runCatching {
                parseRow(row, departments, parts, normalizedDepartments, normalizedParts, overrides)
            }.onFailure { e ->
                errorMessages.add("비액티브 시트 ${index + 2}번째 줄 오류: ${e.message}")
            }
        }

        return ErrorMessages(errorMessages)
    }

    private fun parseInactiveExtraFromRow(row: Row): InactiveExtraRow {
        val reason =
            row.getCell(ColumnNumberMapping.INACTIVE_COL_REASON).getFormattedStringSafe().trim().takeIf { it.isNotBlank() }
        val inactiveSemesterStr =
            row.getCell(ColumnNumberMapping.INACTIVE_COL_ACTIVITY_SEMESTER).getFormattedStringSafe().trim()
                .takeIf { it.isNotBlank() }
        val expectedReturnStr =
            row.getCell(ColumnNumberMapping.INACTIVE_COL_EXPECTED_RETURN).getFormattedStringSafe().trim()
                .takeIf { it.isNotBlank() }
        val smsReplied =
            parseSmsReplied(row.getCell(ColumnNumberMapping.INACTIVE_COL_SMS_REPLIED).getFormattedStringSafe())
        val smsReplyDesiredPeriod =
            row.getCell(ColumnNumberMapping.INACTIVE_COL_SMS_REPLY_DESIRED_PERIOD).getFormattedStringSafe().trim()
                .takeIf { it.isNotBlank() }
        return InactiveExtraRow(reason, inactiveSemesterStr, expectedReturnStr, smsReplied, smsReplyDesiredPeriod)
    }

    private fun parseSmsReplied(raw: String): Boolean? {
        val s = raw.trim().lowercase()
        if (s.isBlank()) return null
        if (s in listOf("o", "y", "예", "true", "1")) return true
        if (s in listOf("x", "n", "아니오", "false", "0")) return false
        return null
    }

    private fun parseRow(
        row: Row,
        departments: Map<String, Department>,
        parts: Map<String, Part>,
        normalizedDepartments: Map<String, Department>,
        normalizedParts: Map<String, Part>,
        overrides: MemberExcelImportOverrides,
    ) {
        val parsedMember: Member = basicMemberExcelProcessor.rowToMember(
            row = row,
            departments = departments,
            parts = parts,
            columnMapping = ColumnNumberMapping.INACTIVE_MEMBER,
            state = MemberState.INACTIVE,
            normalizedDepartments = normalizedDepartments,
            normalizedParts = normalizedParts,
            departmentOverrides = overrides.departmentOverrides,
            joinDateOverrides = overrides.joinDateOverrides,
            joinDateSheetLabel = "비액티브",
        )

        val extra = parseInactiveExtraFromRow(row)
        val effectiveReturn =
            inactiveSheetImportPolicy.effectiveExpectedReturnRaw(extra.expectedReturnStr, overrides.expectedReturnOverrides)
        val reasonToPass = inactiveSheetImportPolicy.mergeExpectedReturnIntoReason(extra.reason, effectiveReturn)
        val expectedReturnSemesterStrToPass =
            effectiveReturn?.takeIf { inactiveSheetImportPolicy.resolveStoredSemester(it) != null }

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
                inactiveSheetImportPolicy.resolveStoredSemester(effectiveReturn) ?: currentInactiveMember.expectedReturnSemester
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

private data class InactiveExtraRow(
    val reason: String?,
    val inactiveSemesterStr: String?,
    val expectedReturnStr: String?,
    val smsReplied: Boolean?,
    val smsReplyDesiredPeriod: String?,
)
