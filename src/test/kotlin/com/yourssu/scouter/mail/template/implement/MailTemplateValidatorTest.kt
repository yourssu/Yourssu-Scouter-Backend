package com.yourssu.scouter.mail.template.implement

import com.yourssu.scouter.mail.template.implement.RecipientAttributeResolver
import com.yourssu.scouter.mail.support.exception.InvalidTemplateException
import org.assertj.core.api.Assertions.assertThatCode
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import java.util.UUID

@Suppress("NonAsciiCharacters")
class MailTemplateValidatorTest {

    private val resolver = mock<RecipientAttributeResolver>().also {
        whenever(it.availableKeys()).thenReturn(
            setOf("applicant.name", "applicant.email", "applicant.part.name"),
        )
    }
    private val validator = MailTemplateValidator(resolver)

    private fun key() = "var-${UUID.randomUUID()}"

    private fun userInputVar(key: String, perRecipient: Boolean = false) =
        TemplateVariable.UserInput(key = key, displayName = "표시명", type = VariableType.TEXT, perRecipient = perRecipient)

    private fun template(
        subject: String = "",
        bodyHtml: String,
        variables: List<TemplateVariable>,
    ) = MailTemplate(
        title = "테스트",
        subject = RenderableText(subject),
        bodyHtml = bodyHtml,
        variables = variables,
        createdBy = 1L,
    )

    // ── 플레이스홀더 일관성 검증 ──────────────────────────────────────

    @Test
    fun `body 플레이스홀더가 변수 목록에 없으면 예외가 발생한다`() {
        val k = key()
        val other = key()
        assertThatThrownBy {
            validator.validate(template(
                bodyHtml = "<p>{{$k}}</p>",
                variables = listOf(userInputVar(other)),
            ))
        }.isInstanceOf(InvalidTemplateException::class.java)
            .hasMessageContaining("선언되지 않은 플레이스홀더")
    }

    @Test
    fun `subject 플레이스홀더가 변수 목록에 없으면 예외가 발생한다`() {
        val k = key()
        assertThatThrownBy {
            validator.validate(template(
                subject = "안녕 {{$k}}",
                bodyHtml = "<p>본문</p>",
                variables = emptyList(),
            ))
        }.isInstanceOf(InvalidTemplateException::class.java)
            .hasMessageContaining("선언되지 않은 플레이스홀더")
    }

    @Test
    fun `선언된 변수가 subject 또는 body에서 사용되지 않으면 예외가 발생한다`() {
        val k = key()
        assertThatThrownBy {
            validator.validate(template(
                bodyHtml = "<p>본문</p>",
                variables = listOf(userInputVar(k)),
            ))
        }.isInstanceOf(InvalidTemplateException::class.java)
            .hasMessageContaining("사용되지 않는 변수")
    }

    @Test
    fun `변수가 subject에만 사용되어도 검증을 통과한다`() {
        val k = key()
        assertThatCode {
            validator.validate(template(
                subject = "안녕 {{$k}}",
                bodyHtml = "<p>본문</p>",
                variables = listOf(userInputVar(k)),
            ))
        }.doesNotThrowAnyException()
    }

    @Test
    fun `변수가 body에만 사용되어도 검증을 통과한다`() {
        val k = key()
        assertThatCode {
            validator.validate(template(
                bodyHtml = "<p>{{$k}}</p>",
                variables = listOf(userInputVar(k)),
            ))
        }.doesNotThrowAnyException()
    }

    @Test
    fun `변수와 플레이스홀더가 모두 없으면 검증을 통과한다`() {
        assertThatCode {
            validator.validate(template(bodyHtml = "<p>안녕하세요.</p>", variables = emptyList()))
        }.doesNotThrowAnyException()
    }

    // ── 변수 키 형식 검증 ──────────────────────────────────────────────

    @Test
    fun `UUID 형식의 키는 허용된다`() {
        val k = "var-550e8400-e29b-41d4-a716-446655440000"
        assertThatCode {
            validator.validate(template(
                bodyHtml = "<p>{{$k}}</p>",
                variables = listOf(userInputVar(k)),
            ))
        }.doesNotThrowAnyException()
    }

    @Test
    fun `var- 로 시작하지 않는 키는 예외가 발생한다`() {
        val k = "applicant"
        assertThatThrownBy {
            validator.validate(template(
                bodyHtml = "<p>{{$k}}</p>",
                variables = listOf(userInputVar(k)),
            ))
        }.isInstanceOf(InvalidTemplateException::class.java)
            .hasMessageContaining("변수 키 형식이 올바르지 않습니다")
    }

    @Test
    fun `UUID 형식이 아닌 키는 예외가 발생한다`() {
        val k = "var-interview-datetime"
        assertThatThrownBy {
            validator.validate(template(
                bodyHtml = "<p>{{$k}}</p>",
                variables = listOf(userInputVar(k)),
            ))
        }.isInstanceOf(InvalidTemplateException::class.java)
            .hasMessageContaining("변수 키 형식이 올바르지 않습니다")
    }

    // ── 중복 키 검증 ──────────────────────────────────────────────────

    @Test
    fun `중복된 변수 키가 있으면 예외가 발생한다`() {
        val k = key()
        assertThatThrownBy {
            validator.validate(template(
                bodyHtml = "<p>{{$k}}</p>",
                variables = listOf(userInputVar(k), userInputVar(k)),
            ))
        }.isInstanceOf(InvalidTemplateException::class.java)
            .hasMessageContaining("중복된 변수 키")
    }

    // ── ApplicantBound 키 검증 ────────────────────────────────────────

    @Test
    fun `ApplicantBound 변수의 attributeKey가 resolver에 있으면 검증을 통과한다`() {
        val k = key()
        assertThatCode {
            validator.validate(template(
                bodyHtml = "<p>{{$k}}</p>",
                variables = listOf(
                    TemplateVariable.ApplicantBound(key = k, displayName = "지원자 이름", attributeKey = "applicant.name"),
                ),
            ))
        }.doesNotThrowAnyException()
    }

    @Test
    fun `ApplicantBound 변수의 attributeKey가 resolver에 없으면 예외가 발생한다`() {
        val k = key()
        assertThatThrownBy {
            validator.validate(template(
                bodyHtml = "<p>{{$k}}</p>",
                variables = listOf(
                    TemplateVariable.ApplicantBound(key = k, displayName = "학번", attributeKey = "applicant.unknown"),
                ),
            ))
        }.isInstanceOf(InvalidTemplateException::class.java)
            .hasMessageContaining("유효하지 않은 attributeKey")
    }

    // ── PartName 검증 ─────────────────────────────────────────────────

    @Test
    fun `PartName 변수는 검증을 통과한다`() {
        val k = key()
        assertThatCode {
            validator.validate(template(
                bodyHtml = "<p>{{$k}} 파트</p>",
                variables = listOf(TemplateVariable.PartName(key = k, displayName = "파트명")),
            ))
        }.doesNotThrowAnyException()
    }

    // ── UserInput 타입 검증 ───────────────────────────────────────────

    @Test
    fun `UserInput에 AUTO_FILL 타입을 사용하면 예외가 발생한다`() {
        val k = key()
        assertThatThrownBy {
            validator.validate(template(
                bodyHtml = "<p>{{$k}}</p>",
                variables = listOf(
                    TemplateVariable.UserInput(key = k, displayName = "잘못된", type = VariableType.APPLICANT, perRecipient = false),
                ),
            ))
        }.isInstanceOf(InvalidTemplateException::class.java)
            .hasMessageContaining("AUTO_FILL 타입 사용 불가")
    }
}
