package com.yourssu.scouter.common.storage.domain.part

import com.yourssu.scouter.common.implement.domain.part.Part
import com.yourssu.scouter.common.storage.domain.division.DivisionEntity
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
) {

    companion object {
        fun from(part: Part) = PartEntity(
            id = part.id,
            division = DivisionEntity.from(part.division),
            name = part.name,
        )
    }

    fun toDomain() = Part(
        id = id,
        division = division.toDomain(),
        name = name,
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

    override fun toString(): String {
        return "PartEntity(id=$id, division=$division, name='$name')"
    }
}
