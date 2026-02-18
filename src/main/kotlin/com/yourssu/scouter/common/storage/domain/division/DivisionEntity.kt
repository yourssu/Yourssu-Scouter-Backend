package com.yourssu.scouter.common.storage.domain.division

import com.yourssu.scouter.common.implement.domain.division.Division
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table

@Entity
@Table(name = "division")
class DivisionEntity(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Column(nullable = false, unique = true)
    val name: String,

    @Column(nullable = false)
    val sortPriority: Int,
) {

    companion object {
        fun from(division: Division): DivisionEntity = DivisionEntity(
            id = division.id,
            name = division.name,
            sortPriority = division.sortPriority,
        )
    }

    fun toDomain() = Division(
        id = id,
        name = name,
        sortPriority = sortPriority,
    )

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as DivisionEntity

        return id == other.id
    }

    override fun hashCode(): Int {
        return id?.hashCode() ?: 0
    }
}
