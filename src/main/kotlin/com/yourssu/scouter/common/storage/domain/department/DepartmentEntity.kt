package com.yourssu.scouter.common.storage.domain.department

import com.yourssu.scouter.common.implement.domain.department.Department
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table

@Entity
@Table(name = "department")
class DepartmentEntity(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Column(nullable = false)
    val collegeId: Long,

    @Column(nullable = false, unique = true)
    val name: String,
) {

    companion object {
        fun from(department: Department) = DepartmentEntity(
            id = department.id,
            collegeId = department.collegeId,
            name = department.name,
        )
    }

    fun toDomain() = Department(
        id = id,
        collegeId = collegeId,
        name = name,
    )

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as DepartmentEntity

        return id == other.id
    }

    override fun hashCode(): Int {
        return id?.hashCode() ?: 0
    }
}
