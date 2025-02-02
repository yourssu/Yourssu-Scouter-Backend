package com.yourssu.scouter.common.storage.domain.part

import com.yourssu.scouter.common.implement.domain.part.Part
import com.yourssu.scouter.common.implement.domain.part.PartRepository
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Repository

@Repository
class PartRepositoryImpl(
    private val jpaPartRepository: JpaPartRepository,
) : PartRepository {

    override fun saveAll(parts: List<Part>) {
        jpaPartRepository.saveAll(parts.map { PartEntity.from(it) })
    }

    override fun findById(id: Long): Part? {
        return jpaPartRepository.findByIdOrNull(id)?.toDomain()
    }

    override fun findAll(): List<Part> {
        return jpaPartRepository.findAll().map { it.toDomain() }
    }

    override fun findAllByIds(partIds: List<Long>): List<Part> {
        return jpaPartRepository.findAllById(partIds).map { it.toDomain() }
    }
}
