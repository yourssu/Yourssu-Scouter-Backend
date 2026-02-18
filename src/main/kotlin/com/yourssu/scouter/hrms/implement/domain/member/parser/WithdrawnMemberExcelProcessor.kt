package com.yourssu.scouter.hrms.implement.domain.member.parser

import com.yourssu.scouter.common.implement.domain.department.Department
import com.yourssu.scouter.common.implement.domain.part.Part
import com.yourssu.scouter.hrms.implement.domain.member.Member
import com.yourssu.scouter.hrms.implement.domain.member.MemberReader
import com.yourssu.scouter.hrms.implement.domain.member.MemberState
import com.yourssu.scouter.hrms.implement.domain.member.MemberWriter
import com.yourssu.scouter.hrms.implement.domain.member.WithdrawnMember
import com.yourssu.scouter.hrms.implement.support.isNullOrBlank
import com.yourssu.scouter.hrms.implement.support.AliasMappingUtils
import org.apache.poi.ss.usermodel.Row
import org.apache.poi.ss.usermodel.Sheet
import org.springframework.stereotype.Component
import java.time.Instant

@Component
class WithdrawnMemberExcelProcessor(
    private val basicMemberExcelProcessor: BasicMemberExcelProcessor,
    private val memberReader: MemberReader,
    private val memberWriter: MemberWriter,
) : MemberExcelProcessor {

    override fun supportingState(): MemberState {
        return MemberState.WITHDRAWN
    }

    override fun parse(
        sheet: Sheet,
        departments: Map<String, Department>,
        parts: Map<String, Part>
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
                parseRow(row, departments, parts, normalizedDepartments, normalizedParts)
            }.onFailure { e ->
                errorMessages.add("탈퇴 시트 ${index + 2}번째 줄 오류: ${e.message}")
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
    ) {
        val parsedMember: Member = basicMemberExcelProcessor.rowToMember(
            row = row,
            departments = departments,
            parts = parts,
            columnMapping = ColumnNumberMapping.WITHDRAWN_MEMBER,
            state = MemberState.WITHDRAWN,
            normalizedDepartments = normalizedDepartments,
            normalizedParts = normalizedParts,
        )

        val oldMember = memberReader.readByStudentIdOrNull(parsedMember.studentId)
        if (oldMember == null) {
            memberWriter.writeMemberWithWithdrawnState(parsedMember)
            return
        }

        parsedMember.id = oldMember.id
        if (oldMember.state == MemberState.WITHDRAWN) {
            parsedMember.updateState(MemberState.WITHDRAWN, oldMember.stateUpdatedTime)
            val currentWithdrawnMember: WithdrawnMember = memberReader.readWithdrawnByMemberId(parsedMember.id!!)
            val updateWithdrawnMember = WithdrawnMember(
                id = currentWithdrawnMember.id,
                member = parsedMember,
            )
            memberWriter.update(updateWithdrawnMember)

            return
        }

        parsedMember.updateState(MemberState.WITHDRAWN, Instant.now())

        if (oldMember.state == MemberState.ACTIVE) {
            memberWriter.deleteFromActiveMember(parsedMember)
        }

        if (oldMember.state == MemberState.INACTIVE) {
            memberWriter.deleteFromInactiveMember(parsedMember)
        }

        if (oldMember.state == MemberState.GRADUATED) {
            memberWriter.deleteFromGraduatedMember(parsedMember)
        }

        memberWriter.writeMemberWithWithdrawnState(parsedMember)
    }
}
