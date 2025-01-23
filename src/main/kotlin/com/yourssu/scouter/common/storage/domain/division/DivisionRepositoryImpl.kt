package com.yourssu.scouter.common.storage.domain.division

import com.yourssu.scouter.common.implement.domain.division.Division
import com.yourssu.scouter.common.implement.domain.division.DivisionRepository
import org.springframework.stereotype.Repository

@Repository
class DivisionRepositoryImpl(
    private val jpaDivisionRepository: JpaDivisionRepository,
) : DivisionRepository {

    override fun save(division: Division): Division = jpaDivisionRepository.save(DivisionEntity.from(division)).toDomain()

    override fun count() = jpaDivisionRepository.count()

    override fun findAll(): List<Division> = jpaDivisionRepository.findAll().map { it.toDomain() }
}
