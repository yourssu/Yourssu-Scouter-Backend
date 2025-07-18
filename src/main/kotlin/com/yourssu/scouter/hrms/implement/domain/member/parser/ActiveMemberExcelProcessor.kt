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
import java.time.LocalDateTime
import org.apache.poi.ss.usermodel.Row
import org.apache.poi.ss.usermodel.Sheet
import org.springframework.stereotype.Component

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
    ): ErrorMessages {
        val errorMessages = mutableListOf<String>()
        val rows = sheet.iterator().asSequence().drop(1)

        for ((index, row) in rows.withIndex()) {
            if (row.getCell(0).isNullOrBlank()) {
                break
            }

            runCatching {
                parseRow(row, departments, parts)
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
    ) {
        val isMembershipFeePaid = row.getCell(10).getStringSafe().equals("o", true)
        val parsedMember: Member = basicMemberExcelProcessor.rowToMember(
            row = row,
            departments = departments,
            parts = parts,
            columnMapping = ColumnNumberMapping.ACTIVE_MEMBER,
            state = MemberState.ACTIVE,
        )

        val oldMember = memberReader.readByStudentIdOrNull(parsedMember.studentId)
        if (oldMember == null) {
            memberWriter.writeMemberWithActiveStatus(parsedMember, isMembershipFeePaid)
            return
        }

        parsedMember.id = oldMember.id
        if (oldMember.state == MemberState.ACTIVE) {
            parsedMember.updateState(MemberState.ACTIVE, oldMember.stateUpdatedTime)
            val currentActiveMember: ActiveMember = memberReader.readActiveByMemberId(parsedMember.id!!)
            val updateActiveMember = ActiveMember(
                id = currentActiveMember.id,
                member = parsedMember,
                isMembershipFeePaid = isMembershipFeePaid,
            )
            memberWriter.update(updateActiveMember)

            return
        }

        parsedMember.updateState(MemberState.ACTIVE, LocalDateTime.now())

        if (oldMember.state == MemberState.INACTIVE) {
            memberWriter.deleteFromInactiveMember(parsedMember)
        }

        if (oldMember.state == MemberState.GRADUATED) {
            memberWriter.deleteFromGraduatedMember(parsedMember)
        }

        if (oldMember.state == MemberState.WITHDRAWN) {
            memberWriter.deleteFromWithdrawnMember(parsedMember)
        }

        memberWriter.writeMemberWithActiveStatus(parsedMember, isMembershipFeePaid)
    }
}
