package com.yourssu.scouter.hrms.implement.domain.member.parser

import com.yourssu.scouter.common.implement.domain.department.Department
import com.yourssu.scouter.common.implement.domain.part.Part
import com.yourssu.scouter.hrms.business.support.utils.NicknameConverter
import com.yourssu.scouter.hrms.implement.domain.member.Member
import com.yourssu.scouter.hrms.implement.domain.member.MemberState
import com.yourssu.scouter.hrms.implement.support.exception.ExcelParseFailedException
import com.yourssu.scouter.hrms.implement.support.getLocalDateSafe
import com.yourssu.scouter.hrms.implement.support.getStringSafe
import com.yourssu.scouter.hrms.implement.support.AliasMappingUtils
import com.yourssu.scouter.hrms.implement.support.MemberParseMappingData
import java.time.LocalDate
import org.apache.poi.ss.usermodel.Row
import org.apache.poi.ss.usermodel.Sheet
import org.springframework.stereotype.Component
import java.time.Instant

@Component
class BasicMemberExcelProcessor(
    private val memberPartRoleResolver: MemberPartRoleResolver,
    private val mappingData: MemberParseMappingData,
) {

    companion object {
        private val TEMP_BIRTHDAY_FOR_NULL = LocalDate.ofEpochDay(0)
        private val TEMP_JOIN_DATE_FOR_NULL = LocalDate.of(2099, 9, 1)

        // 별칭은 설정 파일에서 주입받아 사용
    }

    /**
     * 시트에서 alias+DB로 해석 불가한 학과명(raw) 목록을 수집. 동일 오타는 한 번만 포함.
     */
    fun collectUnknownDepartments(
        sheet: Sheet,
        departments: Map<String, Department>,
        columnMapping: ColumnNumberMapping,
    ): List<String> {
        val normalizedDepartments = departments.entries.associate { AliasMappingUtils.normalizeKey(it.key) to it.value }
        return sheet.iterator().asSequence()
            .drop(1)
            .mapNotNull { row ->
                val raw = row.getCell(columnMapping.departmentName).getStringSafe().trim()
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

    fun rowToMember(
        row: Row,
        departments: Map<String, Department>,
        parts: Map<String, Part>,
        columnMapping: ColumnNumberMapping,
        state: MemberState,
        normalizedDepartments: Map<String, Department>? = null,
        normalizedParts: Map<String, Part>? = null,
        departmentOverrides: Map<String, String> = emptyMap(),
    ): Member {
        val name = row.getCell(columnMapping.name).getStringSafe()
        val email = row.getCell(columnMapping.email).getStringSafe()
        val phoneNumber = row.getCell(columnMapping.phoneNumber).getStringSafe()
        val birthDate: LocalDate = row.getCell(columnMapping.birthDate).getLocalDateSafe(TEMP_BIRTHDAY_FOR_NULL)
            ?: throw ExcelParseFailedException("생일 '${row.getCell(columnMapping.birthDate).getStringSafe()}'를 날짜로 변환할 수 없습니다")
        val departmentNameRaw = row.getCell(columnMapping.departmentName).getStringSafe().trim()
        val canonicalName = departmentOverrides[departmentNameRaw]
            ?: AliasMappingUtils.toCanonicalOrSelf(departmentNameRaw, mappingData.departmentAliases)
        val departmentDirect = departments[canonicalName]
        val department: Department = if (departmentDirect != null) {
            departmentDirect
        } else {
            val normalizedMap: Map<String, Department> =
                normalizedDepartments ?: departments.entries.associate { AliasMappingUtils.normalizeKey(it.key) to it.value }
            normalizedMap[AliasMappingUtils.normalizeKey(canonicalName)]
                ?: throw ExcelParseFailedException("학과 '${departmentNameRaw}'를 찾을 수 없음")
        }
        val studentId = row.getCell(columnMapping.studentId).getStringSafe()
        val partRoleName = row.getCell(columnMapping.partRoleName).getStringSafe()
        val partRoles: MemberPartAndRoles = memberPartRoleResolver.toPartAndRoles(
            roleCell = partRoleName,
            parts = parts,
            normalizedParts = normalizedParts,
        )
        if (partRoles.isEmpty()) {
            throw ExcelParseFailedException("${partRoleName}에 해당하는 파트/역할을 찾을 수 없습니다")
        }
        val nicknameRaw = row.getCell(columnMapping.nickname).getStringSafe()
        val pronunciationRaw = columnMapping.pronunciation?.let { row.getCell(it).getStringSafe() } ?: ""
        val (nicknameEnglish, nicknameKorean) = run {
            val combined = if (pronunciationRaw.isNotBlank()) "$nicknameRaw($pronunciationRaw)" else nicknameRaw
            // 1) 비어 있으면: 영어=이름, 발음=""
            if (combined.isBlank()) {
                name to ""
            } else if (!combined.contains("(") || !combined.contains(")")) {
                // 2) 괄호 없는 경우: 영어=그대로, 발음=""
                combined to ""
            } else {
                // 3) 형식이 이상해도 에러로 막지 않고 최선으로 분해
                runCatching {
                    NicknameConverter.extractNickname(combined) to NicknameConverter.extractPronunciation(combined)
                }.getOrElse {
                    val before = combined.substringBefore("(")
                    val inside = combined.substringAfter("(").substringBefore(")")
                    before to inside
                }
            }
        }
        val joinDate = row.getCell(columnMapping.joinDate).getLocalDateSafe(TEMP_JOIN_DATE_FOR_NULL)
            ?: throw IllegalArgumentException("'가입일 ${row.getCell(columnMapping.joinDate).getStringSafe()}'를 날짜로 변환할 수 없습니다")
        val note = row.getCell(columnMapping.note).getStringSafe()

        return Member(
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
            state = state,
            joinDate = joinDate,
            note = note,
            stateUpdatedTime = Instant.now(),
        )
    }

    /**
     * 인포시트 패치 시: 학번·이메일은 DB(oldMember) 값 유지, 나머지는 시트(parsedMember) 값으로 갱신.
     */
    fun mergeForPatch(oldMember: Member, parsedMember: Member): Member = Member(
        id = oldMember.id,
        name = parsedMember.name,
        email = oldMember.email,
        phoneNumber = parsedMember.phoneNumber,
        birthDate = parsedMember.birthDate,
        department = parsedMember.department,
        studentId = oldMember.studentId,
        parts = parsedMember.parts,
        role = parsedMember.role,
        nicknameEnglish = parsedMember.nicknameEnglish,
        nicknameKorean = parsedMember.nicknameKorean,
        state = parsedMember.state,
        joinDate = parsedMember.joinDate,
        note = parsedMember.note,
        stateUpdatedTime = parsedMember.stateUpdatedTime,
    )
}

data class ColumnNumberMapping(
    val name: Int,
    val email: Int,
    val phoneNumber: Int,
    val birthDate: Int,
    val departmentName: Int,
    val studentId: Int,
    val partRoleName: Int,
    val nickname: Int,
    val pronunciation: Int? = null,
    val joinDate: Int,
    val note: Int,
) {

    companion object {
        fun forState(state: com.yourssu.scouter.hrms.implement.domain.member.MemberState): ColumnNumberMapping =
            when (state) {
                com.yourssu.scouter.hrms.implement.domain.member.MemberState.ACTIVE -> ACTIVE_MEMBER
                com.yourssu.scouter.hrms.implement.domain.member.MemberState.INACTIVE -> INACTIVE_MEMBER
                com.yourssu.scouter.hrms.implement.domain.member.MemberState.GRADUATED -> GRADUATED_MEMBER
                com.yourssu.scouter.hrms.implement.domain.member.MemberState.WITHDRAWN -> WITHDRAWN_MEMBER
            }

        val ACTIVE_MEMBER: ColumnNumberMapping = ColumnNumberMapping(
            name = 2,
            email = 5,
            phoneNumber = 6,
            birthDate = 8,
            departmentName = 7,
            studentId = 9,
            partRoleName = 1,
            nickname = 3,
            pronunciation = 4,
            joinDate = 10,
            note = 12,
        )

        val INACTIVE_MEMBER: ColumnNumberMapping = ColumnNumberMapping(
            name = 1,
            email = 4,
            phoneNumber = 5,
            birthDate = 7,
            departmentName = 6,
            studentId = 8,
            partRoleName = 0,
            nickname = 2,
            pronunciation = 3,
            joinDate = 9,
            note = 10,
        )

        val GRADUATED_MEMBER: ColumnNumberMapping = ColumnNumberMapping(
            name = 2,
            email = 5,
            phoneNumber = 4,
            birthDate = 7,
            departmentName = 6,
            studentId = 8,
            partRoleName = 1,
            nickname = 3,
            pronunciation = 4,
            joinDate = 9,
            note = 10,
        )


        val WITHDRAWN_MEMBER: ColumnNumberMapping = ColumnNumberMapping(
            name = 2,
            email = 5,
            phoneNumber = 6,
            birthDate = 8,
            departmentName = 7,
            studentId = 9,
            partRoleName = 1,
            nickname = 3,
            pronunciation = 4,
            joinDate = 10,
            note = 13,
        )
    }
}
