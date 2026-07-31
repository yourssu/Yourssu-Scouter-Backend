package com.yourssu.scouter.common.part.implement

interface PartRepository {

    fun saveAll(parts: List<Part>)
    fun findById(id: Long): Part?
    fun findAll(): List<Part>
    fun findAllByIds(partIds: List<Long>): List<Part>
    fun updateHasAssignment(partId: Long, hasAssignment: Boolean)
}
