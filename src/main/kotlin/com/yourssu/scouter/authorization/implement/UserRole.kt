package com.yourssu.scouter.authorization.implement

class UserRole(
    val id: Long? = null,
    val userId: Long,
    val role: Role,
) {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as UserRole

        return id == other.id
    }

    override fun hashCode(): Int {
        return id?.hashCode() ?: 0
    }
}
