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

        const val DELIMITER = "-"
        const val YEAR_LABEL = "년"
        const val TERM_LABEL = "학기"

        fun of(date: LocalDate): Semester = Semester(
            year = Year.of(date.year),
            term = Term.of(date)
        )

        fun of(valueWithDelimiter: String): Semester {
            val (yearString, termString) = valueWithDelimiter.split(DELIMITER)

            return of(yearString, termString)
        }

        private fun of(year: String, term: String): Semester {
            val yearNumber = year.replace(YEAR_LABEL, "")
            val termNumber = term.replace(TERM_LABEL, "")
            val yearValue = yearNumber.toInt() % 1000 + 2000
            val termValue: Int = termNumber.toInt()

            return Semester(
                year = Year.of(yearValue),
                term = Term.of(termValue)
            )
        }

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
        val yearCompare = this.year.compareTo(other.year)
        if (yearCompare != 0) {
            return yearCompare
        }

        return this.term.intValue.compareTo(other.term.intValue)
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
}
