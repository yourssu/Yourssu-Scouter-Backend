package com.yourssu.scouter.common.division.implement

import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
@Transactional
class DivisionReader(
    private val divisionRepository: DivisionRepository,
) {

    fun readAll(): List<Division> = divisionRepository.findAll()
}
