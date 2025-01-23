package com.yourssu.scouter.common.implement.domain.part

interface PartRepository {

    fun saveAll(parts: List<Part>)
    fun findAll(): List<Part>
}
