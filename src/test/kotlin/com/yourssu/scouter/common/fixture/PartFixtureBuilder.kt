package com.yourssu.scouter.common.fixture

import com.yourssu.scouter.common.implement.domain.division.Division
import com.yourssu.scouter.common.implement.domain.part.Part

class PartFixtureBuilder {
    private var id: Long? = null
    private var division = DivisionFixtureBuilder().build()
    private var name = "백엔드"
    private var sortPriority = 1

    fun id(id: Long) = apply { this.id = id }
    fun division(division: Division) = apply { this.division = division }
    fun name(name: String) = apply { this.name = name }
    fun sortPriority(sortPriority: Int) = apply { this.sortPriority = sortPriority }

    fun build() = Part(
        id = id,
        division = division,
        name = name,
        sortPriority = sortPriority
    )
}