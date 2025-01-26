package com.yourssu.scouter.common.storage.domain.semester

import com.yourssu.scouter.common.implement.domain.semester.Semester
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table

@Entity
@Table(name = "semester")
class SemesterEntity(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Column(nullable = false)
    val year: Int,

    @Column(nullable = false)
    val semester: Int,
) {

    companion object {
        fun from(semester: Semester): SemesterEntity = SemesterEntity(
            id = semester.id,
            year = semester.year,
            semester = semester.semester
        )
    }

    fun toDomain(): Semester = Semester(
        id = id,
        year = year,
        semester = semester
    )
}
