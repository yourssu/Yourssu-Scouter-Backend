package com.yourssu.scouter.masterdata.part.storage

import com.yourssu.scouter.masterdata.part.implement.Part
import com.yourssu.scouter.masterdata.division.storage.DivisionEntity
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.ForeignKey
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table

@Entity
@Table(name = "part")
class PartEntity(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "division_id", nullable = false, foreignKey = ForeignKey(name = "fk_part_division"))
    val division: DivisionEntity,

    @Column(nullable = false, unique = true)
    val name: String,

    @Column(nullable = false)
    val sortPriority: Int,

    @Column(nullable = false)
    var hasAssignment: Boolean = false,
) {

    companion object {
        fun from(part: Part) = PartEntity(
            id = part.id,
            division = DivisionEntity.from(part.division),
            name = part.name,
            sortPriority = part.sortPriority,
            hasAssignment = part.hasAssignment,
        )
    }

    fun toDomain() = Part(
        id = id,
        division = division.toDomain(),
        name = name,
        sortPriority = sortPriority,
        hasAssignment = hasAssignment,
    )

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as PartEntity

        return id == other.id
    }

    override fun hashCode(): Int {
        return id?.hashCode() ?: 0
    }
}
