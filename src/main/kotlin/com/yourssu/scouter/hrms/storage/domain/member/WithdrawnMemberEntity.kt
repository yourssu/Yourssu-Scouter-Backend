package com.yourssu.scouter.hrms.storage.domain.member

import com.yourssu.scouter.hrms.implement.domain.member.Member
import com.yourssu.scouter.hrms.implement.domain.member.WithdrawnMember
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.OneToOne
import jakarta.persistence.Table

@Entity
@Table(name = "withdrawn_member")
class WithdrawnMemberEntity(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "member_id", nullable = false)
    val member: MemberEntity,
) {

    companion object {
        fun from(withdrawnMember: WithdrawnMember) = WithdrawnMemberEntity(
            id = withdrawnMember.id,
            member = MemberEntity.from(withdrawnMember.member),
        )
    }

    fun toDomain(savedMember: Member) = WithdrawnMember(
        id = id,
        member = savedMember,
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

    override fun toString(): String {
        return "WithdrawnMemberEntity(id=$id, member=$member)"
    }
}
