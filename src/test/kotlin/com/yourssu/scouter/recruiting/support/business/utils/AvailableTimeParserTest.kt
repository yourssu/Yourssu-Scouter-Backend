package com.yourssu.scouter.recruiting.support.business.utils

import com.yourssu.scouter.common.google.ResponseItem
import com.yourssu.scouter.common.initializer.ApplicantAvailableTimeMap
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import kotlin.test.assertEquals

class AvailableTimeParserTest {
    val availableTimeMap: ApplicantAvailableTimeMap =
        ApplicantAvailableTimeMap(
            time = listOf("(\\d+)시\\s*~\\s*(\\d+)시"),
            days = listOf("yyyy M월 d일 E요일 HH:mm", "yyyy MM.dd HH:mm", "yyyy M월 d일 HH:mm"),
        )

    val parser = AvailableTimeParser(availableTimeMap)

    val fixedNow: Instant =
        LocalDateTime.of(2026, 3, 10, 0, 0)
            .atZone(ZoneId.of("Asia/Seoul"))
            .toInstant()
    val parserWithFixedNow = AvailableTimeParser(availableTimeMap) { fixedNow }

    val currentYear = LocalDateTime.now().year

    @Test
    @DisplayName("지원자의 면접 가능시간을 나타내는 ResponseItem 배열을 Instant 배열로 변환한다.")
    fun parseTimeOnlyHourSuccessTest() {
        // given
        val item1 = ResponseItem("09.24", "12시~15시")
        val responseItems: List<ResponseItem> = listOf(item1)

        val expectedOutput =
            listOf(
                LocalDateTime.of(currentYear, 9, 24, 12, 0).atZone(ZoneId.of("Asia/Seoul")).toInstant(),
                LocalDateTime.of(currentYear, 9, 24, 13, 0, 0).atZone(ZoneId.of("Asia/Seoul")).toInstant(),
                LocalDateTime.of(currentYear, 9, 24, 14, 0).atZone(ZoneId.of("Asia/Seoul")).toInstant(),
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
        val item1 = ResponseItem("9/24", "12시~15시")
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
        val item1 = ResponseItem("9.24", "12:00~15:00")
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
        val item1 = ResponseItem("9.24", answer = "14시~12시")
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
        val item1 = ResponseItem("9.24", answer = "불가")
        val responseItems = listOf(item1)
        // when
        val localDateTimes = parser.parse(responseItems)
        // then
        assertEquals(emptyList(), localDateTimes)
    }

    @Test
    @DisplayName("응답이 '상관없음'인 경우 09:00부터 23:00까지 모든 시간을 반환한다.")
    fun parseAllAvailableAnswerTest() {
        // given
        val item1 = ResponseItem("09.24", answer = "상관없음")
        val responseItems = listOf(item1)
        // when
        val localDateTimes = parser.parse(responseItems)
        // then
        assertEquals(15, localDateTimes.size)
        assertEquals(
            LocalDateTime.of(currentYear, 9, 24, 9, 0).atZone(ZoneId.of("Asia/Seoul")).toInstant(),
            localDateTimes.first(),
        )
        assertEquals(
            LocalDateTime.of(currentYear, 9, 24, 23, 0).atZone(ZoneId.of("Asia/Seoul")).toInstant(),
            localDateTimes.last(),
        )
    }

    @Test
    @DisplayName("쉼표로 구분된 여러 시간을 파싱한다.")
    fun parseMultipleTimesTest() {
        // given
        val item1 = ResponseItem("09.24", answer = "12시~14시, 16시~18시")
        val responseItems = listOf(item1)
        val expectedOutput =
            listOf(
                LocalDateTime.of(currentYear, 9, 24, 12, 0).atZone(ZoneId.of("Asia/Seoul")).toInstant(),
                LocalDateTime.of(currentYear, 9, 24, 13, 0).atZone(ZoneId.of("Asia/Seoul")).toInstant(),
                LocalDateTime.of(currentYear, 9, 24, 16, 0).atZone(ZoneId.of("Asia/Seoul")).toInstant(),
                LocalDateTime.of(currentYear, 9, 24, 17, 0).atZone(ZoneId.of("Asia/Seoul")).toInstant(),
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
        val item1 = ResponseItem("09.24", answer = "12시~14시")
        val item2 = ResponseItem("09.25", answer = "16시~18시")
        val responseItems = listOf(item1, item2)
        val expectedOutput =
            listOf(
                LocalDateTime.of(currentYear, 9, 24, 12, 0, 0).atZone(ZoneId.of("Asia/Seoul")).toInstant(),
                LocalDateTime.of(currentYear, 9, 24, 13, 0, 0).atZone(ZoneId.of("Asia/Seoul")).toInstant(),
                LocalDateTime.of(currentYear, 9, 25, 16, 0, 0).atZone(ZoneId.of("Asia/Seoul")).toInstant(),
                LocalDateTime.of(currentYear, 9, 25, 17, 0, 0).atZone(ZoneId.of("Asia/Seoul")).toInstant(),
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
        val item1 = ResponseItem("09.24", answer = "12시~15시")
        val item2 = ResponseItem("9월 25일", answer = "12시~13시")
        val responseItems = listOf(item1, item2)
        val expectedOutput =
            listOf(
                LocalDateTime.of(currentYear, 9, 24, 12, 0, 0).atZone(ZoneId.of("Asia/Seoul")).toInstant(),
                LocalDateTime.of(currentYear, 9, 24, 13, 0, 0).atZone(ZoneId.of("Asia/Seoul")).toInstant(),
                LocalDateTime.of(currentYear, 9, 24, 14, 0, 0).atZone(ZoneId.of("Asia/Seoul")).toInstant(),
                LocalDateTime.of(currentYear, 9, 25, 12, 0, 0).atZone(ZoneId.of("Asia/Seoul")).toInstant(),
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
        val availableTimeMapWithMinutes =
            ApplicantAvailableTimeMap(
                time = listOf("(\\d+):(\\d+)\\s*~\\s*(\\d+):(\\d+)"),
                days = listOf("yyyy M월 d일 HH:mm"),
            )
        val parserWithMinutes = AvailableTimeParser(availableTimeMapWithMinutes)

        val item1 = ResponseItem("9월 24일", answer = "12:30~14:30")
        val responseItems = listOf(item1)
        val expectedOutput =
            listOf(
                LocalDateTime.of(currentYear, 9, 24, 12, 30, 0).atZone(ZoneId.of("Asia/Seoul")).toInstant(),
                LocalDateTime.of(currentYear, 9, 24, 13, 0, 0).atZone(ZoneId.of("Asia/Seoul")).toInstant(),
                LocalDateTime.of(currentYear, 9, 24, 13, 30, 0).atZone(ZoneId.of("Asia/Seoul")).toInstant(),
                LocalDateTime.of(currentYear, 9, 24, 14, 0, 0).atZone(ZoneId.of("Asia/Seoul")).toInstant(),
            )
        // when
        val localDateTimes = parserWithMinutes.parse(responseItems)
        // then
        assertEquals(expectedOutput, localDateTimes)
    }

    @Test
    @DisplayName("질문이 날짜별로 여러 개일 때 availableTimeQuestion과 무관한 항목은 제외하고 파싱한다.")
    fun parseMultipleDateQuestionsTest() {
        // given
        val item1 = ResponseItem("가능하신 시간대를 모두 선택해주세요.\n9월 24일", answer = "12시, 13시")
        val item2 = ResponseItem("가능하신 시간대를 모두 선택해주세요.\n9월 25일", answer = "14시")
        val item3 = ResponseItem("학과", answer = "컴퓨터공학부")
        val responseItems = listOf(item1, item2, item3)
        val expectedOutput =
            listOf(
                LocalDateTime.of(currentYear, 9, 24, 12, 0, 0).atZone(ZoneId.of("Asia/Seoul")).toInstant(),
                LocalDateTime.of(currentYear, 9, 24, 13, 0, 0).atZone(ZoneId.of("Asia/Seoul")).toInstant(),
                LocalDateTime.of(currentYear, 9, 25, 14, 0, 0).atZone(ZoneId.of("Asia/Seoul")).toInstant(),
            )

        // when
        val localDateTimes = parser.parse(responseItems, availableTimeQuestion = "가능하신 시간대를 모두 선택해주세요.")

        // then
        assertEquals(expectedOutput, localDateTimes)
    }

    @Test
    @DisplayName("질문 하나에 날짜와 시간이 모두 담긴 답변 형식일 때도 파싱한다.")
    fun parseSingleQuestionDateTimeOptionsTest() {
        // given
        val item1 = ResponseItem("가능하신 시간대를 모두 선택해주세요.", answer = "9월 24일 12시, 9월 25일 13시")
        val responseItems = listOf(item1)
        val expectedOutput =
            listOf(
                LocalDateTime.of(currentYear, 9, 24, 12, 0, 0).atZone(ZoneId.of("Asia/Seoul")).toInstant(),
                LocalDateTime.of(currentYear, 9, 25, 13, 0, 0).atZone(ZoneId.of("Asia/Seoul")).toInstant(),
            )

        // when
        val localDateTimes = parser.parse(responseItems, availableTimeQuestion = "가능하신 시간대를 모두 선택해주세요.")

        // then
        assertEquals(expectedOutput, localDateTimes)
    }

    @Test
    @DisplayName("날짜별 단일 체크박스 문항이 여러 개일 때 공통 접두어로 모두 매칭해 파싱한다.")
    fun parseSingleQuestionPerDateTest() {
        // given: 날짜마다 독립된 체크박스 문항 - 제목은 "공통 접두어\n날짜" 형태
        val item1 = ResponseItem("가능하신 시간대를 모두 선택해주세요.\n9월 24일", answer = "12시~13시")
        val item2 = ResponseItem("가능하신 시간대를 모두 선택해주세요.\n9월 25일", answer = "13시~14시")
        val responseItems = listOf(item1, item2)
        val expectedOutput =
            listOf(
                LocalDateTime.of(currentYear, 9, 24, 12, 0, 0).atZone(ZoneId.of("Asia/Seoul")).toInstant(),
                LocalDateTime.of(currentYear, 9, 25, 13, 0, 0).atZone(ZoneId.of("Asia/Seoul")).toInstant(),
            )

        // when
        val localDateTimes = parser.parse(responseItems, availableTimeQuestion = "가능하신 시간대를 모두 선택해주세요.")

        // then
        assertEquals(expectedOutput, localDateTimes)
    }

    @Test
    @DisplayName("HH:mm~HH:mm 형식의 체크박스 선택지를 파싱한다.")
    fun parseHourMinuteRangeCheckboxOptionsTest() {
        // given
        val timeRangeMap =
            ApplicantAvailableTimeMap(
                time = listOf("(\\d+):(\\d+)\\s*~\\s*(\\d+):(\\d+)"),
                days = listOf("yyyy M월 d일 E요일 HH:mm"),
            )
        val timeRangeParser = AvailableTimeParser(timeRangeMap) { fixedNow }

        // given: 2026년 9월 16일은 수요일
        val item1 = ResponseItem("가능하신 시간대를 모두 선택해주세요.\n9월 16일 수요일", answer = "17:00~18:00, 18:00~19:00")
        val responseItems = listOf(item1)
        val expectedOutput =
            listOf(
                LocalDateTime.of(2026, 9, 16, 17, 0, 0).atZone(ZoneId.of("Asia/Seoul")).toInstant(),
                LocalDateTime.of(2026, 9, 16, 17, 30, 0).atZone(ZoneId.of("Asia/Seoul")).toInstant(),
                LocalDateTime.of(2026, 9, 16, 18, 0, 0).atZone(ZoneId.of("Asia/Seoul")).toInstant(),
                LocalDateTime.of(2026, 9, 16, 18, 30, 0).atZone(ZoneId.of("Asia/Seoul")).toInstant(),
            )

        // when
        val localDateTimes =
            timeRangeParser.parse(responseItems, availableTimeQuestion = "가능하신 시간대를 모두 선택해주세요.")

        // then
        assertEquals(expectedOutput, localDateTimes)
    }

    @Test
    @DisplayName("요일이 현재 연도와 맞으면 요일을 포함한 형식으로 파싱한다.")
    fun parseDayOfWeekMatchTest() {
        // given: 2026년 9월 11일은 금요일
        val item1 = ResponseItem("가능하신 시간대를 모두 선택해주세요.\n9월 11일 금요일", answer = "14시~16시")
        val responseItems = listOf(item1)
        val expectedOutput =
            listOf(
                LocalDateTime.of(2026, 9, 11, 14, 0, 0).atZone(ZoneId.of("Asia/Seoul")).toInstant(),
                LocalDateTime.of(2026, 9, 11, 15, 0, 0).atZone(ZoneId.of("Asia/Seoul")).toInstant(),
            )

        // when
        val localDateTimes =
            parserWithFixedNow.parse(responseItems, availableTimeQuestion = "가능하신 시간대를 모두 선택해주세요.")

        // then
        assertEquals(expectedOutput, localDateTimes)
    }

    @Test
    @DisplayName("요일이 현재 연도와 맞지 않으면 요일을 무시하고 현재 연도로 파싱한다.")
    fun parseDayOfWeekMismatchIgnoresDayOfWeekTest() {
        // given: 2026년 9월 12일은 토요일이라 '금요일'과 불일치
        val item1 = ResponseItem("가능하신 시간대를 모두 선택해주세요.\n9월 12일 금요일", answer = "14시~16시")
        val responseItems = listOf(item1)
        val expectedOutput =
            listOf(
                LocalDateTime.of(2026, 9, 12, 14, 0, 0).atZone(ZoneId.of("Asia/Seoul")).toInstant(),
                LocalDateTime.of(2026, 9, 12, 15, 0, 0).atZone(ZoneId.of("Asia/Seoul")).toInstant(),
            )

        // when
        val localDateTimes =
            parserWithFixedNow.parse(responseItems, availableTimeQuestion = "가능하신 시간대를 모두 선택해주세요.")

        // then
        assertEquals(expectedOutput, localDateTimes)
    }

    @Test
    @DisplayName("요일 포맷만 설정돼 있어도 요일 불일치 시 요일을 무시하고 현재 연도로 파싱한다.")
    fun parseDayOfWeekMismatchWithOnlyDayOfWeekFormatTest() {
        // given: 운영 설정처럼 요일 포함 포맷만 존재, 2026년 1월 5일은 월요일이라 '화요일'과 불일치
        val dayOfWeekOnlyMap =
            ApplicantAvailableTimeMap(
                time = listOf("(\\d+)시\\s*~\\s*(\\d+)시"),
                days = listOf("yyyy M월 d일 E요일 HH:mm"),
            )
        val dayOfWeekOnlyParser = AvailableTimeParser(dayOfWeekOnlyMap) { fixedNow }

        val item1 = ResponseItem("가능하신 시간대를 모두 선택해주세요.\n1월 5일 화요일", answer = "14시~16시")
        val responseItems = listOf(item1)
        val expectedOutput =
            listOf(
                LocalDateTime.of(2026, 1, 5, 14, 0, 0).atZone(ZoneId.of("Asia/Seoul")).toInstant(),
                LocalDateTime.of(2026, 1, 5, 15, 0, 0).atZone(ZoneId.of("Asia/Seoul")).toInstant(),
            )

        // when
        val localDateTimes =
            dayOfWeekOnlyParser.parse(responseItems, availableTimeQuestion = "가능하신 시간대를 모두 선택해주세요.")

        // then
        assertEquals(expectedOutput, localDateTimes)
    }

    @Test
    @DisplayName("days에 월 정보가 없으면 현재 월을 사용해 파싱한다.")
    fun parseDayWithoutMonthUsesCurrentMonthTest() {
        // given
        val item1 = ResponseItem("16일 월요일", answer = "12시~14시")
        val responseItems = listOf(item1)
        val expectedOutput =
            listOf(
                LocalDateTime.of(2026, 3, 16, 12, 0, 0).atZone(ZoneId.of("Asia/Seoul")).toInstant(),
                LocalDateTime.of(2026, 3, 16, 13, 0, 0).atZone(ZoneId.of("Asia/Seoul")).toInstant(),
            )

        // when
        val localDateTimes = parserWithFixedNow.parse(responseItems)

        // then
        assertEquals(expectedOutput, localDateTimes)
    }
}
