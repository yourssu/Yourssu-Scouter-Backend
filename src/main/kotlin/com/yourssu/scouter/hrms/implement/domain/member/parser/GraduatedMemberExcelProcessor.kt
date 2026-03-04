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
import com.yourssu.scouter.hrms.implement.domain.member.SemesterPeriod
import com.yourssu.scouter.hrms.implement.support.getStringSafe
import com.yourssu.scouter.hrms.implement.support.isNullOrBlank
import com.yourssu.scouter.hrms.implement.support.AliasMappingUtils
import org.apache.poi.ss.usermodel.Row
import org.apache.poi.ss.usermodel.Sheet
import org.springframework.stereotype.Component
import java.time.Instant
import java.time.LocalDate

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
        departmentOverrides: Map<String, String> = emptyMap(),
    ) {
        // 졸업 시트: 11번 열(0-based)이 활동기간/졸업학기
        val graduatedSemesterValue: String = row.getCell(11).getStringSafe()
        val graduatedSemester: Semester? = runCatching {
            semesterReader.readByString(graduatedSemesterValue.replace(",", "").trim())
        }.getOrNull()
        val parsedMember: Member = basicMemberExcelProcessor.rowToMember(
            row = row,
            departments = departments,
            parts = parts,
            columnMapping = ColumnNumberMapping.GRADUATED_MEMBER,
            state = MemberState.GRADUATED,
            normalizedDepartments = normalizedDepartments,
            normalizedParts = normalizedParts,
            departmentOverrides = departmentOverrides,
        )

        val oldMember = memberReader.readByStudentIdOrNull(parsedMember.studentId)
        if (oldMember == null) {
            // 졸업 학기를 파싱할 수 없으면, 현재 날짜 기준으로 이전 학기를 사용
            if (graduatedSemester != null) {
                memberWriter.writeMemberWithGraduatedState(parsedMember, graduatedSemester)
            } else {
                memberWriter.writeMemberWithGraduatedState(parsedMember, LocalDate.now())
            }
            return
        }

        val patchedMember = basicMemberExcelProcessor.mergeForPatch(oldMember, parsedMember)
        if (oldMember.state == MemberState.GRADUATED) {
            patchedMember.updateState(MemberState.GRADUATED, oldMember.stateUpdatedTime)
            val currentGraduatedMember: GraduatedMember = memberReader.readGraduatedByMemberId(patchedMember.id!!)
            val newActivePeriod =
                if (graduatedSemester != null) {
                    val previousSemester = semesterReader.read(graduatedSemester.previous())
                    SemesterPeriod(
                        startSemester = currentGraduatedMember.activePeriod.startSemester,
                        endSemester = previousSemester,
                    )
                } else {
                    currentGraduatedMember.activePeriod
                }
            val updatedGraduatedMember = GraduatedMember(
                id = currentGraduatedMember.id,
                member = patchedMember,
                activePeriod = newActivePeriod,
                isAdvisorDesired = currentGraduatedMember.isAdvisorDesired,
            )
            memberWriter.update(updatedGraduatedMember)
            return
        }

        patchedMember.updateState(MemberState.GRADUATED, Instant.now())

        if (oldMember.state == MemberState.ACTIVE) {
            memberWriter.deleteFromActiveMember(patchedMember)
        }

        if (oldMember.state == MemberState.INACTIVE) {
            memberWriter.deleteFromInactiveMember(patchedMember)
        }

        if (oldMember.state == MemberState.WITHDRAWN) {
            memberWriter.deleteFromWithdrawnMember(patchedMember)
        }

        if (oldMember.state == MemberState.COMPLETED) {
            memberWriter.deleteFromCompletedMember(patchedMember)
        }

        if (graduatedSemester != null) {
            memberWriter.writeMemberWithGraduatedState(patchedMember, graduatedSemester)
        } else {
            memberWriter.writeMemberWithGraduatedState(patchedMember, LocalDate.now())
        }
    }
}
