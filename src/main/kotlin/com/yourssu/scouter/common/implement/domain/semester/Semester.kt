package com.yourssu.scouter.common.implement.domain.semester

import java.time.LocalDate
import java.time.Year

class Semester(
    val id: Long? = null,
    val year: Year,
    val term: Term,
) : Comparable<Semester> {
    constructor(
        year: Int,
        term: Int
    ) : this(
        id = null,
        year = Year.of(year),
        term = Term.from(term)
    )

    companion object {
        fun of(date: LocalDate): Semester = Semester(
            year = Year.of(date.year),
            term = Term.of(date)
        )

        fun previous(date: LocalDate): Semester {
            val current: Semester = of(date)

            return current.previous()
        }
    }

    fun previous(): Semester {
        if (term == Term.SPRING) {
            return Semester(year = year.minusYears(1), term = Term.FALL)
        }

        return Semester(year = year, term = Term.SPRING)
    }

    fun next(): Semester {
        if (term == Term.FALL) {
            return Semester(year = year.plusYears(1), term = Term.SPRING)
        }

        return Semester(year = year, term = Term.FALL)
    }

    override fun compareTo(other: Semester): Int {
        val yearCompare = year.compareTo(other.year)
        if (yearCompare != 0) {
            return yearCompare
        }

        return term.intValue.compareTo(other.term.intValue)
    }

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
