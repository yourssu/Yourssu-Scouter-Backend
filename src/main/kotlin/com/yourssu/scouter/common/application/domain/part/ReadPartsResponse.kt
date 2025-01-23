package com.yourssu.scouter.common.application.domain.part

import com.yourssu.scouter.common.business.domain.part.PartDto

data class ReadPartsResponse(
    val partId: Long,
    val partName: String,
) {

    companion object {
        fun from(partDto: PartDto) = ReadPartsResponse(
            partId = partDto.id,
            partName = partDto.name,
        )
    }
}
