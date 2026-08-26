package com.yourssu.scouter.masterdata.part.implement

import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
@Transactional
class PartWriter(
    private val partRepository: PartRepository,
) {

    fun updateHasAssignment(partId: Long, hasAssignment: Boolean) {
        partRepository.updateHasAssignment(partId, hasAssignment)
    }
}
