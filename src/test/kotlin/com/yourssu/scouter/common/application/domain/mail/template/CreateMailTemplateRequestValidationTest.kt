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
    fun `bodyHtml가 100,000자여도 검증을 통과한다`() {
        // given
        val dto = CreateMailTemplateRequest(
            title = "제목",
            bodyHtml = "a".repeat(100_000),
            variables = emptyList(),
        )

        // when
        val violations = validator.validate(dto)

        // then
        assertThat(violations).isEmpty()
    }

    @Test
    fun `bodyHtml 이 비어있으면 검증 에러가 발생한다`() {
        // given
        val dto = CreateMailTemplateRequest(
            title = "제목",
            bodyHtml = "",
            variables = emptyList(),
        )

        // when
        val violations = validator.validate(dto)

        // then
        assertThat(violations).isNotEmpty
        assertThat(violations.any { it.propertyPath.toString() == "bodyHtml" }).isTrue()
    }

    @Test
    fun `bodyHtml 길이 100001자는 검증 에러가 발생한다`() {
        // given
        val dto = CreateMailTemplateRequest(
            title = "제목",
            bodyHtml = "a".repeat(100_001),
            variables = emptyList(),
        )

        // when
        val violations = validator.validate(dto)

        // then
        assertThat(violations).isNotEmpty
        assertThat(violations.any { it.propertyPath.toString() == "bodyHtml" }).isTrue()
    }
}
