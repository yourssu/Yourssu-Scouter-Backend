package com.yourssu.scouter.hrms.implement.domain.member.parser

import com.yourssu.scouter.common.implement.domain.department.Department
import com.yourssu.scouter.common.implement.domain.part.Part
import com.yourssu.scouter.hrms.business.support.utils.NicknameConverter
import com.yourssu.scouter.hrms.implement.domain.member.Member
import com.yourssu.scouter.hrms.implement.domain.member.MemberState
import com.yourssu.scouter.hrms.implement.support.exception.ExcelParseFailedException
import com.yourssu.scouter.hrms.implement.support.getLocalDateSafe
import com.yourssu.scouter.hrms.implement.support.getStringSafe
import java.time.LocalDate
import java.time.LocalDateTime
import org.apache.poi.ss.usermodel.Row
import org.springframework.stereotype.Component

@Component
class BasicMemberExcelProcessor(
    private val memberPartRoleResolver: MemberPartRoleResolver,
) {

    companion object {
        private val TEMP_BIRTHDAY_FOR_NULL = LocalDate.ofEpochDay(0)
        private val TEMP_JOIN_DATE_FOR_NULL = LocalDate.of(2099, 9, 1)
    }

    fun rowToMember(
        row: Row,
        departments: Map<String, Department>,
        parts: Map<String, Part>,
        columnMapping: ColumnNumberMapping,
        state: MemberState,
    ): Member {
        val name = row.getCell(columnMapping.name).getStringSafe()
        val email = row.getCell(columnMapping.email).getStringSafe()
        val phoneNumber = row.getCell(columnMapping.phoneNumber).getStringSafe()
        val birthDate: LocalDate = row.getCell(columnMapping.birthDate).getLocalDateSafe(TEMP_BIRTHDAY_FOR_NULL)
            ?: throw ExcelParseFailedException("생일 '${row.getCell(columnMapping.birthDate).getStringSafe()}'를 날짜로 변환할 수 없습니다")
        val departmentName = row.getCell(columnMapping.departmentName).getStringSafe()
        val department = departments[departmentName]
            ?: throw ExcelParseFailedException("학과 '${departmentName}'를 찾을 수 없음")
        val studentId = row.getCell(columnMapping.studentId).getStringSafe()
        val partRoleName = row.getCell(columnMapping.partRoleName).getStringSafe()
        val partRoles: MemberPartAndRoles = memberPartRoleResolver.toPartAndRoles(partRoleName, parts)
        if (partRoles.isEmpty()) {
            throw ExcelParseFailedException("${partRoleName}에 해당하는 파트/역할을 찾을 수 없습니다")
        }
        val nickname = row.getCell(columnMapping.nickname).getStringSafe()
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
    val joinDate: Int,
    val note: Int,
) {

    companion object {
        val ACTIVE_MEMBER: ColumnNumberMapping = ColumnNumberMapping(
            name = 2,
            email = 4,
            phoneNumber = 5,
            birthDate = 7,
            departmentName = 6,
            studentId = 8,
            partRoleName = 1,
            nickname = 3,
            joinDate = 9,
            note = 11,
        )


        val INACTIVE_MEMBER: ColumnNumberMapping = ColumnNumberMapping(
            name = 2,
            email = 4,
            phoneNumber = 5,
            birthDate = 7,
            departmentName = 6,
            studentId = 8,
            partRoleName = 1,
            nickname = 3,
            joinDate = 9,
            note = 10,
        )
    }
}
