package com.yourssu.scouter.hrms.implement.domain.member.parser

import com.yourssu.scouter.common.implement.domain.department.Department
import com.yourssu.scouter.common.implement.domain.part.Part
import com.yourssu.scouter.hrms.implement.domain.member.InactiveMember
import com.yourssu.scouter.hrms.implement.domain.member.Member
import com.yourssu.scouter.hrms.implement.domain.member.MemberReader
import com.yourssu.scouter.hrms.implement.domain.member.MemberState
import com.yourssu.scouter.hrms.implement.domain.member.MemberWriter
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
) : MemberExcelProcessor {

    companion object {
        private val TEMP_DATE_FOR_NULL = LocalDate.of(2099, 3, 1)
        private val TEMP_INACTIVE_DATE_FOR_NULL = LocalDate.of(2099, 3, 1)
    }

    override fun supportingState(): MemberState {
        return MemberState.INACTIVE
    }

    override fun parse(
        sheet: Sheet,
        departments: Map<String, Department>,
        parts: Map<String, Part>,
        departmentOverrides: Map<String, String>,
    ): ErrorMessages {
        val errorMessages = mutableListOf<String>()
        val rows = sheet.iterator().asSequence().drop(1)
        val normalizedDepartments: Map<String, Department> =
            departments.entries.associate { AliasMappingUtils.normalizeKey(it.key) to it.value }
        val normalizedParts: Map<String, Part> =
            parts.entries.associate { AliasMappingUtils.normalizeKey(it.key) to it.value }

        for ((index, row) in rows.withIndex()) {
            if (row.getCell(0).isNullOrBlank()) {
                break
            }

            runCatching {
                parseRow(row, departments, parts, normalizedDepartments, normalizedParts, departmentOverrides)
            }.onFailure { e ->
                errorMessages.add("비액티브 시트 ${index + 2}번째 줄 오류: ${e.message}")
            }
        }

        return ErrorMessages(errorMessages)
    }

    private fun parseRow(
        row: Row,
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

        val oldMember = memberReader.readByStudentIdOrNull(parsedMember.studentId)
        if (oldMember == null) {
            memberWriter.writeMemberWithInactiveState(parsedMember, TEMP_DATE_FOR_NULL)

            return
        }

        val patchedMember = basicMemberExcelProcessor.mergeForPatch(oldMember, parsedMember)
        if (oldMember.state == MemberState.INACTIVE) {
            patchedMember.updateState(MemberState.INACTIVE, oldMember.stateUpdatedTime)
            val currentInactiveMember: InactiveMember = memberReader.readInactiveByMemberId(patchedMember.id!!)
            val updateInactiveMember = InactiveMember(
                id = currentInactiveMember.id,
                member = patchedMember,
                activePeriod = currentInactiveMember.activePeriod,
                expectedReturnSemester = currentInactiveMember.expectedReturnSemester,
                inactivePeriod = currentInactiveMember.inactivePeriod,
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

        if (oldMember.state == MemberState.WITHDRAWN) {
            memberWriter.deleteFromWithdrawnMember(patchedMember)
        }

        memberWriter.writeMemberWithInactiveState(patchedMember, TEMP_DATE_FOR_NULL)
    }
}
