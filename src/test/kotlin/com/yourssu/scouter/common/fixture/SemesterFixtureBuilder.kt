package com.yourssu.scouter.common.fixture

import com.yourssu.scouter.common.implement.domain.semester.Semester
import com.yourssu.scouter.common.implement.domain.semester.Term
import java.time.Year

class SemesterFixtureBuilder {

    private var id: Long? = 1L
    private var year: Year = Year.of(2025)
    private var term: Term = Term.SPRING

    fun id(id: Long) = apply { this.id = id }
    fun year(year: Year) = apply { this.year = year }
    fun term(term: Term) = apply { this.term = term }

    fun build() = Semester(
        id = id,
        year = year,
        term = term
    )
}