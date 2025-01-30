package com.yourssu.scouter.hrms.storage.domain.member

import com.yourssu.scouter.common.implement.domain.part.Part
import com.yourssu.scouter.hrms.implement.domain.member.ActiveMember
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.ForeignKey
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.OneToOne
import jakarta.persistence.Table

@Entity
@Table(name = "active_member")
class ActiveMemberEntity(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "member_id", nullable = false, foreignKey = ForeignKey(name = "fk_active_member_member"))
    val member: MemberEntity,

    val isMembershipFeePaid: Boolean,
) {

    companion object {
        fun from(activeMember: ActiveMember) = ActiveMemberEntity(
            id = activeMember.id,
            member = MemberEntity.from(activeMember.member),
            isMembershipFeePaid = activeMember.isMembershipFeePaid,
        )
    }

    fun toDomain(parts: List<Part>) = ActiveMember(
        id = id,
        member = member.toDomain(parts),
        isMembershipFeePaid = isMembershipFeePaid,
    )

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ActiveMemberEntity

        return id == other.id
    }

    override fun hashCode(): Int {
        return id?.hashCode() ?: 0
    }

    override fun toString(): String {
        return "ActiveMemberEntity(id=$id, member=$member, isMembershipFeePaid=$isMembershipFeePaid)"
    }
}
