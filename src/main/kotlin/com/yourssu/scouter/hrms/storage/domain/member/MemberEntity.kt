package com.yourssu.scouter.hrms.storage.domain.member

import com.yourssu.scouter.common.implement.domain.part.Part
import com.yourssu.scouter.common.storage.domain.basetime.BaseTimeEntity
import com.yourssu.scouter.common.storage.domain.department.DepartmentEntity
import com.yourssu.scouter.hrms.implement.domain.member.Member
import com.yourssu.scouter.hrms.implement.domain.member.MemberRole
import com.yourssu.scouter.hrms.implement.domain.member.MemberState
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.FetchType
import jakarta.persistence.ForeignKey
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import java.time.LocalDate
import java.time.LocalDateTime

@Entity
@Table(name = "member")
class MemberEntity(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Column(nullable = false)
    val name: String,

    @Column(nullable = false, unique = true)
    val email: String,

    @Column(nullable = false, unique = true)
    val phoneNumber: String,

    @Column(nullable = false)
    val birthDate: LocalDate,

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "department_id", nullable = false, foreignKey = ForeignKey(name = "fk_member_department"))
    val department: DepartmentEntity,

    @Column(nullable = false, unique = true)
    val studentId: String,

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    val role: MemberRole,

    @Column(nullable = false, unique = true)
    val nicknameEnglish: String,

    @Column(nullable = false, unique = true)
    val nicknameKorean: String,

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    val state: MemberState,

    @Column(nullable = false)
    val joinDate: LocalDate,

    @Column(nullable = false)
    val note: String,

    @Column(nullable = false)
    val stateUpdatedTime: LocalDateTime,

    ) : BaseTimeEntity() {

    companion object {
        fun from(member: Member) = MemberEntity(
            id = member.id,
            name = member.name,
            email = member.email,
            phoneNumber = member.phoneNumber,
            birthDate = member.birthDate,
            department = DepartmentEntity.from(member.department),
            studentId = member.studentId,
            role = member.role,
            nicknameEnglish = member.nicknameEnglish,
            nicknameKorean = member.nicknameKorean,
            state = member.state,
            joinDate = member.joinDate,
            note = member.note,
            stateUpdatedTime = member.stateUpdatedTime,
        )
    }

    fun toDomain(savedParts: Collection<Part>) = Member(
        id = id,
        name = name,
        email = email,
        phoneNumber = phoneNumber,
        birthDate = birthDate,
        department = department.toDomain(),
        studentId = studentId,
        parts = savedParts.toSortedSet(),
        role = role,
        nicknameEnglish = nicknameEnglish,
        nicknameKorean = nicknameKorean,
        state = state,
        joinDate = joinDate,
        note = note,
        stateUpdatedTime = stateUpdatedTime,
        createdTime = createdTime,
        updatedTime = updatedTime,
    )

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as MemberEntity

        return id == other.id
    }

    override fun hashCode(): Int {
        return id?.hashCode() ?: 0
    }
}
