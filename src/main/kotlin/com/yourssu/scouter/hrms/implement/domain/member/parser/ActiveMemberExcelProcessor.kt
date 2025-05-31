package com.yourssu.scouter.hrms.implement.domain.member.parser

import com.yourssu.scouter.common.implement.domain.department.Department
import com.yourssu.scouter.common.implement.domain.part.Part
import com.yourssu.scouter.hrms.business.support.utils.NicknameConverter
import com.yourssu.scouter.hrms.implement.domain.member.Member
import com.yourssu.scouter.hrms.implement.domain.member.MemberReader
import com.yourssu.scouter.hrms.implement.domain.member.MemberState
import com.yourssu.scouter.hrms.implement.domain.member.MemberWriter
import com.yourssu.scouter.hrms.implement.support.getLocalDateSafe
import com.yourssu.scouter.hrms.implement.support.getStringSafe
import java.time.LocalDate
import java.time.LocalDateTime
import org.apache.poi.ss.usermodel.CellType
import org.apache.poi.ss.usermodel.Sheet
import org.springframework.stereotype.Component

@Component
class ActiveMemberExcelProcessor(
    private val memberPartRoleResolver: MemberPartRoleResolver,
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
            if (row.getCell(0) == null || row.getCell(0).cellType == CellType.BLANK) {
                break
            }
            try {
                val partRoleName: String = row.getCell(1).getStringSafe()
                val nickname = row.getCell(3).getStringSafe()
                val departmentName: String = row.getCell(6).getStringSafe()
                val birthDate: LocalDate = row.getCell(7).getLocalDateSafe(LocalDate.ofEpochDay(0))
                    ?: throw RuntimeException("'${row.getCell(7).getStringSafe()}'를 날짜로 변환할 수 없습니다")
                val joinDate: LocalDate = row.getCell(9).getLocalDateSafe(LocalDate.ofEpochDay(0))
                    ?: throw RuntimeException("'${row.getCell(9).getStringSafe()}'를 날짜로 변환할 수 없습니다")
                val isMembershipFeePaid: Boolean = row.getCell(10).getStringSafe().equals("o", true)

                val department: Department? = departments[departmentName]

                val partRoles: MemberPartAndRoles = memberPartRoleResolver.toPartAndRoles(partRoleName, parts)
                if (partRoles.isEmpty()) {
                    errorMessages.add("액티브 시트 ${index + 2}번째 줄 오류: ${partRoleName}에 해당하는 파트/역할을 찾을 수 없습니다")
                    continue
                }
                if (department == null) {
                    errorMessages.add("액티브 시트 ${index + 2}번째 줄 오류: 학과 '${departmentName}'를 찾을 수 없음")
                    continue
                }
                val studentId: String = row.getCell(8).getStringSafe()
                val member: Member? = memberReader.readByStudentIdOrNull(studentId)
                if (member == null) {
                    val newMember = Member(
                        name = row.getCell(2).getStringSafe(),
                        email = row.getCell(4).getStringSafe(),
                        phoneNumber = row.getCell(5).getStringSafe(),
                        birthDate = birthDate,
                        department = department,
                        studentId = row.getCell(8).getStringSafe(),
                        parts = partRoles.getParts().toSortedSet(),
                        role = partRoles.getRole(),
                        nicknameEnglish = NicknameConverter.extractNickname(nickname),
                        nicknameKorean = NicknameConverter.extractPronunciation(nickname),
                        state = MemberState.ACTIVE,
                        joinDate = joinDate,
                        note = row.getCell(11).getStringSafe(),
                        stateUpdatedTime = LocalDateTime.now(),
                    )

                    memberWriter.writeMemberWithActiveStatus(
                        member = newMember,
                        isMembershipFeePaid = isMembershipFeePaid,
                    )
                } else {
                    val updateMember = Member(
                        id = member.id,
                        name = row.getCell(2).getStringSafe(),
                        email = row.getCell(4).getStringSafe(),
                        phoneNumber = row.getCell(5).getStringSafe(),
                        birthDate = birthDate,
                        department = department,
                        studentId = row.getCell(8).getStringSafe(),
                        parts = partRoles.getParts().toSortedSet(),
                        role = partRoles.getRole(),
                        nicknameEnglish = NicknameConverter.extractNickname(nickname),
                        nicknameKorean = NicknameConverter.extractPronunciation(nickname),
                        state = MemberState.ACTIVE,
                        joinDate = joinDate,
                        note = row.getCell(11)?.getStringSafe() ?: "",
                        stateUpdatedTime = member.stateUpdatedTime,
                    )

                    if (member.state != MemberState.ACTIVE) {
                        updateMember.updateState(
                            newState = MemberState.ACTIVE,
                            stateUpdatedTime = LocalDateTime.now(),
                        )
                    }

                    memberWriter.writeMemberWithActiveStatus(
                        member = updateMember,
                        isMembershipFeePaid = isMembershipFeePaid,
                    )
                }
            } catch (e: Exception) {
                errorMessages.add("액티브 시트 ${index + 2}번째 줄 오류: ${e.message}")
            }
        }

        return ErrorMessages(errorMessages)
    }
}
