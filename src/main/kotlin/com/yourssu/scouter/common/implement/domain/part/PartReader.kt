package com.yourssu.scouter.common.implement.domain.part

import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
@Transactional(readOnly = true)
class PartReader(
    private val partRepository: PartRepository,
) {

    fun readAll(): List<Part> = partRepository.findAll()
}
