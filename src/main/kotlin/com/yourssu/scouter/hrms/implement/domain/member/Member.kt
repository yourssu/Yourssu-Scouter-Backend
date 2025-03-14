package com.yourssu.scouter.hrms.implement.domain.member

import com.yourssu.scouter.common.implement.domain.basetime.BaseTime
import com.yourssu.scouter.common.implement.domain.department.Department
import com.yourssu.scouter.common.implement.domain.part.Part
import com.yourssu.scouter.common.implement.domain.semester.Semester
import com.yourssu.scouter.hrms.business.support.utils.MemberRoleConverter
import com.yourssu.scouter.hrms.implement.support.exception.IllegalMemberException
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.SortedSet

class Member(
    val id: Long? = null,
    val name: String,
    val email: String,
    val phoneNumber: String,
    val birthDate: LocalDate,
    val department: Department,
    val studentId: String,
    val parts: SortedSet<Part> = sortedSetOf(),
    var role: MemberRole,
    val nicknameEnglish: String,
    val nicknameKorean: String,
    var state: MemberState,
    val joinDate: LocalDate,
    var note: String,
    var stateUpdatedTime: LocalDateTime,
    createdTime: LocalDateTime? = null,
    updatedTime: LocalDateTime? = null,
) : BaseTime(createdTime, updatedTime), Comparable<Member> {

    init {
        if (note.length > 255) {
            throw IllegalMemberException("비고 란에는 최대 255자까지 입력 가능합니다. 현재 입력된 글자 수: ${note.length}자")
        }
    }

    fun updateState(newState: MemberState, stateUpdatedTime: LocalDateTime) {
        this.role = MemberRole.MEMBER
        this.state = newState
        this.stateUpdatedTime = stateUpdatedTime
    }

    fun updateRole(newRole: MemberRole) {
        if (newRole in listOf(MemberRole.LEAD, MemberRole.VICE_LEAD)) {
            val (currentYear, currentTerm) = Semester.of(LocalDate.now()).run { year to term.intValue }
            val partName: String = this.parts.first().name
            val newRoleName: String = MemberRoleConverter.convertToString(newRole)

            val newNote = "${currentYear}년 ${currentTerm}학기 $partName 파트 $newRoleName 역임\n"

            this.note = "${this.note}${newNote}"
        }

        this.role = newRole
    }

    override fun compareTo(other: Member): Int {
        val partCompare = this.parts.first().compareTo(other.parts.first())

        if (partCompare != 0) {
            return partCompare
        }

        return this.joinDate.compareTo(other.joinDate)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Member

        return id == other.id
    }

    override fun hashCode(): Int {
        return id?.hashCode() ?: 0
    }
}
