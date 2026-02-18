package com.yourssu.scouter.common.storage.domain.mail.template

import com.yourssu.scouter.common.implement.domain.mail.template.MailTemplate
import com.yourssu.scouter.common.implement.domain.mail.template.TemplateVariable
import com.yourssu.scouter.common.implement.domain.mail.template.VariableType
import org.assertj.core.api.Assertions.assertThat
import org.hibernate.exception.ConstraintViolationException
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.context.annotation.Import
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.test.context.TestPropertySource
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager

@DataJpaTest
@Import(MailTemplateRepositoryImpl::class)
@TestPropertySource(properties = ["spring.jpa.hibernate.ddl-auto=create-drop"])
@Suppress("NonAsciiCharacters")
class MailTemplateRepositoryImplUpdateTest {

    @Autowired
    lateinit var mailTemplateRepositoryImpl: MailTemplateRepositoryImpl

    @Autowired
    lateinit var entityManager: TestEntityManager

    @Test
    fun `템플릿 수정 시 동일 variableKey로 변수 전체 교체를 해도 유니크 제약 위반이 발생하지 않는다`() {
        // given
        val created = mailTemplateRepositoryImpl.save(
            MailTemplate(
                title = "제목",
                bodyHtml = "<p>안녕하세요 {{${key}}}님</p>",
                variables = listOf(
                    TemplateVariable(
                        key = key,
                        type = VariableType.APPLICANT,
                        displayName = "지원자",
                        perRecipient = true,
                    )
                ),
                createdBy = 1L,
            )
        )
        flushAndClear()

        // when: 같은 key를 포함한 변수 리스트로 "전체 교체" 업데이트
        val updated = mailTemplateRepositoryImpl.update(
            templateId = created.id!!,
            template = MailTemplate(
                title = "제목-수정",
                bodyHtml = "<p>안녕하세요 {{${key}}}님</p>",
                variables = listOf(
                    TemplateVariable(
                        key = key,
                        type = VariableType.APPLICANT,
                        displayName = "지원자(수정)",
                        perRecipient = true,
                    )
                ),
                createdBy = 999L, // repository에서 기존 createdBy를 유지하는지 확인하기 위한 더미 값
            )
        )
        // then
        assertThat(updated).isNotNull
        assertThat(updated!!.variables).hasSize(1)
        assertThat(updated.variables[0].key).isEqualTo(key)
        assertThat(updated.variables[0].displayName).isEqualTo("지원자(수정)")
        assertThat(updated.createdBy).isEqualTo(1L)
    }

    @Test
    fun `유니크 제약은 동일 templateId에 동일 variableKey 저장을 막는다`() {
        // given
        val entity = MailTemplateEntityFactory.from(
            MailTemplate(
                title = "제목",
                bodyHtml = "<p>안녕하세요 {{${key}}}님</p>",
                variables = listOf(
                    TemplateVariable(key, VariableType.APPLICANT, "지원자", true),
                ),
                createdBy = 1L,
            )
        )
        val saved = entityManager.persist(entity)
        entityManager.flush()

        // when: 같은 template에 같은 key 변수를 또 추가하면 DB 제약 위반
        // (flush/clear 이후 detached 객체를 수정하면 INSERT가 발생하지 않으므로, managed 상태에서 추가해야 한다)
        val duplicated = TemplateVariableEntity(
            template = saved,
            variableKey = key,
            variableType = VariableType.APPLICANT,
            displayName = "지원자2",
            perRecipient = true,
        )
        saved.variables.add(duplicated)

        // then
        org.assertj.core.api.Assertions.assertThatThrownBy {
            entityManager.flush()
        }.isInstanceOfAny(DataIntegrityViolationException::class.java, ConstraintViolationException::class.java)
    }

    @Test
    fun `변수를 0개에서 N개로 업데이트할 수 있다`() {
        // given: 변수 없이 템플릿 생성
        val created = mailTemplateRepositoryImpl.save(
            MailTemplate(
                title = "제목",
                bodyHtml = "<p>본문</p>",
                variables = emptyList(),
                createdBy = 1L,
            )
        )
        flushAndClear()
        assertThat(created.variables).isEmpty()

        // when: 변수 2개 추가
        val updated = mailTemplateRepositoryImpl.update(
            templateId = created.id!!,
            template = MailTemplate(
                title = "제목",
                bodyHtml = "<p>안녕 {{${key}}} {{${key2}}}</p>",
                variables = listOf(
                    TemplateVariable(key, VariableType.APPLICANT, "지원자", true),
                    TemplateVariable(key2, VariableType.PARTNAME, "파트명", false),
                ),
                createdBy = 1L,
            )
        )

        // then
        assertThat(updated).isNotNull
        assertThat(updated!!.variables).hasSize(2)
    }

    @Test
    fun `변수를 N개에서 0개로 업데이트할 수 있다`() {
        // given: 변수 2개로 템플릿 생성
        val created = mailTemplateRepositoryImpl.save(
            MailTemplate(
                title = "제목",
                bodyHtml = "<p>안녕 {{${key}}} {{${key2}}}</p>",
                variables = listOf(
                    TemplateVariable(key, VariableType.APPLICANT, "지원자", true),
                    TemplateVariable(key2, VariableType.PARTNAME, "파트명", false),
                ),
                createdBy = 1L,
            )
        )
        flushAndClear()
        assertThat(created.variables).hasSize(2)

        // when: 변수 모두 제거
        val updated = mailTemplateRepositoryImpl.update(
            templateId = created.id!!,
            template = MailTemplate(
                title = "제목",
                bodyHtml = "<p>본문</p>",
                variables = emptyList(),
                createdBy = 1L,
            )
        )

        // then
        assertThat(updated).isNotNull
        assertThat(updated!!.variables).isEmpty()
    }

    @Test
    fun `변수 일부만 변경해도 정상 처리된다`() {
        // given: [A, B] 변수로 템플릿 생성
        val created = mailTemplateRepositoryImpl.save(
            MailTemplate(
                title = "제목",
                bodyHtml = "<p>{{${key}}} {{${key2}}}</p>",
                variables = listOf(
                    TemplateVariable(key, VariableType.APPLICANT, "지원자", true),
                    TemplateVariable(key2, VariableType.PARTNAME, "파트명", false),
                ),
                createdBy = 1L,
            )
        )
        flushAndClear()

        // when: [A, C]로 변경 (B 제거, C 추가, A 유지)
        val updated = mailTemplateRepositoryImpl.update(
            templateId = created.id!!,
            template = MailTemplate(
                title = "제목",
                bodyHtml = "<p>{{${key}}} {{${key3}}}</p>",
                variables = listOf(
                    TemplateVariable(key, VariableType.APPLICANT, "지원자(수정)", true),
                    TemplateVariable(key3, VariableType.DATE, "면접일", false),
                ),
                createdBy = 1L,
            )
        )

        // then
        assertThat(updated).isNotNull
        assertThat(updated!!.variables).hasSize(2)
        assertThat(updated.variables.map { it.key }).containsExactlyInAnyOrder(key, key3)
        assertThat(updated.variables.find { it.key == key }?.displayName).isEqualTo("지원자(수정)")
    }

    private fun flushAndClear() {
        entityManager.flush()
        entityManager.clear()
    }

    companion object {
        private const val key = "var-550e8400-e29b-41d4-a716-446655440000"
        private const val key2 = "var-660e8400-e29b-41d4-a716-446655440001"
        private const val key3 = "var-770e8400-e29b-41d4-a716-446655440002"
    }
}
