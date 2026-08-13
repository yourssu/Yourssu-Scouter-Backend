package com.yourssu.scouter.masterdata.part.application.dto

import com.yourssu.scouter.masterdata.part.business.dto.PartDto

data class ReadPartsResponse(
    val partId: Long,
    val partName: String,
    val hasAssignment: Boolean,
) {

    companion object {
        fun from(partDto: PartDto) = ReadPartsResponse(
            partId = partDto.id,
            partName = partDto.name,
            hasAssignment = partDto.hasAssignment,
        )
    }
}
