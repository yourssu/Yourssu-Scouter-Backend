package com.yourssu.scouter.hrms.implement.domain.member

class ActiveMember(
    val id: Long? = null,
    val member: Member,
    val isMembershipFeePaid: Boolean = false,
) {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ActiveMember

        return id == other.id
    }

    override fun hashCode(): Int {
        return id?.hashCode() ?: 0
    }

    override fun toString(): String {
        return "ActiveMember(id=$id, member=$member, isMembershipFeePaid=$isMembershipFeePaid)"
    }
}
