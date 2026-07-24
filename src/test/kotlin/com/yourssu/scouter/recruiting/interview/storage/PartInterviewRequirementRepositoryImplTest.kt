package com.yourssu.scouter.recruiting.interview.storage

import com.yourssu.scouter.common.division.storage.DivisionEntity
import com.yourssu.scouter.common.part.storage.PartEntity
import com.yourssu.scouter.recruiting.interview.implement.PartInterviewRequirement
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatCode
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager
import org.springframework.context.annotation.Import

@DataJpaTest
@Import(PartInterviewRequirementRepositoryImpl::class)
@Suppress("NonAsciiCharacters")
class PartInterviewRequirementRepositoryImplTest {

    @Autowired
    lateinit var partInterviewRequirementRepositoryImpl: PartInterviewRequirementRepositoryImpl

    @Autowired
    lateinit var entityManager: TestEntityManager

    private var partId: Long = 0

    @BeforeEach
    fun setUp() {
        val division = entityManager.persist(DivisionEntity(null, "개발", 1))
        val part = entityManager.persist(PartEntity(null, division, "PM", 1))
        partId = part.id!!

        entityManager.flush()
        entityManager.clear()
    }

    @Test
    fun `설정된 요구조건이 없으면 null을 반환한다`() {
        // when
        val result = partInterviewRequirementRepositoryImpl.findByPartId(partId)

        // then
        assertThat(result).isNull()
    }

    @Test
    fun `요구조건이 없는 파트에 처음 설정하면 저장된다`() {
        // given
        val requirement = PartInterviewRequirement(partId, "주도성", "커뮤니케이션", "문제 구조화")

        // when
        assertThatCode {
            partInterviewRequirementRepositoryImpl.upsert(requirement)
        }.doesNotThrowAnyException()

        // then
        val found = partInterviewRequirementRepositoryImpl.findByPartId(partId)
        assertThat(found).isNotNull
        assertThat(found!!.culture).isEqualTo("주도성")
        assertThat(found.team).isEqualTo("커뮤니케이션")
        assertThat(found.job).isEqualTo("문제 구조화")
    }

    @Test
    fun `이미 설정된 요구조건을 다시 저장해도 예외 없이 갱신된다`() {
        // given: 최초 설정
        partInterviewRequirementRepositoryImpl.upsert(PartInterviewRequirement(partId, "주도성", "커뮤니케이션", "문제 구조화"))

        // when: 같은 파트에 재설정
        assertThatCode {
            partInterviewRequirementRepositoryImpl.upsert(PartInterviewRequirement(partId, "집중력", "책임감", "실행안 전환"))
        }.doesNotThrowAnyException()

        // then
        val found = partInterviewRequirementRepositoryImpl.findByPartId(partId)
        assertThat(found).isNotNull
        assertThat(found!!.culture).isEqualTo("집중력")
        assertThat(found.team).isEqualTo("책임감")
        assertThat(found.job).isEqualTo("실행안 전환")
    }
}
