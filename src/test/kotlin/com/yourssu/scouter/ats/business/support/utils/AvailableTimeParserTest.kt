package com.yourssu.scouter.ats.business.support.utils

import com.yourssu.scouter.common.implement.support.google.ResponseItem
import com.yourssu.scouter.common.implement.support.initialization.ApplicantAvailableTimeMap
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import java.time.LocalDateTime
import java.time.ZoneId
import kotlin.test.assertEquals

class AvailableTimeParserTest {

    val availableTimeMap: ApplicantAvailableTimeMap = ApplicantAvailableTimeMap(
        time = listOf("(\\d+)시\\s*~\\s*(\\d+)시"),
        days = listOf("yyyy M월 d일 E요일 HH:mm", "yyyy MM.dd HH:mm")
    )

    val parser = AvailableTimeParser(availableTimeMap)

    @Test
    @DisplayName("지원자의 면접 가능시간을 나타내는 ResponseItem 배열을 Instant 배열로 변환한다.")
    fun parseTimeOnlyHourSuccessTest() {
        // given
        val item1 = ResponseItem(":09.24", "12시~15시")
        val responseItems: List<ResponseItem> = listOf(item1)

        val expectedOutput = listOf(
            LocalDateTime.of(2025, 9, 24, 12, 0).atZone(ZoneId.of("Asia/Seoul")).toInstant(),
            LocalDateTime.of(2025, 9, 24, 13, 0, 0).atZone(ZoneId.of("Asia/Seoul")).toInstant(),
            LocalDateTime.of(2025, 9, 24, 14, 0).atZone(ZoneId.of("Asia/Seoul")).toInstant(),
        )
        // when
        val localDateTimes = parser.parse(responseItems)
        // then
        assertEquals(expectedOutput, localDateTimes)
    }

    @Test
    @DisplayName("availableTimeMap에 지원하는 날짜 형식이 없는 경우 빈 배열을 반환한다.")
    fun parseInvalidDayFormatTest() {
        // given
        val item1 = ResponseItem(":9/24", "12시~15시")
        val responseItems: List<ResponseItem> = listOf(item1)
        // when
        val localDateTimes = parser.parse(responseItems)
        // then
        assertEquals(emptyList(), localDateTimes)
    }

    @Test
    @DisplayName("availableTimeMap에 지원하는 시간 형식이 없는 경우 빈 배열을 반환한다.")
    fun parseInvalidTimeFormatTest() {
        // given
        val item1 = ResponseItem(":9.24", "12:00~15:00")
        val responseItems: List<ResponseItem> = listOf(item1)
        // when
        val localDateTimes = parser.parse(responseItems)
        // then
        assertEquals(emptyList(), localDateTimes)
    }

    @Test
    @DisplayName("빈 배열이 입력으로 들어올 경우, 빈 배열을 반환한다.")
    fun parseEmptyListTest() {
        // given
        val responseItems: List<ResponseItem> = emptyList()
        // when
        val localDateTimes = parser.parse(responseItems)
        // then
        assertEquals(emptyList(), localDateTimes)
    }

    @Test
    @DisplayName("시간이 역순으로 들어올 경우, 빈 배열을 반환한다.")
    fun parseReversedTimeTest() {
        // given
        val item1 = ResponseItem(":9.24", answer = "14시~12시")
        val responseItems = listOf(item1)
        // when
        val localDateTimes = parser.parse(responseItems)
        // then
        assertEquals(emptyList(), localDateTimes)
    }

    @Test
    @DisplayName("응답이 '불가'인 경우 빈 배열을 반환한다.")
    fun parseUnavailableAnswerTest() {
        // given
        val item1 = ResponseItem(":9.24", answer = "불가")
        val responseItems = listOf(item1)
        // when
        val localDateTimes = parser.parse(responseItems)
        // then
        assertEquals(emptyList(), localDateTimes)
    }

    @Test
    @DisplayName("응답이 '상관없음'인 경우 00:00부터 23:00까지 모든 시간을 반환한다.")
    fun parseAllAvailableAnswerTest() {
        // given
        val item1 = ResponseItem(":09.24", answer = "상관없음")
        val responseItems = listOf(item1)
        // when
        val localDateTimes = parser.parse(responseItems)
        // then
        assertEquals(24, localDateTimes.size)
        assertEquals(
            LocalDateTime.of(2025, 9, 24, 0, 0).atZone(ZoneId.of("Asia/Seoul")).toInstant(),
            localDateTimes.first()
        )
        assertEquals(
            LocalDateTime.of(2025, 9, 24, 23, 0).atZone(ZoneId.of("Asia/Seoul")).toInstant(),
            localDateTimes.last()
        )
    }

    @Test
    @DisplayName("쉼표로 구분된 여러 시간을 파싱한다.")
    fun parseMultipleTimesTest() {
        // given
        val item1 = ResponseItem(":09.24", answer = "12시~14시, 16시~18시")
        val responseItems = listOf(item1)
        val expectedOutput = listOf(
            LocalDateTime.of(2025, 9, 24, 12, 0).atZone(ZoneId.of("Asia/Seoul")).toInstant(),
            LocalDateTime.of(2025, 9, 24, 13, 0).atZone(ZoneId.of("Asia/Seoul")).toInstant(),
            LocalDateTime.of(2025, 9, 24, 16, 0).atZone(ZoneId.of("Asia/Seoul")).toInstant(),
            LocalDateTime.of(2025, 9, 24, 17, 0).atZone(ZoneId.of("Asia/Seoul")).toInstant()
        )
        // when
        val localDateTimes = parser.parse(responseItems)
        // then
        assertEquals(expectedOutput, localDateTimes)
    }

    @Test
    @DisplayName("여러 ResponseItem이 있을 때 모든 시간을 합쳐서 반환한다.")
    fun parseMultipleResponseItemsTest() {
        // given
        val item1 = ResponseItem(":09.24", answer = "12시~14시")
        val item2 = ResponseItem(":09.25", answer = "16시~18시")
        val responseItems = listOf(item1, item2)
        val expectedOutput = listOf(
            LocalDateTime.of(2025, 9, 24, 12, 0, 0).atZone(ZoneId.of("Asia/Seoul")).toInstant(),
            LocalDateTime.of(2025, 9, 24, 13, 0, 0).atZone(ZoneId.of("Asia/Seoul")).toInstant(),
            LocalDateTime.of(2025, 9, 25, 16, 0, 0).atZone(ZoneId.of("Asia/Seoul")).toInstant(),
            LocalDateTime.of(2025, 9, 25, 17, 0, 0).atZone(ZoneId.of("Asia/Seoul")).toInstant(),
        )
        // when
        val localDateTimes = parser.parse(responseItems)
        // then
        assertEquals(expectedOutput, localDateTimes)
    }

    @Test
    @DisplayName("각기 다른 날짜 형식을 사용해도 파싱한다.")
    fun parseAlternateDateFormatTest() {
        // given
        val item1 = ResponseItem(":09.24", answer = "12시~15시")
        val item2 = ResponseItem(":9월 25일 목요일", answer = "12시~13시")
        val responseItems = listOf(item1, item2)
        val expectedOutput = listOf(
            LocalDateTime.of(2025, 9, 24, 12, 0, 0).atZone(ZoneId.of("Asia/Seoul")).toInstant(),
            LocalDateTime.of(2025, 9, 24, 13, 0, 0).atZone(ZoneId.of("Asia/Seoul")).toInstant(),
            LocalDateTime.of(2025, 9, 24, 14, 0, 0).atZone(ZoneId.of("Asia/Seoul")).toInstant(),
            LocalDateTime.of(2025, 9, 25, 12, 0, 0).atZone(ZoneId.of("Asia/Seoul")).toInstant()
        )
        // when
        val localDateTimes = parser.parse(responseItems)
        // then
        assertEquals(expectedOutput, localDateTimes)
    }

    @Test
    @DisplayName("분 단위 시간 패턴을 파싱한다.")
    fun parseTimeWithMinutesTest() {
        // given
        val availableTimeMapWithMinutes = ApplicantAvailableTimeMap(
            time = listOf("(\\d+):(\\d+)\\s*~\\s*(\\d+):(\\d+)"),
            days = listOf("yyyy M월 d일 E요일 HH:mm")
        )
        val parserWithMinutes = AvailableTimeParser(availableTimeMapWithMinutes)

        val item1 = ResponseItem(":9월 24일 수요일", answer = "12:30~14:30")
        val responseItems = listOf(item1)
        val expectedOutput = listOf(
            LocalDateTime.of(2025, 9, 24, 12, 30, 0).atZone(ZoneId.of("Asia/Seoul")).toInstant(),
            LocalDateTime.of(2025, 9, 24, 13, 0, 0).atZone(ZoneId.of("Asia/Seoul")).toInstant(),
            LocalDateTime.of(2025, 9, 24, 13, 30, 0).atZone(ZoneId.of("Asia/Seoul")).toInstant(),
            LocalDateTime.of(2025, 9, 24, 14, 0, 0).atZone(ZoneId.of("Asia/Seoul")).toInstant(),
        )
        // when
        val localDateTimes = parserWithMinutes.parse(responseItems)
        // then
        assertEquals(expectedOutput, localDateTimes)
    }
}