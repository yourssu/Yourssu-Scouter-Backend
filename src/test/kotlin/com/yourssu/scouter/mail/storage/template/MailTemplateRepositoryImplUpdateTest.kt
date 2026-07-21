package com.yourssu.scouter.mail.storage.template

import com.yourssu.scouter.mail.implement.template.MailTemplate
import com.yourssu.scouter.mail.implement.template.RenderableText
import com.yourssu.scouter.mail.implement.template.TemplateVariable
import com.yourssu.scouter.mail.implement.template.VariableType
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.hibernate.exception.ConstraintViolationException
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager
import org.springframework.context.annotation.Import
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.test.context.TestPropertySource

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
                subject = RenderableText("메일 제목 {{$key}}"),
                bodyHtml = "<p>안녕하세요 {{${key}}}님</p>",
                variables = listOf(
                    TemplateVariable.ApplicantBound(key = key, displayName = "지원자", attributeKey = "applicant.name"),
                ),
                createdBy = 1L,
            ),
        )
        flushAndClear()

        // when: 같은 key를 포함한 변수 리스트로 "전체 교체" 업데이트
        val updated = mailTemplateRepositoryImpl.update(
            templateId = created.id!!,
            template = MailTemplate(
                title = "제목-수정",
                subject = RenderableText("메일 제목(수정) {{$key}}"),
                bodyHtml = "<p>안녕하세요 {{${key}}}님</p>",
                variables = listOf(
                    TemplateVariable.ApplicantBound(key = key, displayName = "지원자(수정)", attributeKey = "applicant.name"),
                ),
                createdBy = 999L, // repository에서 기존 createdBy를 유지하는지 확인하기 위한 더미 값
            ),
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
                subject = RenderableText("제목"),
                bodyHtml = "<p>안녕하세요 {{${key}}}님</p>",
                variables = listOf(
                    TemplateVariable.ApplicantBound(key = key, displayName = "지원자", attributeKey = "applicant.name"),
                ),
                createdBy = 1L,
            ),
        )
        val saved = entityManager.persist(entity)
        entityManager.flush()

        // when: 같은 template에 같은 key 변수를 또 추가하면 DB 제약 위반
        val duplicated = TemplateVariableEntity(
            template = saved,
            variableKey = key,
            variableType = VariableType.APPLICANT,
            displayName = "지원자2",
            perRecipient = false,
            attributeKey = "applicant.name",
        )
        saved.variables.add(duplicated)

        // then
        assertThatThrownBy {
            entityManager.flush()
        }.isInstanceOfAny(DataIntegrityViolationException::class.java, ConstraintViolationException::class.java)
    }

    @Test
    fun `변수를 0개에서 N개로 업데이트할 수 있다`() {
        // given: 변수 없이 템플릿 생성
        val created = mailTemplateRepositoryImpl.save(
            MailTemplate(
                title = "제목",
                subject = RenderableText("제목"),
                bodyHtml = "<p>본문</p>",
                variables = emptyList(),
                createdBy = 1L,
            ),
        )
        flushAndClear()
        assertThat(created.variables).isEmpty()

        // when: 변수 2개 추가
        val updated = mailTemplateRepositoryImpl.update(
            templateId = created.id!!,
            template = MailTemplate(
                title = "제목",
                subject = RenderableText("{{$key}} 파트"),
                bodyHtml = "<p>안녕 {{${key}}} {{${key2}}}</p>",
                variables = listOf(
                    TemplateVariable.ApplicantBound(key = key, displayName = "지원자", attributeKey = "applicant.name"),
                    TemplateVariable.PartName(key = key2, displayName = "파트명"),
                ),
                createdBy = 1L,
            ),
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
                subject = RenderableText("{{$key}} 파트"),
                bodyHtml = "<p>안녕 {{${key}}} {{${key2}}}</p>",
                variables = listOf(
                    TemplateVariable.ApplicantBound(key = key, displayName = "지원자", attributeKey = "applicant.name"),
                    TemplateVariable.PartName(key = key2, displayName = "파트명"),
                ),
                createdBy = 1L,
            ),
        )
        flushAndClear()
        assertThat(created.variables).hasSize(2)

        // when: 변수 모두 제거
        val updated = mailTemplateRepositoryImpl.update(
            templateId = created.id!!,
            template = MailTemplate(
                title = "제목",
                subject = RenderableText("제목"),
                bodyHtml = "<p>본문</p>",
                variables = emptyList(),
                createdBy = 1L,
            ),
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
                subject = RenderableText("{{$key}} 파트"),
                bodyHtml = "<p>{{${key}}} {{${key2}}}</p>",
                variables = listOf(
                    TemplateVariable.ApplicantBound(key = key, displayName = "지원자", attributeKey = "applicant.name"),
                    TemplateVariable.PartName(key = key2, displayName = "파트명"),
                ),
                createdBy = 1L,
            ),
        )
        flushAndClear()

        // when: [A, C]로 변경 (B 제거, C 추가, A 유지)
        val updated = mailTemplateRepositoryImpl.update(
            templateId = created.id!!,
            template = MailTemplate(
                title = "제목",
                subject = RenderableText("{{$key}} 파트"),
                bodyHtml = "<p>{{${key}}} {{${key3}}}</p>",
                variables = listOf(
                    TemplateVariable.ApplicantBound(key = key, displayName = "지원자(수정)", attributeKey = "applicant.name"),
                    TemplateVariable.UserInput(key = key3, displayName = "면접일", type = VariableType.DATE, perRecipient = false),
                ),
                createdBy = 1L,
            ),
        )

        // then
        assertThat(updated).isNotNull
        assertThat(updated!!.variables).hasSize(2)
        assertThat(updated.variables.map { it.key }).containsExactlyInAnyOrder(key, key3)
        assertThat(updated.variables.find { it.key == key }?.displayName).isEqualTo("지원자(수정)")
    }

    @Test
    fun `저장된 ApplicantBound 변수는 attributeKey를 포함해 복원된다`() {
        // given
        val saved = mailTemplateRepositoryImpl.save(
            MailTemplate(
                title = "제목",
                subject = RenderableText("제목"),
                bodyHtml = "<p>{{$key}}</p>",
                variables = listOf(
                    TemplateVariable.ApplicantBound(key = key, displayName = "지원자 이름", attributeKey = "applicant.name"),
                ),
                createdBy = 1L,
            ),
        )
        flushAndClear()

        // when
        val loaded = mailTemplateRepositoryImpl.findById(saved.id!!)

        // then
        val variable = loaded!!.variables[0] as TemplateVariable.ApplicantBound
        assertThat(variable.attributeKey).isEqualTo("applicant.name")
    }

    @Test
    fun `저장된 PartName 변수는 TemplateVariable_PartName 타입으로 복원된다`() {
        // given
        val saved = mailTemplateRepositoryImpl.save(
            MailTemplate(
                title = "제목",
                subject = RenderableText("제목"),
                bodyHtml = "<p>{{$key}}</p>",
                variables = listOf(TemplateVariable.PartName(key = key, displayName = "파트명")),
                createdBy = 1L,
            ),
        )
        flushAndClear()

        // when
        val loaded = mailTemplateRepositoryImpl.findById(saved.id!!)

        // then
        assertThat(loaded!!.variables[0]).isInstanceOf(TemplateVariable.PartName::class.java)
    }

    @Test
    fun `저장된 subject는 그대로 복원된다`() {
        // given
        val savedTemplate = mailTemplateRepositoryImpl.save(
            MailTemplate(
                title = "제목",
                subject = RenderableText("[유어슈] 면접 안내입니다"),
                bodyHtml = "<p>본문</p>",
                variables = emptyList(),
                createdBy = 1L,
            ),
        )
        flushAndClear()

        // when
        val loaded = mailTemplateRepositoryImpl.findById(savedTemplate.id!!)

        // then
        assertThat(loaded!!.subject.raw).isEqualTo("[유어슈] 면접 안내입니다")
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
