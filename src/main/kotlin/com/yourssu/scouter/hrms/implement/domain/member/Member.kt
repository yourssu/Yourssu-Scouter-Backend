package com.yourssu.scouter.hrms.implement.domain.member

import com.yourssu.scouter.common.implement.domain.basetime.BaseTime
import com.yourssu.scouter.common.implement.domain.department.Department
import com.yourssu.scouter.common.implement.domain.part.Part
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
    val role: MemberRole,
    val nicknameEnglish: String,
    val nicknameKorean: String,
    val state: MemberState,
    val joinDate: LocalDate,
    val note: String,
    val stateUpdatedTime: LocalDateTime,
    createdTime: LocalDateTime? = null,
    updatedTime: LocalDateTime? = null,
) : BaseTime(createdTime, updatedTime), Comparable<Member> {

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
