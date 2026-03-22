package com.yourssu.scouter.hrms.implement.domain.member.parser

import com.yourssu.scouter.common.implement.domain.department.Department
import com.yourssu.scouter.common.implement.domain.part.Part
import com.yourssu.scouter.hrms.business.support.utils.NicknameConverter
import com.yourssu.scouter.hrms.implement.domain.member.ActiveMember
import com.yourssu.scouter.hrms.implement.domain.member.Member
import com.yourssu.scouter.hrms.implement.domain.member.MemberReader
import com.yourssu.scouter.hrms.implement.domain.member.MemberRole
import com.yourssu.scouter.hrms.implement.domain.member.MemberState
import com.yourssu.scouter.hrms.implement.domain.member.MemberWriter
import com.yourssu.scouter.hrms.implement.support.AliasMappingUtils
import com.yourssu.scouter.hrms.implement.support.MemberParseMappingData
import com.yourssu.scouter.hrms.implement.support.exception.ExcelParseFailedException
import com.yourssu.scouter.hrms.implement.support.getFlexibleLocalDateSafe
import com.yourssu.scouter.hrms.implement.support.getFormattedStringSafe
import com.yourssu.scouter.hrms.implement.support.getStringSafe
import com.yourssu.scouter.hrms.implement.support.isNullOrBlank
import com.yourssu.scouter.hrms.implement.support.isStrikethrough
import org.apache.poi.ss.usermodel.Row
import org.apache.poi.ss.usermodel.Sheet
import org.springframework.stereotype.Component
import java.time.Instant
import java.time.LocalDate
import java.util.UUID

/**
 * 지원자 합격시트 전용 파서.
 * - 첫 행은 헤더, 2행부터 데이터.
 * - 컬럼: 0=일시, 1=지원 포지션, 2=이름, 3=닉네임, 4=소속, 5=전화번호, 6=생년월일, 7=학번, 8=재/휴학여부, 9=학년, 10 이후 무시.
 * - 이름 셀에 취소선이 그어진 행은 스킵.
 * - 시트 내 중복/DB 중복 시 에러 없이 기존 멤버를 패치(업데이트). 전화번호 또는 학번으로 기존 멤버를 찾으면 해당 멤버를 시트 값으로 갱신하고 ACTIVE 유지.
 * - 새 멤버는 ACTIVE로 추가, 가입일은 업로드 시 입력값.
 */
@Component
class ApplicantPassSheetProcessor(
    private val memberPartRoleResolver: MemberPartRoleResolver,
    private val mappingData: MemberParseMappingData,
    private val memberReader: MemberReader,
    private val memberWriter: MemberWriter,
) {

    companion object {
        private const val COL_TIMESTAMP = 0
        private const val COL_POSITION = 1
        private const val COL_NAME = 2
        private const val COL_NICKNAME = 3
        private const val COL_DEPARTMENT = 4
        private const val COL_PHONE = 5
        private const val COL_BIRTH_DATE = 6
        private const val COL_STUDENT_ID = 7
        private const val COL_LEAVE_STATUS = 8
        private const val COL_GRADE = 9

        private val TEMP_BIRTHDAY_FOR_NULL = LocalDate.of(1970, 12, 31)
        private val GRADE_DIGITS_REGEX = Regex("""\d+""")
    }

    /**
     * 시트에서 alias+DB로 해석 불가한 학과명(raw) 목록을 수집. 매핑 폼에 띄우기 위해 사용.
     */
    fun collectUnknownDepartments(
        sheet: Sheet,
        departments: Map<String, Department>,
    ): List<String> {
        val normalizedDepartments = departments.entries.associate { AliasMappingUtils.normalizeKey(it.key) to it.value }
        return sheet.iterator().asSequence()
            .drop(1)
            .filter { row -> row.getCell(COL_NAME).isNullOrBlank().not() }
            .filter { row -> row.getCell(COL_NAME).isStrikethrough().not() }
            .mapNotNull { row ->
                val raw = row.getCell(COL_DEPARTMENT).getStringSafe().trim()
                if (raw.isBlank()) return@mapNotNull null
                val aliasOrOriginal = AliasMappingUtils.toCanonicalOrSelf(raw, mappingData.departmentAliases)
                val found = departments[aliasOrOriginal] != null ||
                    normalizedDepartments[AliasMappingUtils.normalizeKey(aliasOrOriginal)] != null
                if (found) null else raw
            }
            .distinct()
            .sorted()
            .toList()
    }

    fun parse(
        sheet: Sheet,
        departments: Map<String, Department>,
        parts: Map<String, Part>,
        joinDate: LocalDate,
        departmentOverrides: Map<String, String> = emptyMap(),
    ): ErrorMessages {
        val errors = mutableListOf<String>()
        val normalizedDepartments = departments.entries.associate { AliasMappingUtils.normalizeKey(it.key) to it.value }
        val normalizedParts = parts.entries.associate { AliasMappingUtils.normalizeKey(it.key) to it.value }

        // 1) 유효 행 수집 (취소선/빈 행 제외). 시트 내 중복은 에러 없이 나중 행이 이전 행을 덮어쓰는 패치로 처리.
        val dataRows = sheet.iterator().asSequence()
            .drop(1)
            .withIndex()
            .map { (index, row) -> IndexedRow(index + 2, row) }
            .filter { (_, row) -> row.getCell(COL_NAME).isNullOrBlank().not() }
            .filter { (_, row) -> row.getCell(COL_NAME).isStrikethrough().not() }
            .map { (line, row) ->
                val phone = row.getCell(COL_PHONE).getStringSafe().trim()
                val studentId = row.getCell(COL_STUDENT_ID).getStringSafe().trim()
                DataRow(line, phone, studentId, row)
            }
            .toList()

        // 2) 각 행: 기존 멤버(전화번호 또는 학번) 있으면 패치, 없으면 신규 추가
        for (dataRow in dataRows) {
            val rowIndex = dataRow.line
            val phone = dataRow.phone
            val studentId = dataRow.studentId
            val row = dataRow.row
            runCatching {
                val oldMember = (if (phone.isNotBlank()) memberReader.readByPhoneNumberOrNull(phone) else null)
                    ?: (if (studentId.isNotBlank()) memberReader.readByStudentIdOrNull(studentId) else null)
                val parsedFromRow = rowToMember(
                    row = row,
                    departments = departments,
                    parts = parts,
                    normalizedDepartments = normalizedDepartments,
                    normalizedParts = normalizedParts,
                    joinDate = joinDate,
                    phoneNumber = phone.ifBlank { "" },
                    studentId = studentId.ifBlank { "" },
                    departmentOverrides = departmentOverrides,
                )
                if (oldMember == null) {
                    val toInsert = ensurePlaceholderIdentifiers(parsedFromRow)
                    memberWriter.writeMemberWithActiveStatus(toInsert.member, isMembershipFeePaid = false, grade = toInsert.grade, isOnLeave = toInsert.isOnLeave)
                } else {
                    patchAndWriteActive(oldMember, parsedFromRow)
                }
            }.onFailure { e ->
                // 이미 다른 트랜잭션에서 member_part가 정리되었거나 오래된 상태인 경우는
                // 취소선 행과 비슷하게 "그냥 스킵"하도록 오류 리스트에 추가하지 않는다.
                val msg = e.message ?: ""
                val isStaleMemberPart =
                    msg.contains("Row was updated or deleted by another transaction") &&
                        msg.contains("MemberPartEntity")
                if (!isStaleMemberPart) {
                    errors.add("지원자 합격시트 ${rowIndex}행 오류: ${e.message}")
                }
            }
        }

        return ErrorMessages(errors)
    }

    private fun rowToMember(
        row: Row,
        departments: Map<String, Department>,
        parts: Map<String, Part>,
        normalizedDepartments: Map<String, Department>,
        normalizedParts: Map<String, Part>,
        joinDate: LocalDate,
        phoneNumber: String,
        studentId: String,
        departmentOverrides: Map<String, String>,
    ): ParsedApplicantRow {
        val name = row.getCell(COL_NAME).getStringSafe()
        require(name.isNotBlank()) { "이름이 비어 있습니다." }

        val birthCell = row.getCell(COL_BIRTH_DATE)
        val birthDate = birthCell.getFlexibleLocalDateSafe(null)
            ?: throw ExcelParseFailedException("생년월일 '${birthCell.getFormattedStringSafe()}'를 날짜로 변환할 수 없습니다.")

        val departmentNameRaw = row.getCell(COL_DEPARTMENT).getStringSafe().trim()
        val canonicalName = departmentOverrides[departmentNameRaw]
            ?: AliasMappingUtils.toCanonicalOrSelf(departmentNameRaw, mappingData.departmentAliases)
        val department = departments[canonicalName]
            ?: normalizedDepartments[AliasMappingUtils.normalizeKey(canonicalName)]
            ?: throw ExcelParseFailedException(
                "학과 '$departmentNameRaw'를 찾을 수 없습니다. (매핑을 선택했는지 확인하세요.)"
            )

        val partRoleName = row.getCell(COL_POSITION).getStringSafe()
        val partRoles = memberPartRoleResolver.toPartAndRoles(
            roleCell = partRoleName,
            parts = parts,
            normalizedParts = normalizedParts,
        )
        if (partRoles.isEmpty()) {
            throw ExcelParseFailedException("'$partRoleName'에 해당하는 파트/역할을 찾을 수 없습니다.")
        }

        val nicknameRaw = row.getCell(COL_NICKNAME).getStringSafe().trim()
        val (nicknameEnglish, nicknameKorean) = if (nicknameRaw.contains("(")) {
            Pair(
                NicknameConverter.extractNickname(nicknameRaw),
                NicknameConverter.extractPronunciation(nicknameRaw),
            )
        } else {
            Pair(if (nicknameRaw.isNotBlank()) nicknameRaw else name, "")
        }

        val sanitizedStudentId = studentId.ifBlank { "UNKNOWN" }.replace(" ", "")
        val email = "pass-$sanitizedStudentId@scouter-placeholder"

        val member = Member(
            name = name,
            email = email,
            phoneNumber = phoneNumber,
            birthDate = birthDate,
            department = department,
            studentId = studentId,
            parts = partRoles.getParts().toSortedSet(),
            role = partRoles.getRole(),
            nicknameEnglish = nicknameEnglish,
            nicknameKorean = nicknameKorean,
            state = MemberState.ACTIVE,
            joinDate = joinDate,
            note = "",
            stateUpdatedTime = Instant.now(),
        )
        val leaveRaw = row.getCell(COL_LEAVE_STATUS).getStringSafe().trim()
        val gradeRaw = row.getCell(COL_GRADE).getStringSafe().trim()
        return ParsedApplicantRow(
            member = member,
            grade = parseGrade(gradeRaw),
            isOnLeave = parseLeaveStatus(leaveRaw),
        )
    }

    /**
     * 재/휴학여부 문자열 → Boolean? (비어 있으면 null, 휴학→true, 재학→false).
     * "비휴학", "미휴학", "무휴학" 등은 문자열에 "휴학"이 포함되어도 재학(휴학 아님)으로 본다.
     */
    private fun parseLeaveStatus(raw: String): Boolean? {
        if (raw.isBlank()) return null
        val normalized = raw.trim().lowercase()
        if (normalized.contains("비휴학") || normalized.contains("미휴학") || normalized.contains("무휴학")) {
            return false
        }
        if (normalized.contains("휴학")) return true
        if (normalized.contains("재학")) return false
        return null
    }

    /**
     * 학년 문자열 → Int? (비어 있으면 null).
     * "3학년", "3 학년" 등 숫자+접미사 형식은 앞쪽 연속 숫자만 추출한다. 순수 숫자도 그대로 파싱.
     */
    private fun parseGrade(raw: String): Int? {
        val trimmed = raw.trim()
        if (trimmed.isBlank()) return null
        trimmed.toIntOrNull()?.let { return it }
        return GRADE_DIGITS_REGEX.find(trimmed)?.value?.toIntOrNull()
    }

    /** 신규 추가 시 학번/이메일이 비어 있으면 플레이스홀더로 채워 DB unique 제약을 만족시킨다. */
    private fun ensurePlaceholderIdentifiers(parsed: ParsedApplicantRow): ParsedApplicantRow {
        val member = parsed.member
        val sanitizedPhone = member.phoneNumber.replace("-", "").replace(" ", "")
        val suffix = when {
            sanitizedPhone.isNotBlank() -> sanitizedPhone
            member.studentId.isNotBlank() -> member.studentId.replace(" ", "")
            else -> "UNKNOWN-${UUID.randomUUID().toString().take(8)}"
        }
        val studentId = member.studentId.ifBlank { "PASS-$suffix" }
        val email = if (member.email.isBlank() || member.email.contains("UNKNOWN")) "pass-$suffix@scouter-placeholder" else member.email
        val phoneNumber = member.phoneNumber.ifBlank { "NO-PHONE-$suffix".take(30) }
        val fixedMember = Member(
            id = member.id,
            name = member.name,
            email = email,
            phoneNumber = phoneNumber,
            birthDate = member.birthDate,
            department = member.department,
            studentId = studentId,
            parts = member.parts,
            role = member.role,
            nicknameEnglish = member.nicknameEnglish,
            nicknameKorean = member.nicknameKorean,
            state = member.state,
            joinDate = member.joinDate,
            note = member.note,
            stateUpdatedTime = member.stateUpdatedTime,
        )
        return ParsedApplicantRow(member = fixedMember, grade = parsed.grade, isOnLeave = parsed.isOnLeave)
    }

    /** 기존 멤버를 시트 값으로 패치한 뒤 ACTIVE로 유지(또는 복귀)한다. 식별자(email/phone/studentId)는 기존 값 유지. */
    private fun patchAndWriteActive(oldMember: Member, parsedFromRow: ParsedApplicantRow) {
        val p = parsedFromRow.member
        val patched = Member(
            id = oldMember.id,
            name = p.name,
            email = oldMember.email,
            phoneNumber = oldMember.phoneNumber,
            birthDate = p.birthDate,
            department = p.department,
            studentId = oldMember.studentId,
            parts = p.parts,
            role = p.role,
            nicknameEnglish = p.nicknameEnglish,
            nicknameKorean = p.nicknameKorean,
            state = MemberState.ACTIVE,
            joinDate = p.joinDate,
            note = oldMember.note,
            stateUpdatedTime = Instant.now(),
        )
        val grade = parsedFromRow.grade
        val isOnLeave = parsedFromRow.isOnLeave
        if (oldMember.state == MemberState.ACTIVE) {
            patched.updateState(MemberState.ACTIVE, oldMember.stateUpdatedTime)
            val currentActive = memberReader.readActiveByMemberId(patched.id!!)
            memberWriter.update(ActiveMember(id = currentActive.id, member = patched, isMembershipFeePaid = currentActive.isMembershipFeePaid, grade = grade, isOnLeave = isOnLeave))
            return
        }
        patched.updateState(MemberState.ACTIVE, Instant.now())
        when (oldMember.state) {
            MemberState.INACTIVE -> memberWriter.deleteFromInactiveMember(patched)
            MemberState.GRADUATED -> memberWriter.deleteFromGraduatedMember(patched)
            MemberState.COMPLETED -> memberWriter.deleteFromCompletedMember(patched)
            MemberState.WITHDRAWN -> memberWriter.deleteFromWithdrawnMember(patched)
            else -> { }
        }
        memberWriter.writeMemberWithActiveStatus(patched, isMembershipFeePaid = false, grade = grade, isOnLeave = isOnLeave)
    }

    private data class ParsedApplicantRow(
        val member: Member,
        val grade: Int?,
        val isOnLeave: Boolean?,
    )

    private data class DataRow(
        val line: Int,
        val phone: String,
        val studentId: String,
        val row: Row,
    )

    private data class IndexedRow(
        val line: Int,
        val row: Row,
    )
}
