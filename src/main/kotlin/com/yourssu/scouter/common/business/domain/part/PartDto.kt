package com.yourssu.scouter.common.business.domain.part

import com.yourssu.scouter.common.implement.domain.part.Part

data class PartDto(
    val id: Long,
    val divisionId: Long,
    val name: String,
) {

    companion object {
        fun from(part: Part): PartDto = PartDto(
            id = part.id!!,
            divisionId = part.divisionId,
            name = part.name,
        )
    }
}
