package com.yourssu.scouter.common.business.domain.part

import com.yourssu.scouter.common.implement.domain.part.Part

data class ReadPartsResult(
    val partDtos: List<PartDto>,
) {

    companion object {
        fun from(parts: List<Part>): ReadPartsResult = ReadPartsResult(
            partDtos = parts.map { PartDto.from(it) },
        )
    }
}
