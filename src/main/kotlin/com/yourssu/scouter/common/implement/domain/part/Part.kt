package com.yourssu.scouter.common.implement.domain.part

class Part(
    val id: Long? = null,
    val divisionId: Long,
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
        return "Part(id=$id, divisionId=$divisionId, name='$name')"
    }
}
