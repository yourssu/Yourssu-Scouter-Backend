package com.yourssu.scouter.hrms.implement.domain.member.parser

import com.yourssu.scouter.common.implement.domain.department.Department
import com.yourssu.scouter.common.implement.domain.part.Part
import com.yourssu.scouter.hrms.implement.domain.member.Member
import com.yourssu.scouter.hrms.implement.domain.member.MemberReader
import com.yourssu.scouter.hrms.implement.domain.member.MemberState
import com.yourssu.scouter.hrms.implement.domain.member.MemberWriter
import com.yourssu.scouter.hrms.implement.support.isNullOrBlank
import java.time.LocalDate
import java.time.LocalDateTime
import org.apache.poi.ss.usermodel.Row
import org.apache.poi.ss.usermodel.Sheet
import org.springframework.stereotype.Component

@Component
class InactiveMemberExcelProcessor(
    private val basicMemberExcelProcessor: BasicMemberExcelProcessor,
    private val memberReader: MemberReader,
    private val memberWriter: MemberWriter,
) : MemberExcelProcessor {

    companion object {
        private val TEMP_DATE_FOR_NULL = LocalDate.of(2099, 3, 1)
    }

    override fun supportingState(): MemberState {
        return MemberState.INACTIVE
    }

    override fun parse(sheet: Sheet, departments: Map<String, Department>, parts: Map<String, Part>): ErrorMessages {
        val errorMessages = mutableListOf<String>()
        val rows = sheet.iterator().asSequence().drop(1)

        for ((index, row) in rows.withIndex()) {
            if (row.getCell(0).isNullOrBlank()) {
                break
            }

            runCatching {
                parseRow(row, departments, parts)
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
    ) {
        val parsedMember: Member = basicMemberExcelProcessor.rowToMember(
            row = row,
            departments = departments,
            parts = parts,
            columnMapping = ColumnNumberMapping.INACTIVE_MEMBER,
            state = MemberState.INACTIVE,
        )

        val oldMember = memberReader.readByStudentIdOrNull(parsedMember.studentId)
        if (oldMember == null) {
            memberWriter.writeMemberWithInactiveState(parsedMember, TEMP_DATE_FOR_NULL)

            return
        }

        parsedMember.id = oldMember.id
        if (oldMember.state == MemberState.INACTIVE) {
            parsedMember.updateState(MemberState.INACTIVE, oldMember.stateUpdatedTime)
        }

        parsedMember.updateState(MemberState.INACTIVE, LocalDateTime.now())

        if (oldMember.state == MemberState.ACTIVE) {
            memberWriter.deleteFromActiveMember(parsedMember)
        }

        if (oldMember.state == MemberState.GRADUATED) {
            memberWriter.deleteFromGraduatedMember(parsedMember)
        }

        if (oldMember.state == MemberState.WITHDRAWN) {
            memberWriter.deleteFromWithdrawnMember(parsedMember)
        }

        memberWriter.writeMemberWithInactiveState(parsedMember, TEMP_DATE_FOR_NULL)
    }
}
