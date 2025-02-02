package com.yourssu.scouter.common.storage.domain.semester

import com.yourssu.scouter.common.implement.domain.semester.Semester
import com.yourssu.scouter.common.implement.domain.semester.SemesterRepository
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Repository

@Repository
class SemesterRepositoryImpl(
    private val jpaSemesterRepository: JpaSemesterRepository,
) : SemesterRepository {

    override fun save(semester: Semester): Semester {
        return jpaSemesterRepository.save(SemesterEntity.from(semester)).toDomain()
    }

    override fun findById(semesterId: Long): Semester? {
        return jpaSemesterRepository.findByIdOrNull(semesterId)?.toDomain()
    }

    override fun find(semester: Semester): Semester? {
        return jpaSemesterRepository.findByYearAndTerm(semester.year, semester.term)?.toDomain()
    }

    override fun findAll(): List<Semester> {
        return jpaSemesterRepository.findAll().map { it.toDomain() }
    }

    override fun deleteById(semesterId: Long) {
        jpaSemesterRepository.deleteById(semesterId)
    }
}
