package com.yourssu.scouter.common.implement.domain.division

import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
@Transactional
class DivisionReader(
    private val divisionRepository: DivisionRepository,
) {

    fun readAll(): List<Division> {
        return divisionRepository.findAll()
    }
}
