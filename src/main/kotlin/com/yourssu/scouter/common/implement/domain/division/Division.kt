package com.yourssu.scouter.common.implement.domain.division

class Division(
    val id: Long? = null,
    val name: String,
    val sortPriority: Int,
): Comparable<Division> {

    override fun compareTo(other: Division): Int {
        return this.sortPriority.compareTo(other.sortPriority)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Division

        return id == other.id
    }

    override fun hashCode(): Int {
        return id?.hashCode() ?: 0
    }
}
