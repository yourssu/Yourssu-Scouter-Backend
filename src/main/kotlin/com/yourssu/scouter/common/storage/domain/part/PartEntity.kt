package com.yourssu.scouter.common.storage.domain.part

import com.yourssu.scouter.common.implement.domain.part.Part
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table

@Entity
@Table(name = "part")
class PartEntity(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Column(nullable = false)
    val divisionId: Long,

    @Column(nullable = false, unique = true)
    val name: String,
) {

    companion object {
        fun from(part: Part) = PartEntity(
            id = part.id,
            divisionId = part.divisionId,
            name = part.name,
        )
    }

    fun toDomain() = Part(
        id = id,
        divisionId = divisionId,
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
        return "PartEntity(id=$id, divisionId=$divisionId, name='$name')"
    }
}
