package com.yourssu.scouter.hrms.application.domain.member

import com.yourssu.scouter.hrms.business.domain.member.MemberSyncResult
import java.net.URI
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.http.HttpStatus

class MemberSyncExtensionsTest {

    @Test
    fun `toResponse는 createdCount가 1 이상이면 201과 Location을 반환한다`() {
        // given
        val result = MemberSyncResult(failureMessages = emptyList(), createdCount = 2)
        val location = URI.create("/members")

        // when
        val response = result.toResponse(location)

        // then
        assertThat(response.statusCode).isEqualTo(HttpStatus.CREATED)
        assertThat(response.headers.location?.toString()).isEqualTo("/members")
        assertThat(response.body!!.createdCount).isEqualTo(2)
    }

    @Test
    fun `toResponse는 createdCount가 0이면 200을 반환한다`() {
        // given
        val result = MemberSyncResult(failureMessages = listOf("no changes"), createdCount = 0)
        val location = URI.create("/members")

        // when
        val response = result.toResponse(location)

        // then
        assertThat(response.statusCode).isEqualTo(HttpStatus.OK)
        assertThat(response.headers.location).isNull()
        assertThat(response.body!!.failureMessages).containsExactly("no changes")
        assertThat(response.body!!.createdCount).isEqualTo(0)
    }
}
