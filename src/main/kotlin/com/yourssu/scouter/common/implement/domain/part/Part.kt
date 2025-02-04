package com.yourssu.scouter.common.implement.domain.part

import com.yourssu.scouter.common.implement.domain.division.Division

class Part(
    val id: Long? = null,
    val division: Division,
    val name: String,
    val sortPriority: Int,
) : Comparable<Part> {

    override fun compareTo(other: Part): Int {
        val divisionCompare = this.division.compareTo(other.division)
        if (divisionCompare != 0) {
            return divisionCompare
        }

        return this.sortPriority.compareTo(other.sortPriority)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Part

        return id == other.id
    }

    override fun hashCode(): Int {
        return id?.hashCode() ?: 0
    }
}
