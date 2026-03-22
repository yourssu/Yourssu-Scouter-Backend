package com.yourssu.scouter.hrms.implement.domain.member.parser

import com.yourssu.scouter.common.implement.domain.department.Department
import com.yourssu.scouter.common.implement.domain.part.Part
import com.yourssu.scouter.common.implement.domain.semester.Semester
import com.yourssu.scouter.common.implement.domain.semester.SemesterRepository
import com.yourssu.scouter.hrms.implement.domain.member.Member
import com.yourssu.scouter.hrms.implement.domain.member.MemberReader
import com.yourssu.scouter.hrms.implement.domain.member.MemberState
import com.yourssu.scouter.hrms.implement.domain.member.MemberWriter
import com.yourssu.scouter.hrms.implement.support.AliasMappingUtils
import com.yourssu.scouter.hrms.implement.support.exception.ExcelParseFailedException
import com.yourssu.scouter.hrms.implement.support.getFormattedStringSafe
import com.yourssu.scouter.hrms.implement.support.isNullOrBlank
import org.apache.poi.ss.usermodel.Row
import org.apache.poi.ss.usermodel.Sheet
import org.springframework.stereotype.Component
import java.time.Instant

@Component
class CompletionMemberExcelProcessor(
    private val basicMemberExcelProcessor: BasicMemberExcelProcessor,
    private val memberReader: MemberReader,
    private val memberWriter: MemberWriter,
    private val semesterRepository: SemesterRepository,
) : MemberExcelProcessor {

    override fun supportingState(): MemberState = MemberState.COMPLETED

    override fun parse(
        sheet: Sheet,
        departments: Map<String, Department>,
        parts: Map<String, Part>,
        departmentOverrides: Map<String, String>,
        completionSemesterOverrides: Map<String, String>,
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
                parseRow(
                    row,
                    departments,
                    parts,
                    normalizedDepartments,
                    normalizedParts,
                    departmentOverrides,
                    completionSemesterOverrides,
                )
            }.onFailure { e ->
                errorMessages.add("수료 시트 ${index + 2}번째 줄 오류: ${e.message}")
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
        completionSemesterOverrides: Map<String, String>,
    ) {
        val sheetRaw: String = row.getCell(11).getFormattedStringSafe()
        val completionSemester: Semester = resolveCompletionSemester(sheetRaw, completionSemesterOverrides)

        val parsedMember: Member = basicMemberExcelProcessor.rowToMember(
            row = row,
            departments = departments,
            parts = parts,
            columnMapping = ColumnNumberMapping.COMPLETED_MEMBER,
            state = MemberState.COMPLETED,
            normalizedDepartments = normalizedDepartments,
            normalizedParts = normalizedParts,
            departmentOverrides = departmentOverrides,
        )

        val oldMember = memberReader.readByStudentIdOrNull(parsedMember.studentId)
        if (oldMember == null) {
            memberWriter.writeMemberWithCompletedState(parsedMember, completionSemester)
            return
        }

        val patchedMember = basicMemberExcelProcessor.mergeForPatch(oldMember, parsedMember)
        patchedMember.updateState(MemberState.COMPLETED, Instant.now())

        if (oldMember.state != MemberState.COMPLETED) {
            when (oldMember.state) {
                MemberState.ACTIVE -> memberWriter.deleteFromActiveMember(patchedMember)
                MemberState.INACTIVE -> memberWriter.deleteFromInactiveMember(patchedMember)
                MemberState.GRADUATED -> memberWriter.deleteFromGraduatedMember(patchedMember)
                MemberState.WITHDRAWN -> memberWriter.deleteFromWithdrawnMember(patchedMember)
                MemberState.COMPLETED -> {
                    // no-op
                }
            }
        }

        memberWriter.writeMemberWithCompletedState(patchedMember, completionSemester)
    }

    private fun resolveCompletionSemester(sheetRaw: String, overrides: Map<String, String>): Semester {
        val key = sheetRaw.trim()
        resolveLabelToStoredSemester(key)?.let { return it }
        val corrected = overrides[key]?.trim()?.takeIf { it.isNotBlank() }
        if (corrected != null) {
            resolveLabelToStoredSemester(corrected)?.let { return it }
        }
        val sheetHint = key.ifBlank { "(비어 있음)" }
        val mappedHint = corrected ?: "(매핑 없음)"
        throw ExcelParseFailedException(
            "수료 학기를 확인할 수 없습니다. 시트 11열 raw: '$sheetHint', 매핑 입력: '$mappedHint' (yy-s 예: 25-1, DB semester 등록 필요)",
        )
    }

    private fun resolveLabelToStoredSemester(label: String): Semester? {
        val t = label.trim()
        if (t.isBlank()) return null
        val parsed = runCatching { Semester.of(t) }.getOrNull() ?: return null
        return semesterRepository.find(parsed)
    }
}
