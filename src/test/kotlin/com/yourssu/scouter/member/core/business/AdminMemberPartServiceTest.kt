package com.yourssu.scouter.member.core.business

import com.yourssu.scouter.masterdata.department.storage.DepartmentEntity
import com.yourssu.scouter.masterdata.division.storage.DivisionEntity
import com.yourssu.scouter.masterdata.part.storage.PartEntity
import com.yourssu.scouter.member.core.implement.MemberRole
import com.yourssu.scouter.member.core.implement.MemberState
import com.yourssu.scouter.member.core.storage.ActiveMemberEntity
import com.yourssu.scouter.member.core.storage.MemberEntity
import com.yourssu.scouter.member.core.storage.MemberPartEntity
import com.yourssu.scouter.member.support.exception.IllegalMemberPartAssignmentException
import jakarta.persistence.EntityManager
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.transaction.annotation.Transactional
import java.time.Instant
import java.time.LocalDate

/**
 * member_part 에는 (member_id, part_id) 유니크 제약이 없다(Flyway 이전 테이블).
 * 그래서 중복 행이 이미 들어가 있을 수 있고, "마지막 파트" 판정은 행 수가 아니라 서로 다른 파트 수로 해야 한다.
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
@Suppress("NonAsciiCharacters")
class AdminMemberPartServiceTest {

    @Autowired
    lateinit var adminMemberPartService: AdminMemberPartService

    @Autowired
    lateinit var em: EntityManager

    private lateinit var backend: PartEntity
    private lateinit var android: PartEntity
    private var memberId: Long = 0

    @BeforeEach
    fun setUp() {
        val division = DivisionEntity(null, "파트배정디비전", 97)
        em.persist(division)
        backend = PartEntity(null, division, "배정백엔드", 97)
        em.persist(backend)
        android = PartEntity(null, division, "배정안드로이드", 96)
        em.persist(android)

        memberId = persistMember("배정대상", "assign-target@example.com", "010-9000-0001", "20260101", "assigntarget")
        em.persist(MemberPartEntity(null, em.find(MemberEntity::class.java, memberId), backend))

        em.flush()
        em.clear()
    }

    @Test
    fun `파트를 배정하면 member_part 행이 늘어난다`() {
        adminMemberPartService.assignPart(memberId, android.id!!)
        em.flush()
        em.clear()

        assertThat(partNamesOf(memberId)).containsExactlyInAnyOrder("배정백엔드", "배정안드로이드")
        assertThat(rowCountOf(memberId)).isEqualTo(2)
    }

    @Test
    fun `이미 배정된 파트는 다시 배정하지 않는다`() {
        assertThatThrownBy { adminMemberPartService.assignPart(memberId, backend.id!!) }
            .isInstanceOf(IllegalMemberPartAssignmentException::class.java)
            .hasMessageContaining("이미")

        assertThat(rowCountOf(memberId)).isEqualTo(1)
    }

    @Test
    fun `배정을 해제하면 행이 줄어든다`() {
        adminMemberPartService.assignPart(memberId, android.id!!)
        em.flush()
        em.clear()

        adminMemberPartService.unassignPart(memberId, android.id!!)
        em.flush()
        em.clear()

        assertThat(partNamesOf(memberId)).containsExactly("배정백엔드")
    }

    @Test
    fun `마지막 파트는 해제할 수 없다`() {
        assertThatThrownBy { adminMemberPartService.unassignPart(memberId, backend.id!!) }
            .isInstanceOf(IllegalMemberPartAssignmentException::class.java)
            .hasMessageContaining("마지막 파트")

        assertThat(partNamesOf(memberId)).containsExactly("배정백엔드")
    }

    @Test
    fun `배정되지 않은 파트는 해제할 수 없다`() {
        assertThatThrownBy { adminMemberPartService.unassignPart(memberId, android.id!!) }
            .isInstanceOf(IllegalMemberPartAssignmentException::class.java)
            .hasMessageContaining("배정되어 있지 않습니다")
    }

    @Test
    fun `같은 파트가 두 행으로 들어가 있어도 파트 하나로 센다`() {
        // given: 유니크 제약이 없어 실제로 들어갈 수 있는 상태를 만든다
        em.persist(MemberPartEntity(null, em.find(MemberEntity::class.java, memberId), backend))
        em.flush()
        em.clear()
        assertThat(rowCountOf(memberId)).isEqualTo(2)

        // when, then: 행은 2개지만 서로 다른 파트는 1개이므로 마지막 파트다
        assertThatThrownBy { adminMemberPartService.unassignPart(memberId, backend.id!!) }
            .isInstanceOf(IllegalMemberPartAssignmentException::class.java)
            .hasMessageContaining("마지막 파트")
    }

    @Test
    fun `해제는 같은 파트의 중복 행을 남김없이 지운다`() {
        // given: backend 가 두 행, android 가 한 행
        val member = em.find(MemberEntity::class.java, memberId)
        em.persist(MemberPartEntity(null, member, backend))
        em.persist(MemberPartEntity(null, member, android))
        em.flush()
        em.clear()
        assertThat(rowCountOf(memberId)).isEqualTo(3)

        // when
        adminMemberPartService.unassignPart(memberId, backend.id!!)
        em.flush()
        em.clear()

        // then
        assertThat(rowCountOf(memberId)).isEqualTo(1)
        assertThat(partNamesOf(memberId)).containsExactly("배정안드로이드")
    }

    @Test
    fun `파트가 없는 멤버도 목록에 나오고 맨 앞에 선다`() {
        val partless = persistMember("파트없음", "no-part@example.com", "010-9000-0002", "20260102", "nopart")
        em.flush()
        em.clear()

        val members = adminMemberPartService.readAllByFilters(MemberState.ACTIVE, null, null)
        val ids = members.map { it.memberId }

        assertThat(ids).contains(partless, memberId)
        assertThat(members.first { it.memberId == partless }.parts).isEmpty()
        assertThat(ids.indexOf(partless)).isLessThan(ids.indexOf(memberId))
    }

    private fun persistMember(name: String, email: String, phone: String, studentId: String, nickname: String): Long {
        val department = em.createQuery("SELECT d FROM DepartmentEntity d", DepartmentEntity::class.java)
            .setMaxResults(1)
            .singleResult
        val member = MemberEntity(
            id = null,
            name = name,
            email = email,
            phoneNumber = phone,
            birthDate = LocalDate.of(2000, 1, 1),
            department = department,
            studentId = studentId,
            role = MemberRole.MEMBER,
            nicknameEnglish = nickname,
            nicknameKorean = name,
            state = MemberState.ACTIVE,
            joinDate = LocalDate.of(2024, 3, 1),
            note = "",
            stateUpdatedTime = Instant.now(),
        )
        em.persist(member)
        em.persist(ActiveMemberEntity(null, member, false, 3, false))
        em.flush()

        return member.id!!
    }

    private fun partNamesOf(memberId: Long): List<String> =
        em.createQuery(
            "SELECT mp.part.name FROM MemberPartEntity mp WHERE mp.member.id = :id ORDER BY mp.part.name",
            String::class.java,
        ).setParameter("id", memberId).resultList

    private fun rowCountOf(memberId: Long): Long =
        em.createQuery(
            "SELECT COUNT(mp) FROM MemberPartEntity mp WHERE mp.member.id = :id",
            java.lang.Long::class.java,
        ).setParameter("id", memberId).singleResult.toLong()
}
