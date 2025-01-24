package com.yourssu.scouter.hrms.implement.domain.member

import com.yourssu.scouter.common.implement.domain.department.Department
import com.yourssu.scouter.common.implement.domain.part.Part
import java.time.LocalDate

class Member(
    val id: Long? = null,
    val name: String,
    val email: String,
    val phoneNumber: String,
    val birthDate: LocalDate,
    val department: Department,
    val studentId: String,
    val part: Part,
    val role: MemberRole,
    val nicknameEnglish: String,
    val nicknameKorean: String,
    val state: MemberState,
    val joinDate: LocalDate,
    val isMembershipFeePaid: Boolean,
    val note: String,
) {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Member

        return id == other.id
    }

    override fun hashCode(): Int {
        return id?.hashCode() ?: 0
    }

    override fun toString(): String {
        return "Member(id=$id, name='$name', email='$email', phoneNumber='$phoneNumber', birthDate=$birthDate, department=$department, studentId='$studentId', part=$part, role=$role, nicknameEnglish='$nicknameEnglish', nicknameKorean='$nicknameKorean', state=$state, joinDate=$joinDate, membershipFee=$isMembershipFeePaid, note='$note')"
    }
}
