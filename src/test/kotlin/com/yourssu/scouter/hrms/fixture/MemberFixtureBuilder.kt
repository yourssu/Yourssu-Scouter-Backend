package com.yourssu.scouter.hrms.fixture

import com.yourssu.scouter.common.fixture.DivisionFixtureBuilder
import com.yourssu.scouter.common.fixture.PartFixtureBuilder
import com.yourssu.scouter.common.implement.domain.department.Department
import com.yourssu.scouter.hrms.implement.domain.member.Member
import com.yourssu.scouter.hrms.implement.domain.member.MemberRole
import com.yourssu.scouter.hrms.implement.domain.member.MemberState
import java.time.Instant
import java.time.LocalDate

class MemberFixtureBuilder {
    private var id: Long? = 1L
    private var name = "홍길동"
    private var email = "hong@soongsil.ac.kr"
    private var phoneNumber = "010-1234-5678"
    private var birthDate: LocalDate = LocalDate.of(2000, 1, 1)
    private var department = Department(id = 1L, collegeId = 1L, name = "컴퓨터학부")
    private var studentId = "20210001"
    private var parts = sortedSetOf(
        PartFixtureBuilder()
            .id(1L)
            .division(DivisionFixtureBuilder().id(1L).build())
            .build()
    )
    private var role = MemberRole.MEMBER
    private var nicknameEnglish = "piki"
    private var nicknameKorean = "피키"
    private var state = MemberState.ACTIVE
    private var joinDate: LocalDate = LocalDate.of(2024, 3, 1)
    private var note = ""
    private var stateUpdatedTime: Instant = Instant.parse("2024-03-01T00:00:00Z")
    private var createdTime: Instant? = Instant.parse("2024-03-01T00:00:00Z")
    private var updatedTime: Instant? = Instant.parse("2024-03-01T00:00:00Z")

    fun id(id: Long?) = apply { this.id = id }
    fun name(name: String) = apply { this.name = name }
    fun email(email: String) = apply { this.email = email }
    fun studentId(studentId: String) = apply { this.studentId = studentId }

    fun build() = Member(
        id = id,
        name = name,
        email = email,
        phoneNumber = phoneNumber,
        birthDate = birthDate,
        department = department,
        studentId = studentId,
        parts = parts,
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
}
