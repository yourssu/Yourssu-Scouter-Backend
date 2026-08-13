package com.yourssu.scouter.masterdata.part.business.dto

import com.yourssu.scouter.masterdata.part.implement.Part

data class ReadPartsResult(
    val partDtos: List<PartDto>,
) {

    companion object {
        fun from(parts: List<Part>): ReadPartsResult = ReadPartsResult(
            partDtos = parts.map { PartDto.from(it) },
        )
    }
}
