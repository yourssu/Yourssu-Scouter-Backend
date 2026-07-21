package com.yourssu.scouter.common.part.business

import com.yourssu.scouter.common.part.implement.Part

data class ReadPartsResult(
    val partDtos: List<PartDto>,
) {

    companion object {
        fun from(parts: List<Part>): ReadPartsResult = ReadPartsResult(
            partDtos = parts.map { PartDto.from(it) },
        )
    }
}
