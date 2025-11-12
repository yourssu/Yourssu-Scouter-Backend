package com.yourssu.scouter.hrms.implement.domain.member.parser

import com.yourssu.scouter.common.implement.domain.department.Department
import com.yourssu.scouter.common.implement.domain.part.Part
import com.yourssu.scouter.common.implement.domain.semester.Semester
import com.yourssu.scouter.common.implement.domain.semester.SemesterReader
import com.yourssu.scouter.hrms.implement.domain.member.GraduatedMember
import com.yourssu.scouter.hrms.implement.domain.member.Member
import com.yourssu.scouter.hrms.implement.domain.member.MemberReader
import com.yourssu.scouter.hrms.implement.domain.member.MemberState
import com.yourssu.scouter.hrms.implement.domain.member.MemberWriter
import com.yourssu.scouter.hrms.implement.support.getStringSafe
import com.yourssu.scouter.hrms.implement.support.isNullOrBlank
import com.yourssu.scouter.hrms.implement.support.AliasMappingUtils
import java.time.LocalDateTime
import org.apache.poi.ss.usermodel.Row
import org.apache.poi.ss.usermodel.Sheet
import org.springframework.stereotype.Component

@Component
class GraduatedMemberExcelProcessor(
    private val basicMemberExcelProcessor: BasicMemberExcelProcessor,
    private val semesterReader: SemesterReader,
    private val memberReader: MemberReader,
    private val memberWriter: MemberWriter,
) : MemberExcelProcessor {

    override fun supportingState(): MemberState {
        return MemberState.GRADUATED
    }

    override fun parse(sheet: Sheet, departments: Map<String, Department>, parts: Map<String, Part>): ErrorMessages {
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
                errorMessages.add("졸업 시트 ${index + 2}번째 줄 오류: ${e.message}")
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
        val graduatedSemesterValue: String = row.getCell(10).getStringSafe()
        val graduatedSemester: Semester = semesterReader.readByString(graduatedSemesterValue)
        val parsedMember: Member = basicMemberExcelProcessor.rowToMember(
            row = row,
            departments = departments,
            parts = parts,
            columnMapping = ColumnNumberMapping.GRADUATED_MEMBER,
            state = MemberState.GRADUATED,
            normalizedDepartments = normalizedDepartments,
            normalizedParts = normalizedParts,
        )

        val oldMember = memberReader.readByStudentIdOrNull(parsedMember.studentId)
        if (oldMember == null) {
            memberWriter.writeMemberWithGraduatedState(parsedMember, graduatedSemester)
            return
        }


        parsedMember.id = oldMember.id
        if (oldMember.state == MemberState.GRADUATED) {
            parsedMember.updateState(MemberState.GRADUATED, oldMember.stateUpdatedTime)
            val currentGraduatedMember: GraduatedMember = memberReader.readGraduatedByMemberId(parsedMember.id!!)
            val updateGraduatedMember = GraduatedMember(
                id = currentGraduatedMember.id,
                member = parsedMember,
                joinSemester = currentGraduatedMember.activePeriod.startSemester,
                previousSemesterBeforeStateChange = semesterReader.read(graduatedSemester.previous()),
            )

            memberWriter.update(updateGraduatedMember)

            return
        }

        parsedMember.updateState(MemberState.GRADUATED, LocalDateTime.now())

        if (oldMember.state == MemberState.ACTIVE) {
            memberWriter.deleteFromActiveMember(parsedMember)
        }

        if (oldMember.state == MemberState.INACTIVE) {
            memberWriter.deleteFromInactiveMember(parsedMember)
        }

        if (oldMember.state == MemberState.WITHDRAWN) {
            memberWriter.deleteFromWithdrawnMember(parsedMember)
        }

        memberWriter.writeMemberWithGraduatedState(parsedMember, graduatedSemester)
    }
}
