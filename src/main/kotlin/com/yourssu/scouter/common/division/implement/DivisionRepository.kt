package com.yourssu.scouter.common.division.implement

interface DivisionRepository {

    fun save(division: Division): Division
    fun count(): Long
    fun findAll(): List<Division>
}
