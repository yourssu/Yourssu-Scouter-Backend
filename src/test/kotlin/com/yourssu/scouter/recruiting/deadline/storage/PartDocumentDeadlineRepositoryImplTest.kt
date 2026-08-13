package com.yourssu.scouter.recruiting.deadline.storage

import com.yourssu.scouter.recruiting.deadline.implement.PartDocumentDeadline
import com.yourssu.scouter.masterdata.division.storage.DivisionEntity
import com.yourssu.scouter.masterdata.part.storage.PartEntity
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatCode
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager
import org.springframework.context.annotation.Import
import java.time.Instant

@DataJpaTest
@Import(PartDocumentDeadlineRepositoryImpl::class)
@Suppress("NonAsciiCharacters")
class PartDocumentDeadlineRepositoryImplTest {

    @Autowired
    lateinit var partDocumentDeadlineRepositoryImpl: PartDocumentDeadlineRepositoryImpl

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
    fun `설정된 마감일이 없으면 null을 반환한다`() {
        // when
        val result = partDocumentDeadlineRepositoryImpl.findByPartId(partId)

        // then
        assertThat(result).isNull()
    }

    @Test
    fun `마감일이 없는 파트에 처음 설정하면 저장된다`() {
        // given
        val deadline = Instant.parse("2025-11-22T14:59:00Z")

        // when
        assertThatCode {
            partDocumentDeadlineRepositoryImpl.upsert(PartDocumentDeadline(partId, deadline))
        }.doesNotThrowAnyException()

        // then
        val found = partDocumentDeadlineRepositoryImpl.findByPartId(partId)
        assertThat(found).isNotNull
        assertThat(found!!.deadline).isEqualTo(deadline)
    }

    @Test
    fun `이미 설정된 마감일을 다시 저장해도 예외 없이 갱신된다`() {
        // given: 최초 설정
        val firstDeadline = Instant.parse("2025-11-22T14:59:00Z")
        partDocumentDeadlineRepositoryImpl.upsert(PartDocumentDeadline(partId, firstDeadline))

        // when: 같은 파트에 재설정
        val secondDeadline = Instant.parse("2025-12-01T00:00:00Z")
        assertThatCode {
            partDocumentDeadlineRepositoryImpl.upsert(PartDocumentDeadline(partId, secondDeadline))
        }.doesNotThrowAnyException()

        // then
        val found = partDocumentDeadlineRepositoryImpl.findByPartId(partId)
        assertThat(found).isNotNull
        assertThat(found!!.deadline).isEqualTo(secondDeadline)
    }
}
