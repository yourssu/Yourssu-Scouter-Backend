package com.yourssu.scouter.common.implement.domain.mail.template

import com.yourssu.scouter.common.implement.support.exception.InvalidTemplateException
import org.assertj.core.api.Assertions.assertThatCode
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test

@Suppress("NonAsciiCharacters")
class MailTemplateValidatorTest {

    @Test
    fun `본문에 등장하는 플레이스홀더가 변수 목록에 없으면 예외가 발생한다`() {
        // given
        val template = MailTemplate(
            title = "합격 안내",
            bodyHtml = "<p>면접 일정은 {{var-interview-datetime}} 입니다.</p>",
            variables = listOf(
                TemplateVariable(
                    key = "var-other",
                    type = VariableType.TEXT,
                    displayName = "기타",
                    perRecipient = false,
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
        val template = MailTemplate(
            title = "합격 안내",
            bodyHtml = "<p>안녕하세요.</p>",
            variables = listOf(
                TemplateVariable(
                    key = "var-interview-datetime",
                    type = VariableType.DATE,
                    displayName = "면접 일시",
                    perRecipient = true,
                ),
            ),
            createdBy = 1L,
        )

        // when & then
        assertThatCode { MailTemplateValidator.validate(template) }
            .doesNotThrowAnyException()
    }
}
