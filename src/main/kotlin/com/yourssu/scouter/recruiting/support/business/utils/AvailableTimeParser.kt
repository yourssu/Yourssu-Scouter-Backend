package com.yourssu.scouter.recruiting.support.business.utils

import com.yourssu.scouter.common.support.implement.google.ResponseItem
import com.yourssu.scouter.common.support.implement.initialization.ApplicantAvailableTimeMap
import org.springframework.stereotype.Component
import java.time.Instant
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.util.Locale

@Component
class AvailableTimeParser(
    private val availableTimeMap: ApplicantAvailableTimeMap,
    private val nowProvider: () -> Instant = { Instant.now() },
) {
    private val dayOfWeekInDaysRegex = Regex("""\s*[월화수목금토일]요일""")
    private val dayOfWeekInFormatRegex = Regex("""\s*E+요일""")

    fun parse(
        responseItems: List<ResponseItem>,
        availableTimeQuestion: String? = null,
    ): List<Instant> =
        selectCandidateItems(responseItems, availableTimeQuestion)
            .flatMap { responseItem ->
                parseByQuestionDayAndAnswerTime(responseItem)
                    .ifEmpty { parseDateTimeFromAnswer(responseItem.answer) }
            }
            .distinct()
            .sorted()

    private fun selectCandidateItems(
        responseItems: List<ResponseItem>,
        availableTimeQuestion: String?,
    ): List<ResponseItem> {
        if (availableTimeQuestion == null) {
            return responseItems
        }

        val directMatchedItems = responseItems.filter { it.question.startsWith(availableTimeQuestion) }
        if (directMatchedItems.isEmpty()) {
            return responseItems
        }

        val heuristicallyMatchedItems =
            responseItems
                .filterNot { directMatchedItems.contains(it) }
                .filter { isLikelyAvailableTimeItem(it) }

        return directMatchedItems + heuristicallyMatchedItems
    }

    private fun isLikelyAvailableTimeItem(responseItem: ResponseItem): Boolean =
        parseByQuestionDayAndAnswerTime(responseItem).isNotEmpty() ||
            parseDateTimeFromAnswer(responseItem.answer).isNotEmpty()

    private fun parseByQuestionDayAndAnswerTime(responseItem: ResponseItem): List<Instant> {
        if (responseItem.answer == "불가") {
            return emptyList()
        }

        val days = responseItem.question.substringAfterLast(":").trim()
        val times: List<String> = getAvailableTimes(responseItem.answer) ?: return emptyList()
        val nowInSeoul = nowInSeoul()
        val year = nowInSeoul.year

        return getAvailableTimeInstants(times, year, nowInSeoul.monthValue, days)
            ?: emptyList()
    }

    private fun getAvailableTimeInstants(
        times: List<String>,
        year: Int,
        currentMonth: Int,
        days: String,
    ): List<Instant>? =
        parseWithConfiguredFormats(times, year, days)
            ?: parseIgnoringDayOfWeek(times, year, days)
            ?: parseWithCurrentMonthFallback(times, year, currentMonth, days)

    // 폼에는 연도 정보가 없어 현재 연도로 가정하는데, 실제 작성 연도가 다르면
    // 날짜에 함께 적힌 요일이 현재 연도의 요일과 어긋나 파싱에 실패한다.
    // 이 경우 날짜와 포맷 양쪽에서 요일을 제거하고 현재 연도 기준으로 파싱한다.
    private fun parseIgnoringDayOfWeek(
        times: List<String>,
        year: Int,
        days: String,
    ): List<Instant>? {
        val daysWithoutDayOfWeek = days.replace(dayOfWeekInDaysRegex, "").trim()
        if (daysWithoutDayOfWeek == days) {
            return null
        }

        val formatsWithoutDayOfWeek =
            availableTimeMap.days
                .map { format -> format.replace(dayOfWeekInFormatRegex, "").trim() }
                .distinct()

        return parseWithFormats(formatsWithoutDayOfWeek, times, year, daysWithoutDayOfWeek)
    }

    private fun parseWithConfiguredFormats(
        times: List<String>,
        year: Int,
        days: String,
    ): List<Instant>? = parseWithFormats(availableTimeMap.days, times, year, days)

    private fun parseWithFormats(
        formats: List<String>,
        times: List<String>,
        year: Int,
        days: String,
    ): List<Instant>? =
        formats.firstNotNullOfOrNull { format ->
            val formatter = DateTimeFormatter.ofPattern(format).withLocale(Locale.KOREA)
            try {
                times.map { time ->
                    LocalDateTime.parse("$year $days $time", formatter)
                        .atZone(ZoneId.of("Asia/Seoul"))
                        .toInstant()
                }
            } catch (_: DateTimeParseException) {
                null
            }
        }

    private fun parseWithCurrentMonthFallback(
        times: List<String>,
        year: Int,
        currentMonth: Int,
        days: String,
    ): List<Instant>? {
        if (containsMonthInfo(days)) {
            return null
        }

        val dayWithCurrentMonth = "${currentMonth}월 $days"
        return parseWithConfiguredFormats(times, year, dayWithCurrentMonth)
            ?: parseIgnoringDayOfWeek(times, year, dayWithCurrentMonth)
    }

    private fun containsMonthInfo(days: String): Boolean =
        Regex("""\d{1,2}\s*월""").containsMatchIn(days) ||
            Regex("""\d{1,2}[./-]\d{1,2}""").containsMatchIn(days)

    private fun parseDateTimeFromAnswer(answer: String): List<Instant> {
        if (answer == "불가" || answer == "상관없음") {
            return emptyList()
        }

        val year = nowInSeoul().year
        return answer.split(",")
            .map { it.trim() }
            .mapNotNull { token -> parseDateTimeToken(year, token) }
    }

    private fun nowInSeoul() = nowProvider().atZone(ZoneId.of("Asia/Seoul"))

    private fun parseDateTimeToken(
        year: Int,
        token: String,
    ): Instant? {
        val dateTimeRegex =
            Regex(
                """(\d{1,2})\s*월\s*(\d{1,2})\s*일(?:\s*[가-힣]+요일)?\s*(\d{1,2})(?::(\d{1,2}))?\s*시?""",
            )
        val result = dateTimeRegex.matchEntire(token)?.groupValues ?: return null

        val month = result[1].toInt()
        val day = result[2].toInt()
        val hour = result[3].toInt()
        val minute = result[4].ifEmpty { "0" }.toInt()

        return LocalDateTime.of(year, month, day, hour, minute)
            .atZone(ZoneId.of("Asia/Seoul"))
            .toInstant()
    }

    private fun getAvailableTimes(answer: String): List<String>? {
        return if (answer == "상관없음") {
            allAvailable()
        } else {
            answer.split(",").flatMap { time ->
                parseTime(time.trim()) ?: return null
            }
        }
    }

    private fun parseTime(time: String): List<String>? =
        parseTimeByConfiguredPattern(time)
            ?: parseSingleTime(time)

    private fun parseTimeByConfiguredPattern(time: String): List<String>? =
        availableTimeMap.time.firstNotNullOfOrNull { pattern ->
            val regex = pattern.toRegex()
            val result = regex.find(time)?.destructured?.toList()

            when (result?.size) {
                2 -> parseTimeOnlyHour(result[0], result[1])
                4 ->
                    parseTimeWithMinutes(
                        result[0],
                        result[1],
                        result[2],
                        result[3],
                    )

                else -> null
            }
        }

    private fun parseSingleTime(time: String): List<String>? {
        val hourMatch = Regex("""^(\d{1,2})\s*시$""").matchEntire(time)
        if (hourMatch != null) {
            val hour = hourMatch.groupValues[1].toInt()
            return listOf("%02d:00".format(hour))
        }

        val minuteMatch = Regex("""^(\d{1,2}):(\d{2})$""").matchEntire(time)
        if (minuteMatch != null) {
            val hour = minuteMatch.groupValues[1].toInt()
            val minute = minuteMatch.groupValues[2].toInt()
            return listOf("%02d:%02d".format(hour, minute))
        }

        return null
    }

    private fun allAvailable(): List<String> = parseTimeOnlyHour("09", "24")

    private fun parseTimeOnlyHour(
        before: String,
        after: String,
    ): List<String> {
        val result = mutableListOf<String>()
        for (hour in before.toInt() until after.toInt()) {
            result.add("%02d:00".format(hour))
        }
        return result
    }

    private fun parseTimeWithMinutes(
        beforeHour: String,
        beforeMinute: String,
        afterHour: String,
        afterMinute: String,
    ): List<String> {
        val result = mutableListOf<String>()
        val startTime = LocalTime.of(beforeHour.toInt(), beforeMinute.toInt())
        val endTime = LocalTime.of(afterHour.toInt(), afterMinute.toInt())

        var currentTime = startTime
        while (currentTime.isBefore(endTime)) {
            result.add("%02d:%02d".format(currentTime.hour, currentTime.minute))
            currentTime = currentTime.plusMinutes(30)
        }

        return result
    }
}
