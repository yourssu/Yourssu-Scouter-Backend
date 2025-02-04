package com.yourssu.scouter.hrms.storage.domain.member

import com.yourssu.scouter.common.storage.domain.semester.SemesterEntity
import com.yourssu.scouter.hrms.implement.domain.member.GraduatedMember
import com.yourssu.scouter.hrms.implement.domain.member.Member
import com.yourssu.scouter.hrms.implement.domain.member.SemesterPeriod
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
@Table(name = "graduated_member")
class GraduatedMemberEntity(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "member_id", nullable = false)
    val member: MemberEntity,

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(
        name = "active_start_semester_id",
        nullable = false,
        foreignKey = ForeignKey(name = "fk_graduated_member_active_start_semester")
    )
    val activeStartSemester: SemesterEntity,

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(
        name = "active_end_semester_id",
        nullable = false,
        foreignKey = ForeignKey(name = "fk_graduated_member_active_end_semester")
    )
    val activeEndSemester: SemesterEntity,

    val isAdvisorDesired: Boolean,
) {

    companion object {
        fun from(graduatedMember: GraduatedMember) = GraduatedMemberEntity(
            id = graduatedMember.id,
            member = MemberEntity.from(graduatedMember.member),
            activeStartSemester = SemesterEntity.from(graduatedMember.activePeriod.startSemester),
            activeEndSemester = SemesterEntity.from(graduatedMember.activePeriod.endSemester),
            isAdvisorDesired = graduatedMember.isAdvisorDesired,
        )
    }

    fun toDomain(savedMember: Member) = GraduatedMember(
        id = id,
        member = savedMember,
        activePeriod = SemesterPeriod(
            startSemester = activeStartSemester.toDomain(),
            endSemester = activeEndSemester.toDomain()
        ),
        isAdvisorDesired = isAdvisorDesired,
    )

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as GraduatedMemberEntity

        return id == other.id
    }

    override fun hashCode(): Int {
        return id?.hashCode() ?: 0
    }
}
