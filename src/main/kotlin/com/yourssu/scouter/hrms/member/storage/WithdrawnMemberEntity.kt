package com.yourssu.scouter.hrms.member.storage

import com.yourssu.scouter.hrms.member.implement.Member
import com.yourssu.scouter.hrms.member.implement.WithdrawnMember
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.OneToOne
import jakarta.persistence.Table
import java.time.LocalDate

@Entity
@Table(name = "withdrawn_member")
class WithdrawnMemberEntity(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "member_id", nullable = false)
    val member: MemberEntity,

    @Column(name = "withdrawn_date")
    val withdrawnDate: LocalDate? = null,
) {

    companion object {
        fun from(withdrawnMember: WithdrawnMember) = WithdrawnMemberEntity(
            id = withdrawnMember.id,
            member = MemberEntity.from(withdrawnMember.member),
            withdrawnDate = withdrawnMember.withdrawnDate,
        )
    }

    fun toDomain(savedMember: Member) = WithdrawnMember(
        id = id,
        member = savedMember,
        withdrawnDate = withdrawnDate,
    )

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as WithdrawnMemberEntity

        return id == other.id
    }

    override fun hashCode(): Int {
        return id?.hashCode() ?: 0
    }
}
