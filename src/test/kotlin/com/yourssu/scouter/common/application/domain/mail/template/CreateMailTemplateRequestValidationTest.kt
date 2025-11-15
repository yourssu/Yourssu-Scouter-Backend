package com.yourssu.scouter.common.application.domain.mail.template

import jakarta.validation.Validation
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test

class CreateMailTemplateRequestValidationTest {

    companion object {
        private val factory = Validation.buildDefaultValidatorFactory()
        private val validator = factory.validator

        @JvmStatic
        @AfterAll
        fun tearDown() {
            factory.close()
        }
    }

    @Test
    fun `bodyHtml 길이 2000자는 통과한다`() {
        // given
        val dto = CreateMailTemplateRequest(
            title = "제목",
            bodyHtml = "a".repeat(2000),
            variables = emptyList(),
        )

        // when
        val violations = validator.validate(dto)

        // then
        assertThat(violations).isEmpty()
    }

    @Test
    fun `bodyHtml 길이 2001자는 검증 에러가 발생한다`() {
        // given
        val dto = CreateMailTemplateRequest(
            title = "제목",
            bodyHtml = "a".repeat(2001),
            variables = emptyList(),
        )

        // when
        val violations = validator.validate(dto)

        // then
        assertThat(violations).isNotEmpty
        assertThat(violations.any { it.propertyPath.toString() == "bodyHtml" }).isTrue()
    }
}
