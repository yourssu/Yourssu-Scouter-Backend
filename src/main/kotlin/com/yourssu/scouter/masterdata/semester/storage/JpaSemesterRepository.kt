package com.yourssu.scouter.masterdata.semester.storage

import com.yourssu.scouter.masterdata.semester.implement.Term
import java.time.Year
import org.springframework.data.jpa.repository.JpaRepository

interface JpaSemesterRepository : JpaRepository<SemesterEntity, Long> {

    fun findByYearAndTerm(year: Year, term: Term): SemesterEntity?
}
