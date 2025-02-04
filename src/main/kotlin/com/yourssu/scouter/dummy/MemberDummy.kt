package com.yourssu.scouter.dummy

import com.yourssu.scouter.common.implement.domain.department.DepartmentRepository
import com.yourssu.scouter.common.implement.domain.part.Part
import com.yourssu.scouter.common.implement.domain.part.PartRepository
import com.yourssu.scouter.common.implement.domain.semester.SemesterRepository
import com.yourssu.scouter.common.storage.domain.part.PartEntity
import com.yourssu.scouter.common.storage.domain.semester.SemesterEntity
import com.yourssu.scouter.hrms.implement.domain.member.Member
import com.yourssu.scouter.hrms.implement.domain.member.MemberRole
import com.yourssu.scouter.hrms.implement.domain.member.MemberState
import com.yourssu.scouter.hrms.storage.domain.member.ActiveMemberEntity
import com.yourssu.scouter.hrms.storage.domain.member.GraduatedMemberEntity
import com.yourssu.scouter.hrms.storage.domain.member.InactiveMemberEntity
import com.yourssu.scouter.hrms.storage.domain.member.JpaActiveMemberRepository
import com.yourssu.scouter.hrms.storage.domain.member.JpaGraduatedMemberRepository
import com.yourssu.scouter.hrms.storage.domain.member.JpaInactiveMemberRepository
import com.yourssu.scouter.hrms.storage.domain.member.JpaMemberPartRepository
import com.yourssu.scouter.hrms.storage.domain.member.JpaMemberRepository
import com.yourssu.scouter.hrms.storage.domain.member.JpaWithdrawnMemberRepository
import com.yourssu.scouter.hrms.storage.domain.member.MemberEntity
import com.yourssu.scouter.hrms.storage.domain.member.MemberPartEntity
import com.yourssu.scouter.hrms.storage.domain.member.WithdrawnMemberEntity
import java.time.LocalDate
import org.springframework.stereotype.Component
import kotlin.random.Random

@Component
class MemberDummy(
    private val departmentRepository: DepartmentRepository,
    private val partRepository: PartRepository,
    private val semesterRepository: SemesterRepository,
    private val jpaMemberRepository: JpaMemberRepository,
    private val jpaMemberPartRepository: JpaMemberPartRepository,
    private val jpaActiveMemberRepository: JpaActiveMemberRepository,
    private val jpaInactiveMemberRepository: JpaInactiveMemberRepository,
    private val jpaGraduatedMemberRepository: JpaGraduatedMemberRepository,
    private val jpaWithdrawnMemberRepository: JpaWithdrawnMemberRepository,
) {

    val names = listOf(
        "김지훈", "박수진", "이서준", "정혜진", "최민석", "강지은", "조한별", "임지영", "한윤호", "배유진",
        "오승현", "윤다영", "장민지", "송하윤", "차은지", "문정호", "홍지훈", "배정민", "서윤아", "양찬호"
    )

    val nicknameEnglishes = listOf(
        "Alex", "Brian", "Cathy", "David", "Eva",
        "Frank", "Grace", "Harry", "Ivy", "Jack",
        "Kelly", "Leo", "Mia", "Nick", "Olivia",
        "Paul", "Rachel", "Sam", "Tina", "Victor"
    )

    val nicknameKoreans = listOf(
        "알렉스", "브라이언", "캐시", "데이빗", "에바",
        "프랭크", "그레이스", "해리", "아이비", "잭",
        "켈리", "레오", "미아", "닉", "올리비아",
        "폴", "레이첼", "샘", "티나", "빅터"
    )

    val phoneNumbers = listOf(
        "010-3333-3333", "010-4444-4444", "010-5555-5555", "010-6666-6666", "010-7777-7777",
        "010-8888-8888", "010-9999-9999", "010-1234-5678", "010-2345-6789", "010-3456-7890",
        "010-4567-8901", "010-5678-9012", "010-6789-0123", "010-7890-1234", "010-8901-2345",
        "010-9012-3456", "010-0123-4567", "010-1234-5679", "010-2345-6780", "010-3456-7891"
    )

    val studentIds = listOf(
        "20203333", "20204444", "20205555", "20206666", "20207777",
        "20208888", "20209999", "20201234", "20202345", "20203456",
        "20204567", "20205678", "20206789", "20207890", "20208901",
        "20209012", "20210123", "20211234", "20212345", "20213456"
    )

    val roles = listOf(MemberRole.LEAD, MemberRole.VICE_LEAD, MemberRole.MEMBER)
    val states = listOf(MemberState.ACTIVE, MemberState.INACTIVE, MemberState.GRADUATED, MemberState.WITHDRAWN)

    fun run() {
        val parts = partRepository.findAll()
        val departments = departmentRepository.findAllByOrderByNameAsc()
        val semesters = semesterRepository.findAll()

        for (i in 0 until 20) {
            val toSave = Member(
                name = names[i],
                email = nicknameEnglishes[i] + ".urssu@gmail.com",
                phoneNumber = phoneNumbers[i],
                birthDate = LocalDate.of((1996..2005).random(), (1..12).random(), (1..28).random()),
                department = departments.random(),
                studentId = studentIds[i],
                parts = parts.shuffled().take((1..2).random()).toSortedSet(),
                role = roles.random(),
                nicknameEnglish = nicknameEnglishes[i],
                nicknameKorean = nicknameKoreans[i],
                state = states.random(),
                joinDate = LocalDate.of((2018..2024).random(), (1..12).random(), (1..28).random()),
                note = "ChatGPT가 만들어준 더미 데이터",
                stateUpdatedTime = LocalDate.of((2018..2024).random(), (1..12).random(), (1..28).random()).atStartOfDay()
            )
            val savedMemberEntity = jpaMemberRepository.save(MemberEntity.from(toSave))

            val memberPartEntities = toSave.parts.map { part ->
                MemberPartEntity(member = savedMemberEntity, part = PartEntity.from(part))
            }
            val parts: List<Part> = jpaMemberPartRepository.saveAll(memberPartEntities).map { it.part.toDomain() }
            val savedMember = savedMemberEntity.toDomain(parts)
            when (savedMember.state) {
                MemberState.ACTIVE -> jpaActiveMemberRepository.save(
                    ActiveMemberEntity(
                        member = MemberEntity.from(savedMember),
                        isMembershipFeePaid = Random.nextBoolean()
                    )
                )

                MemberState.INACTIVE -> {
                    val randomStartIdx = semesters.indices.random()
                    val randomEndIdx = (randomStartIdx..<semesters.size).random()
                    jpaInactiveMemberRepository.save(
                        InactiveMemberEntity(
                            member = MemberEntity.from(savedMember),
                            activeStartSemester = SemesterEntity.from(semesters[randomStartIdx]),
                            activeEndSemester = SemesterEntity.from(semesters[randomEndIdx]),
                            expectedReturnSemester = SemesterEntity.from(semesters.random()),
                            inactiveStartSemester = SemesterEntity.from(semesters[randomStartIdx]),
                            inactiveEndSemester = SemesterEntity.from(semesters[randomEndIdx]),
                        )
                    )
                }

                MemberState.GRADUATED -> {
                    val randomStartIdx = semesters.indices.random()
                    val randomEndIdx = (randomStartIdx..<semesters.size).random()
                    jpaGraduatedMemberRepository.save(
                        GraduatedMemberEntity(
                            member = MemberEntity.from(savedMember),
                            activeStartSemester = SemesterEntity.from(semesters[randomStartIdx]),
                            activeEndSemester = SemesterEntity.from(semesters[randomEndIdx]),
                            isAdvisorDesired = Random.nextBoolean()
                        )
                    )
                }

                MemberState.WITHDRAWN -> {
                    jpaWithdrawnMemberRepository.save(
                        WithdrawnMemberEntity(
                            member = MemberEntity.from(savedMember),
                        )
                    )
                }
            }
        }
    }
}
