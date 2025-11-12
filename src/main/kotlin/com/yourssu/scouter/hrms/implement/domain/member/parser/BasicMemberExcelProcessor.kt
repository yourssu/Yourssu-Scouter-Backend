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
import java.time.LocalDateTime
import org.apache.poi.ss.usermodel.Row
import org.springframework.stereotype.Component

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

    fun rowToMember(
        row: Row,
        departments: Map<String, Department>,
        parts: Map<String, Part>,
        columnMapping: ColumnNumberMapping,
        state: MemberState,
        normalizedDepartments: Map<String, Department>? = null,
        normalizedParts: Map<String, Part>? = null,
    ): Member {
        val name = row.getCell(columnMapping.name).getStringSafe()
        val email = row.getCell(columnMapping.email).getStringSafe()
        val phoneNumber = row.getCell(columnMapping.phoneNumber).getStringSafe()
        val birthDate: LocalDate = row.getCell(columnMapping.birthDate).getLocalDateSafe(TEMP_BIRTHDAY_FOR_NULL)
            ?: throw ExcelParseFailedException("생일 '${row.getCell(columnMapping.birthDate).getStringSafe()}'를 날짜로 변환할 수 없습니다")
        val departmentNameRaw = row.getCell(columnMapping.departmentName).getStringSafe()
        val aliasOrOriginalName = AliasMappingUtils.toCanonicalOrSelf(departmentNameRaw, mappingData.departmentAliases)

        val departmentDirect = departments[aliasOrOriginalName]
        val department: Department = if (departmentDirect != null) {
            departmentDirect
        } else {
            val normalizedMap: Map<String, Department> =
                normalizedDepartments ?: departments.entries.associate { AliasMappingUtils.normalizeKey(it.key) to it.value }
            normalizedMap[AliasMappingUtils.normalizeKey(aliasOrOriginalName)]
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
        val nickname = if (pronunciationRaw.isNotBlank()) "$nicknameRaw($pronunciationRaw)" else nicknameRaw
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
            nicknameEnglish = NicknameConverter.extractNickname(nickname),
            nicknameKorean = NicknameConverter.extractPronunciation(nickname),
            state = state,
            joinDate = joinDate,
            note = note,
            stateUpdatedTime = LocalDateTime.now(),
        )
    }
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
