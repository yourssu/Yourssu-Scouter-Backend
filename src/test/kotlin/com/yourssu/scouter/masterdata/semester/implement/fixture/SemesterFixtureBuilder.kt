package com.yourssu.scouter.masterdata.semester.implement.fixture

import com.yourssu.scouter.masterdata.semester.implement.Semester
import com.yourssu.scouter.masterdata.semester.implement.Term
import java.time.Year

class SemesterFixtureBuilder {

    private var id: Long? = null
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