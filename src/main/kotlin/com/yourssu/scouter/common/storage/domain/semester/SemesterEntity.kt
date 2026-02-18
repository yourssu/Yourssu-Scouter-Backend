package com.yourssu.scouter.common.storage.domain.semester

import com.yourssu.scouter.common.implement.domain.semester.Semester
import com.yourssu.scouter.common.implement.domain.semester.Term
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.Year

@Entity
@Table(name = "semester")
class SemesterEntity(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Column(name = "academic_year", nullable = false)
    val year: Year,

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    val term: Term,
) {

    companion object {
        fun from(semester: Semester): SemesterEntity = SemesterEntity(
            id = semester.id,
            year = semester.year,
            term = semester.term
        )
    }

    fun toDomain(): Semester = Semester(
        id = id,
        year = year,
        term = term
    )
}
