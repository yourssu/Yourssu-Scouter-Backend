package com.yourssu.scouter.common.division.storage

import com.yourssu.scouter.common.division.implement.Division
import com.yourssu.scouter.common.division.implement.DivisionRepository
import org.springframework.stereotype.Repository

@Repository
class DivisionRepositoryImpl(
    private val jpaDivisionRepository: JpaDivisionRepository,
) : DivisionRepository {

    override fun save(division: Division): Division =
        jpaDivisionRepository.save(DivisionEntity.from(division)).toDomain()

    override fun count() = jpaDivisionRepository.count()

    override fun findAll(): List<Division> = jpaDivisionRepository.findAll().map { it.toDomain() }
}
