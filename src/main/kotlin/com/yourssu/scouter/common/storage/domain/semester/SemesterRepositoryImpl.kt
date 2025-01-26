package com.yourssu.scouter.common.storage.domain.semester

import com.yourssu.scouter.common.implement.domain.semester.Semester
import com.yourssu.scouter.common.implement.domain.semester.SemesterRepository
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Repository

@Repository
class SemesterRepositoryImpl(
    private val jpaSemesterRepository: JpaSemesterRepository,
) : SemesterRepository {

    override fun findById(semesterId: Long): Semester? {
        return jpaSemesterRepository.findByIdOrNull(semesterId)?.toDomain()
    }
}
