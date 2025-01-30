package com.yourssu.scouter.hrms.implement.domain.member

class WithdrawnMember(
    val id: Long? = null,
    val member: Member,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as WithdrawnMember

        return id == other.id
    }

    override fun hashCode(): Int {
        return id.hashCode()
    }

    override fun toString(): String {
        return "WithdrawnMember(id=$id, member=$member)"
    }
}
