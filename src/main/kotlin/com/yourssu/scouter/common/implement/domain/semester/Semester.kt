package com.yourssu.scouter.common.implement.domain.semester

import java.time.Year

class Semester(
    val id: Long? = null,
    val year: Year,
    val term: Term,
) {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Semester

        return id == other.id
    }

    override fun hashCode(): Int {
        return id?.hashCode() ?: 0
    }

    override fun toString(): String {
        return "Semester(id=$id, year=$year, semester=$term)"
    }
}
