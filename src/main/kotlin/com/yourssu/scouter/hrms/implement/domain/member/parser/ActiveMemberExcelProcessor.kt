package com.yourssu.scouter.hrms.implement.domain.member.parser

import com.yourssu.scouter.common.implement.domain.department.Department
import com.yourssu.scouter.common.implement.domain.part.Part
import com.yourssu.scouter.hrms.implement.domain.member.ActiveMember
import com.yourssu.scouter.hrms.implement.domain.member.Member
import com.yourssu.scouter.hrms.implement.domain.member.MemberReader
import com.yourssu.scouter.hrms.implement.domain.member.MemberState
import com.yourssu.scouter.hrms.implement.domain.member.MemberWriter
import com.yourssu.scouter.hrms.implement.support.getStringSafe
import com.yourssu.scouter.hrms.implement.support.isNullOrBlank
import com.yourssu.scouter.hrms.implement.support.isStrikethrough
import com.yourssu.scouter.hrms.implement.support.AliasMappingUtils
import org.apache.poi.ss.usermodel.Row
import org.apache.poi.ss.usermodel.Sheet
import org.springframework.stereotype.Component
import java.time.Instant

@Component
class ActiveMemberExcelProcessor(
    private val basicMemberExcelProcessor: BasicMemberExcelProcessor,
    private val memberReader: MemberReader,
    private val memberWriter: MemberWriter,
) : MemberExcelProcessor {

    override fun supportingState(): MemberState {
        return MemberState.ACTIVE
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

            // 이름 셀에 취소선이 그어져 있으면 해당 행은 스킵
            val nameCell = row.getCell(ColumnNumberMapping.ACTIVE_MEMBER.name)
            if (nameCell.isStrikethrough()) {
                continue
            }

            runCatching {
                parseRow(row, departments, parts, normalizedDepartments, normalizedParts, departmentOverrides)
            }.onFailure { e ->
                errorMessages.add("액티브 시트 ${index + 2}번째 줄 오류: ${e.message}")
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
        val isMembershipFeePaid = row.getCell(11).getStringSafe().equals("o", true)
        val parsedMember: Member = basicMemberExcelProcessor.rowToMember(
            row = row,
            departments = departments,
            parts = parts,
            columnMapping = ColumnNumberMapping.ACTIVE_MEMBER,
            state = MemberState.ACTIVE,
            normalizedDepartments = normalizedDepartments,
            normalizedParts = normalizedParts,
            departmentOverrides = departmentOverrides,
        )

        val oldMember = memberReader.readByStudentIdOrNull(parsedMember.studentId)
        if (oldMember == null) {
            memberWriter.writeMemberWithActiveStatus(parsedMember, isMembershipFeePaid)
            return
        }

        val patchedMember = basicMemberExcelProcessor.mergeForPatch(oldMember, parsedMember)
        if (oldMember.state == MemberState.ACTIVE) {
            patchedMember.updateState(MemberState.ACTIVE, oldMember.stateUpdatedTime)
            val currentActiveMember: ActiveMember = memberReader.readActiveByMemberId(patchedMember.id!!)
            val updateActiveMember = ActiveMember(
                id = currentActiveMember.id,
                member = patchedMember,
                isMembershipFeePaid = isMembershipFeePaid,
            )
            memberWriter.update(updateActiveMember)

            return
        }

        patchedMember.updateState(MemberState.ACTIVE, Instant.now())

        if (oldMember.state == MemberState.INACTIVE) {
            memberWriter.deleteFromInactiveMember(patchedMember)
        }

        if (oldMember.state == MemberState.GRADUATED) {
            memberWriter.deleteFromGraduatedMember(patchedMember)
        }

        if (oldMember.state == MemberState.WITHDRAWN) {
            memberWriter.deleteFromWithdrawnMember(patchedMember)
        }

        memberWriter.writeMemberWithActiveStatus(patchedMember, isMembershipFeePaid)
    }
}
