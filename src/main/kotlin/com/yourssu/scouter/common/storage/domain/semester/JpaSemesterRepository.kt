package com.yourssu.scouter.common.storage.domain.semester

import com.yourssu.scouter.common.implement.domain.semester.Term
import java.time.Year
import org.springframework.data.jpa.repository.JpaRepository

interface JpaSemesterRepository : JpaRepository<SemesterEntity, Long> {

    fun findByYearAndTerm(year: Year, term: Term): SemesterEntity?
}
