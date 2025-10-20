package com.yourssu.scouter.common.fixture

import com.yourssu.scouter.common.implement.domain.division.Division

class DivisionFixtureBuilder {
    private var id: Long? = 1L
    private var name = "컴퓨터학부"
    private var sortPriority = 1

    fun id(id: Long) = apply { this.id = id }
    fun name(name: String) = apply { this.name = name }
    fun sortPriority(sortPriority: Int) = apply { this.sortPriority = sortPriority }

    fun build() = Division(
        id = id,
        name = name,
        sortPriority = sortPriority
    )
}