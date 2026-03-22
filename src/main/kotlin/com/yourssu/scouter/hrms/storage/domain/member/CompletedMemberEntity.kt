package com.yourssu.scouter.hrms.storage.domain.member

import com.yourssu.scouter.common.storage.domain.semester.SemesterEntity
import com.yourssu.scouter.hrms.implement.domain.member.CompletedMember
import com.yourssu.scouter.hrms.implement.domain.member.Member
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.ForeignKey
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.OneToOne
import jakarta.persistence.Table

@Entity
@Table(name = "completed_member")
class CompletedMemberEntity(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "member_id", nullable = false)
    val member: MemberEntity,

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(
        name = "completion_semester_id",
        nullable = false,
        foreignKey = ForeignKey(name = "fk_completed_member_completion_semester")
    )
    val completionSemester: SemesterEntity,
) {

    companion object {
        fun from(completedMember: CompletedMember) = CompletedMemberEntity(
            id = completedMember.id,
            member = MemberEntity.from(completedMember.member),
            completionSemester = SemesterEntity.from(completedMember.completionSemester),
        )
    }

    fun toDomain(savedMember: Member) = CompletedMember(
        id = id,
        member = savedMember,
        completionSemester = completionSemester.toDomain(),
    )

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as CompletedMemberEntity

        return id == other.id
    }

    override fun hashCode(): Int {
        return id?.hashCode() ?: 0
    }
}
