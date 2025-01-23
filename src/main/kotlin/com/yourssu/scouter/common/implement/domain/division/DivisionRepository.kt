package com.yourssu.scouter.common.implement.domain.division

interface DivisionRepository {

    fun save(division: Division): Division
    fun count(): Long
    fun findAll(): List<Division>
}
