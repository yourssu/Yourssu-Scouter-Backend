package com.yourssu.scouter.common.part.application

import com.yourssu.scouter.common.part.business.PartDto

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
