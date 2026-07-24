package com.yourssu.scouter.recruiting.interview.storage

import com.yourssu.scouter.common.division.storage.DivisionEntity
import com.yourssu.scouter.common.part.storage.PartEntity
import com.yourssu.scouter.recruiting.interview.implement.InterviewScript
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatCode
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager
import org.springframework.context.annotation.Import

@DataJpaTest
@Import(InterviewScriptRepositoryImpl::class)
@Suppress("NonAsciiCharacters")
class InterviewScriptRepositoryImplTest {

    @Autowired
    lateinit var interviewScriptRepositoryImpl: InterviewScriptRepositoryImpl

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
    fun `설정된 스크립트가 없으면 null을 반환한다`() {
        // when
        val result = interviewScriptRepositoryImpl.findByPartId(partId)

        // then
        assertThat(result).isNull()
    }

    @Test
    fun `스크립트가 없는 파트에 처음 설정하면 저장된다`() {
        // given
        val script = InterviewScript(partId, "안녕하세요, 면접에 참여해 주셔서 감사합니다.", "오늘 면접 수고하셨습니다.")

        // when
        assertThatCode {
            interviewScriptRepositoryImpl.upsert(script)
        }.doesNotThrowAnyException()

        // then
        val found = interviewScriptRepositoryImpl.findByPartId(partId)
        assertThat(found).isNotNull
        assertThat(found!!.opening).isEqualTo("안녕하세요, 면접에 참여해 주셔서 감사합니다.")
        assertThat(found.closing).isEqualTo("오늘 면접 수고하셨습니다.")
    }

    @Test
    fun `이미 설정된 스크립트를 다시 저장해도 예외 없이 갱신된다`() {
        // given: 최초 설정
        interviewScriptRepositoryImpl.upsert(InterviewScript(partId, "기존 오프닝", "기존 클로징"))

        // when: 같은 파트에 재설정
        assertThatCode {
            interviewScriptRepositoryImpl.upsert(InterviewScript(partId, "새로운 오프닝", "새로운 클로징"))
        }.doesNotThrowAnyException()

        // then
        val found = interviewScriptRepositoryImpl.findByPartId(partId)
        assertThat(found).isNotNull
        assertThat(found!!.opening).isEqualTo("새로운 오프닝")
        assertThat(found.closing).isEqualTo("새로운 클로징")
    }
}
