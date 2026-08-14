package com.yourssu.scouter.auth.authorization.storage

import com.yourssu.scouter.auth.authorization.implement.Role
import com.yourssu.scouter.auth.authorization.implement.UserRole
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table

@Entity
@Table(name = "user_role")
class UserRoleEntity(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Column(nullable = false)
    val userId: Long,

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    val role: Role,
) {

    companion object {
        fun from(userRole: UserRole) = UserRoleEntity(
            id = userRole.id,
            userId = userRole.userId,
            role = userRole.role,
        )
    }

    fun toDomain() = UserRole(
        id = id,
        userId = userId,
        role = role,
    )

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as UserRoleEntity

        return id == other.id
    }

    override fun hashCode(): Int {
        return id?.hashCode() ?: 0
    }
}
