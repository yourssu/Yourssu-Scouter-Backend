package com.yourssu.scouter.common.implement.domain.part

import com.yourssu.scouter.common.implement.support.exception.PartNotFoundException
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
@Transactional(readOnly = true)
class PartReader(
    private val partRepository: PartRepository,
) {

    fun readById(partId: Long): Part = partRepository.findById(partId)
        ?: throw PartNotFoundException("지정한 파트를 찾을 수 없습니다.")

    fun readAll(): List<Part> = partRepository.findAll()

    fun readAllByIds(partIds: List<Long>): List<Part> {
        return partRepository.findAllByIds(partIds)
    }
}
