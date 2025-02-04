package com.yourssu.scouter.hrms.storage.domain.member

import com.yourssu.scouter.common.storage.domain.semester.SemesterEntity
import com.yourssu.scouter.hrms.implement.domain.member.InactiveMember
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
@Table(name = "inactive_member")
class InactiveMemberEntity(
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
        foreignKey = ForeignKey(name = "fk_inactive_member_active_start_semester")
    )
    val activeStartSemester: SemesterEntity,

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(
        name = "active_end_semester_id",
        nullable = false,
        foreignKey = ForeignKey(name = "fk_inactive_member_active_end_semester")
    )
    val activeEndSemester: SemesterEntity,

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(
        name = "expected_return_semester_id",
        nullable = false,
        foreignKey = ForeignKey(name = "fk_inactive_member_expected_return_semester")
    )
    val expectedReturnSemester: SemesterEntity,

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(
        name = "inactive_start_semester_id",
        nullable = false,
        foreignKey = ForeignKey(name = "fk_inactive_member_inactive_start_semester")
    )
    val inactiveStartSemester: SemesterEntity,

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(
        name = "inactive_end_semester_id",
        nullable = false,
        foreignKey = ForeignKey(name = "fk_inactive_member_inactive_end_semester")
    )
    val inactiveEndSemester: SemesterEntity,
) {

    companion object {
        fun from(inactiveMember: InactiveMember) = InactiveMemberEntity(
            id = inactiveMember.id,
            member = MemberEntity.from(inactiveMember.member),
            activeStartSemester = SemesterEntity.from(inactiveMember.activePeriod.startSemester),
            activeEndSemester = SemesterEntity.from(inactiveMember.activePeriod.endSemester),
            expectedReturnSemester = SemesterEntity.from(inactiveMember.expectedReturnSemester),
            inactiveStartSemester = SemesterEntity.from(inactiveMember.inactivePeriod.startSemester),
            inactiveEndSemester = SemesterEntity.from(inactiveMember.inactivePeriod.endSemester),
        )
    }

    fun toDomain(savedMember: Member) = InactiveMember(
        id = id,
        member = savedMember,
        activePeriod = SemesterPeriod(
            startSemester = activeStartSemester.toDomain(),
            endSemester = activeEndSemester.toDomain(),
        ),
        expectedReturnSemester = expectedReturnSemester.toDomain(),
        inactivePeriod = SemesterPeriod(
            startSemester = inactiveStartSemester.toDomain(),
            endSemester = inactiveEndSemester.toDomain(),
        ),
    )

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as InactiveMemberEntity

        return id == other.id
    }

    override fun hashCode(): Int {
        return id?.hashCode() ?: 0
    }
}
