package com.yourssu.scouter.hrms.implement.domain.member.parser

import com.yourssu.scouter.common.implement.domain.department.Department
import com.yourssu.scouter.common.implement.domain.part.Part
import com.yourssu.scouter.hrms.implement.domain.member.Member
import com.yourssu.scouter.hrms.implement.domain.member.MemberReader
import com.yourssu.scouter.hrms.implement.domain.member.MemberState
import com.yourssu.scouter.hrms.implement.domain.member.MemberWriter
import com.yourssu.scouter.hrms.implement.domain.member.WithdrawnMember
import com.yourssu.scouter.hrms.implement.support.AliasMappingUtils
import com.yourssu.scouter.hrms.implement.support.exception.ExcelParseFailedException
import com.yourssu.scouter.hrms.implement.support.getFlexibleLocalDateSafe
import com.yourssu.scouter.hrms.implement.support.getStringSafe
import com.yourssu.scouter.hrms.implement.support.isNullOrBlank
import org.apache.poi.ss.usermodel.Row
import org.apache.poi.ss.usermodel.Sheet
import org.springframework.stereotype.Component
import java.time.Instant
import java.time.LocalDate

@Component
class WithdrawnMemberExcelProcessor(
    private val memberReader: MemberReader,
    private val memberWriter: MemberWriter,
    private val memberPartRoleResolver: MemberPartRoleResolver,
) : MemberExcelProcessor {

    companion object {
        /** 탈퇴일 셀을 날짜로 읽을 수 없을 때 행은 저장하고 일자만 폴백 */
        private val TEMP_WITHDRAWN_DATE_FOR_UNPARSABLE = LocalDate.of(2099, 12, 31)
    }

    override fun supportingState(): MemberState {
        return MemberState.WITHDRAWN
    }

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

        for ((index, row) in rows.withIndex()) {
            if (row.getCell(0).isNullOrBlank()) {
                break
            }

            runCatching {
                parseRow(row, departments, parts, normalizedDepartments, departmentOverrides)
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
        departmentOverrides: Map<String, String> = emptyMap(),
    ) {
        val name = row.getCell(0).getStringSafe()
        val nicknameRaw = row.getCell(1).getStringSafe()
        // 탈퇴 시트의 2열은 학과(전공)가 아니라 부서/파트 이름이다.
        val partNameRaw = row.getCell(2).getStringSafe().trim()
        val withdrawnDateCell = row.getCell(3)
        val noteFromSheet = row.getCell(4).getStringSafe()

        if (name.isBlank()) {
            throw ExcelParseFailedException("이름이 비어 있습니다.")
        }

        if (partNameRaw.isBlank()) {
            throw ExcelParseFailedException("부서(파트)가 비어 있습니다.")
        }

        val withdrawnDate: LocalDate =
            withdrawnDateCell.getFlexibleLocalDateSafe(null) ?: TEMP_WITHDRAWN_DATE_FOR_UNPARSABLE

        // 이름 기준으로 액티브/비액티브/졸업/수료/탈퇴 멤버 검색 후, 부서(파트)로 1명으로 좁힌다.
        val activeCandidates = memberReader.searchAllActiveByNameOrNickname(name).map { it.member }
        val inactiveCandidates = memberReader.searchAllInactiveByNameOrNickname(name).map { it.member }
        val graduatedCandidates = memberReader.searchAllGraduatedByNameOrNickname(name).map { it.member }
        val completedCandidates = memberReader.searchAllCompletedByNameOrNickname(name).map { it.member }
        val withdrawnCandidates = memberReader.searchAllWithdrawnByNameOrNickname(name).map { it.member }

        val baseCandidates: List<Member> =
            activeCandidates + inactiveCandidates + graduatedCandidates + completedCandidates + withdrawnCandidates
        val resolvedParts = memberPartRoleResolver.toPartAndRoles(partNameRaw, parts).getParts()
        val candidates: List<Member> =
            baseCandidates.filter { member ->
                member.parts.any { it in resolvedParts } ||
                    member.parts.any { part -> part.name == partNameRaw }
            }

        val nicknameForMessage =
            nicknameRaw.takeIf { it.isNotBlank() }?.let { " / 닉네임 '$it'" }.orEmpty()

        if (candidates.isEmpty()) {
            throw ExcelParseFailedException("이름 '$name'$nicknameForMessage 에 해당하는 멤버를 찾을 수 없습니다 (부서: '$partNameRaw').")
        }

        if (candidates.size > 1) {
            throw ExcelParseFailedException("이름 '$name'$nicknameForMessage 에 해당하는 멤버가 여러 명입니다. 학번으로 구분이 필요합니다. (부서: '$partNameRaw')")
        }

        val target: Member = candidates.single()

        // 비고 및 탈퇴 일자 메모를 기존 비고에 합친다.
        val withdrawNotePrefix = "탈퇴일자: ${withdrawnDate}"
        val extraNote =
            if (noteFromSheet.isNotBlank()) "$withdrawNotePrefix / $noteFromSheet" else withdrawNotePrefix

        target.note =
            if (target.note.isBlank()) extraNote else "${target.note}\n$extraNote"

        val previousState = target.state
        target.updateState(MemberState.WITHDRAWN, Instant.now())

        if (previousState == MemberState.ACTIVE) {
            memberWriter.deleteFromActiveMember(target)
        }

        if (previousState == MemberState.INACTIVE) {
            memberWriter.deleteFromInactiveMember(target)
        }

        if (previousState == MemberState.GRADUATED) {
            memberWriter.deleteFromGraduatedMember(target)
        }

        if (previousState == MemberState.COMPLETED) {
            memberWriter.deleteFromCompletedMember(target)
        }

        if (previousState == MemberState.WITHDRAWN) {
            memberWriter.deleteFromWithdrawnMember(target)
        }

        memberWriter.writeMemberWithWithdrawnState(target, withdrawnDate)
    }
}
