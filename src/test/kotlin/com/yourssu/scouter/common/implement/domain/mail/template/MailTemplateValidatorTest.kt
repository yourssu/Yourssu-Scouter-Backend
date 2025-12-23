package com.yourssu.scouter.common.implement.domain.mail.template

import com.yourssu.scouter.common.implement.support.exception.InvalidTemplateException
import org.assertj.core.api.Assertions.assertThatCode
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import java.util.concurrent.atomic.AtomicLong

@Suppress("NonAsciiCharacters")
class MailTemplateValidatorTest {

    private val keyCounter = AtomicLong(System.currentTimeMillis())

    /**
     * 테스트용 변수 키 생성
     * 실제 DB에서 생성하는 방식과 다를 수 있으나, 형식 검증 테스트에는 충분함
     * 형식: var-{숫자}
     */
    private fun generateVarKey(): String = "var-${keyCounter.incrementAndGet()}"

    @Test
    fun `본문에 등장하는 플레이스홀더가 변수 목록에 없으면 예외가 발생한다`() {
        // given
        val interviewKey = generateVarKey()
        val otherKey = generateVarKey()
        val template = MailTemplate(
            title = "합격 안내",
            bodyHtml = "<p>면접 일정은 {{$interviewKey}} 입니다.</p>",
            variables = listOf(
                TemplateVariable(
                    key = otherKey,
                    type = VariableType.TEXT,
                    displayName = "기타",
                    perRecipient = false,
                    requiresUserInput = true,
                ),
            ),
            createdBy = 1L,
        )

        // when & then
        assertThatThrownBy { MailTemplateValidator.validate(template) }
            .isInstanceOf(InvalidTemplateException::class.java)
            .hasMessageContaining("Variables missing for placeholders")
    }

    @Test
    fun `변수로 선언되어 있으나 본문에서 사용하지 않아도 저장이 허용된다`() {
        // given
        val interviewKey = generateVarKey()
        val template = MailTemplate(
            title = "합격 안내",
            bodyHtml = "<p>안녕하세요.</p>",
            variables = listOf(
                TemplateVariable(
                    key = interviewKey,
                    type = VariableType.DATE,
                    displayName = "면접 일시",
                    perRecipient = true,
                    requiresUserInput = true,
                ),
            ),
            createdBy = 1L,
        )

        // when & then
        assertThatCode { MailTemplateValidator.validate(template) }
            .doesNotThrowAnyException()
    }

    @Test
    fun `변수 중 일부만 본문에서 사용되어도 저장이 허용된다`() {
        // given
        val interviewKey = generateVarKey()
        val extraKey = generateVarKey()
        val template = MailTemplate(
            title = "합격 안내",
            bodyHtml = "<p>면접 일정은 {{$interviewKey}} 입니다.</p>",
            variables = listOf(
                TemplateVariable(
                    key = interviewKey,
                    type = VariableType.DATE,
                    displayName = "면접 일시",
                    perRecipient = true,
                    requiresUserInput = true,
                ),
                TemplateVariable(
                    key = extraKey,
                    type = VariableType.TEXT,
                    displayName = "추가 안내 문구",
                    perRecipient = false,
                    requiresUserInput = true,
                ),
            ),
            createdBy = 1L,
        )

        // when & then
        assertThatCode { MailTemplateValidator.validate(template) }
            .doesNotThrowAnyException()
    }

    @Test
    fun `중복된 변수 키가 있으면 예외가 발생한다`() {
        // given
        val interviewKey = generateVarKey()
        val template = MailTemplate(
            title = "합격 안내",
            bodyHtml = "<p>면접 일정은 {{$interviewKey}} 입니다.</p>",
            variables = listOf(
                TemplateVariable(
                    key = interviewKey,
                    type = VariableType.DATE,
                    displayName = "면접 일시1",
                    perRecipient = true,
                    requiresUserInput = true,
                ),
                TemplateVariable(
                    key = interviewKey,
                    type = VariableType.DATE,
                    displayName = "면접 일시2",
                    perRecipient = true,
                    requiresUserInput = true,
                ),
            ),
            createdBy = 1L,
        )

        // when & then
        assertThatThrownBy { MailTemplateValidator.validate(template) }
            .isInstanceOf(InvalidTemplateException::class.java)
            .hasMessageContaining("Duplicate variable keys")
    }

    @Test
    fun `var-로 시작하지 않는 키는 예외가 발생한다`() {
        // given
        val template = MailTemplate(
            title = "합격 안내",
            bodyHtml = "<p>지원자 {{applicant}}님, 안녕하세요.</p>",
            variables = listOf(
                TemplateVariable(
                    key = "applicant",
                    type = VariableType.APPLICANT,
                    displayName = "지원자 정보",
                    perRecipient = true,
                    requiresUserInput = false,
                ),
            ),
            createdBy = 1L,
        )

        // when & then
        assertThatThrownBy { MailTemplateValidator.validate(template) }
            .isInstanceOf(InvalidTemplateException::class.java)
            .hasMessageContaining("must start with 'var-'")
    }

    @Test
    fun `숫자 형식이 아닌 키는 예외가 발생한다`() {
        // given
        val template = MailTemplate(
            title = "합격 안내",
            bodyHtml = "<p>면접 일정은 {{var-interview-datetime}} 입니다.</p>",
            variables = listOf(
                TemplateVariable(
                    key = "var-interview-datetime",
                    type = VariableType.DATE,
                    displayName = "면접 일시",
                    perRecipient = true,
                    requiresUserInput = true,
                ),
            ),
            createdBy = 1L,
        )

        // when & then
        assertThatThrownBy { MailTemplateValidator.validate(template) }
            .isInstanceOf(InvalidTemplateException::class.java)
            .hasMessageContaining("must follow the format 'var-{number}'")
    }

    @Test
    fun `UUID 형식의 키는 예외가 발생한다`() {
        // given
        val uuidKey = "var-550e8400-e29b-41d4-a716-446655440000"
        val template = MailTemplate(
            title = "합격 안내",
            bodyHtml = "<p>면접 일정은 {{$uuidKey}} 입니다.</p>",
            variables = listOf(
                TemplateVariable(
                    key = uuidKey,
                    type = VariableType.DATE,
                    displayName = "면접 일시",
                    perRecipient = true,
                    requiresUserInput = true,
                ),
            ),
            createdBy = 1L,
        )

        // when & then
        assertThatThrownBy { MailTemplateValidator.validate(template) }
            .isInstanceOf(InvalidTemplateException::class.java)
            .hasMessageContaining("must follow the format 'var-{number}'")
    }

    @Test
    fun `숫자 형식의 키는 허용된다`() {
        // given
        val numericKey = "var-1762579979965"
        val template = MailTemplate(
            title = "합격 안내",
            bodyHtml = "<p>면접 일정은 {{$numericKey}} 입니다.</p>",
            variables = listOf(
                TemplateVariable(
                    key = numericKey,
                    type = VariableType.DATE,
                    displayName = "면접 일시",
                    perRecipient = true,
                    requiresUserInput = true,
                ),
            ),
            createdBy = 1L,
        )

        // when & then
        assertThatCode { MailTemplateValidator.validate(template) }
            .doesNotThrowAnyException()
    }

    @Test
    fun `자동 채움 변수가 잘못된 타입이면 예외가 발생한다`() {
        // given
        // requiresUserInput=false인데 자동 채움 변수 타입이 아닌 타입을 사용
        val invalidType = VariableType.USER_INPUT_TYPES.first() // 사용자 입력 변수 타입 중 하나
        val applicantKey = generateVarKey()
        val template = MailTemplate(
            title = "합격 안내",
            bodyHtml = "<p>지원자 {{$applicantKey}}님, 안녕하세요.</p>",
            variables = listOf(
                TemplateVariable(
                    key = applicantKey,
                    type = invalidType, // 자동 채움 변수 타입이 아님
                    displayName = "지원자 정보",
                    perRecipient = true,
                    requiresUserInput = false, // 자동 채움 변수로 설정했지만 타입이 맞지 않음
                ),
            ),
            createdBy = 1L,
        )

        // when & then
        assertThatThrownBy { MailTemplateValidator.validate(template) }
            .isInstanceOf(InvalidTemplateException::class.java)
            .hasMessageContaining("must have an auto-fill type")
    }

    @Test
    fun `사용자 입력 변수가 자동 채움 타입이면 예외가 발생한다`() {
        // given
        // requiresUserInput=true인데 자동 채움 변수 타입을 사용
        val invalidType = VariableType.AUTO_FILL_TYPES.first() // 자동 채움 변수 타입 중 하나
        val interviewKey = generateVarKey()
        val template = MailTemplate(
            title = "합격 안내",
            bodyHtml = "<p>면접 일정은 {{$interviewKey}} 입니다.</p>",
            variables = listOf(
                TemplateVariable(
                    key = interviewKey,
                    type = invalidType, // 자동 채움 변수 타입
                    displayName = "면접 일시",
                    perRecipient = true,
                    requiresUserInput = true, // 사용자 입력 변수로 설정했지만 타입이 맞지 않음
                ),
            ),
            createdBy = 1L,
        )

        // when & then
        assertThatThrownBy { MailTemplateValidator.validate(template) }
            .isInstanceOf(InvalidTemplateException::class.java)
            .hasMessageContaining("must have a user-input type")
    }

    @Test
    fun `자동 채움 변수가 APPLICANT 타입이면 검증 통과한다`() {
        // given
        val applicantKey = generateVarKey()
        val template = MailTemplate(
            title = "합격 안내",
            bodyHtml = "<p>지원자 {{$applicantKey}}님, 안녕하세요.</p>",
            variables = listOf(
                TemplateVariable(
                    key = applicantKey,
                    type = VariableType.APPLICANT,
                    displayName = "지원자 정보",
                    perRecipient = true,
                    requiresUserInput = false,
                ),
            ),
            createdBy = 1L,
        )

        // when & then
        assertThatCode { MailTemplateValidator.validate(template) }
            .doesNotThrowAnyException()
    }

    @Test
    fun `자동 채움 변수가 PARTNAME 타입이면 검증 통과한다`() {
        // given
        val partNameKey = generateVarKey()
        val template = MailTemplate(
            title = "합격 안내",
            bodyHtml = "<p>{{$partNameKey}} 팀에 합격하셨습니다.</p>",
            variables = listOf(
                TemplateVariable(
                    key = partNameKey,
                    type = VariableType.PARTNAME,
                    displayName = "파트 이름",
                    perRecipient = false,
                    requiresUserInput = false,
                ),
            ),
            createdBy = 1L,
        )

        // when & then
        assertThatCode { MailTemplateValidator.validate(template) }
            .doesNotThrowAnyException()
    }
}
