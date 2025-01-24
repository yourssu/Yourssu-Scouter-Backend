package com.yourssu.scouter.common.implement.domain.part

import com.yourssu.scouter.common.implement.domain.division.Division

class Part(
    val id: Long? = null,
    val division: Division,
    val name: String,
) {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Part

        return id == other.id
    }

    override fun hashCode(): Int {
        return id?.hashCode() ?: 0
    }

    override fun toString(): String {
        return "Part(id=$id, division=$division, name='$name')"
    }
}
