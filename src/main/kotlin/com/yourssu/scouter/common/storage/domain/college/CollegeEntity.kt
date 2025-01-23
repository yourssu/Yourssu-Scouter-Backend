package com.yourssu.scouter.common.storage.domain.college

import com.yourssu.scouter.common.implement.domain.college.College
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table

@Entity
@Table(name = "college")
class CollegeEntity(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Column(unique = true, nullable = false)
    val name: String,
) {

    companion object {
        fun from(college: College) = CollegeEntity(
            id = college.id,
            name = college.name,
        )
    }

    fun toDomain() = College(
        id = id,
        name = name,
    )

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as CollegeEntity

        return id == other.id
    }

    override fun hashCode(): Int {
        return id?.hashCode() ?: 0
    }

    override fun toString(): String {
        return "CollegeEntity(id=$id, name='$name')"
    }
}
