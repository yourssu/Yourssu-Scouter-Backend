package com.yourssu.scouter.masterdata.part.implement

interface PartRepository {

    fun saveAll(parts: List<Part>)
    fun findById(id: Long): Part?
    fun findAll(): List<Part>
    fun findAllByIds(partIds: List<Long>): List<Part>
    fun updateHasAssignment(partId: Long, hasAssignment: Boolean)
}
