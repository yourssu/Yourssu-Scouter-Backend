package com.yourssu.scouter.common.implement.domain.department

class Department(
    val id: Long? = null,
    val collegeId: Long,
    val name: String,
) {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Department

        return id == other.id
    }

    override fun hashCode(): Int {
        return id?.hashCode() ?: 0
    }
}
