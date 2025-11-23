package com.yourssu.scouter.ats.business.support.utils

import com.yourssu.scouter.common.implement.support.google.ResponseItem
import com.yourssu.scouter.common.implement.support.initialization.ApplicantAvailableTimeMap
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
    private val availableTimeMap: ApplicantAvailableTimeMap
) {

    fun parse(
        responseItems: List<ResponseItem>
    ): List<Instant> =
        getLocalDateTimeByDay(responseItems)

    private fun getLocalDateTimeByDay(responseItems: List<ResponseItem>): List<Instant> =
        responseItems.flatMap {
            if (it.answer == "불가") return@flatMap emptyList()
            val days = it.question.substringAfterLast(":")
            val times: List<String>? = getAvailableTimes(it.answer)
            val year = LocalDateTime.now().year

            getAvailableTimeInstants(times, year, days)
                ?: emptyList()
        }

    private fun getAvailableTimeInstants(
        times: List<String>?,
        year: Int,
        days: String
    ): List<Instant>? = availableTimeMap.days.firstNotNullOfOrNull { format ->
        val formatter = DateTimeFormatter.ofPattern(format).withLocale(Locale.KOREA)
        try {
            times?.map { time ->
                LocalDateTime.parse("$year $days $time", formatter)
                    .atZone(ZoneId.of("Asia/Seoul"))
                    .toInstant()
            }
        } catch (_: DateTimeParseException) {
            null
        }
    }


    private fun getAvailableTimes(answer: String): List<String>? {
        val times: List<String> =
            if (answer == "상관없음") allAvailable()
            else {
                answer.split(",").flatMap { time ->
                    parseTime(time) ?: return null
                }
            }
        return times
    }

    private fun parseTime(time: String): List<String>? =
        availableTimeMap.time.firstNotNullOfOrNull { pattern ->
            val regex = pattern.toRegex()
            val result = regex.find(time)?.destructured?.toList()

            when (result?.size) {
                2 -> parseTimeOnlyHour(result[0], result[1])
                4 -> parseTimeWithMinutes(
                    result[0],
                    result[1],
                    result[2],
                    result[3]
                )

                else -> null
            }
        }

    private fun allAvailable(): List<String> =
        parseTimeOnlyHour("00", "24")

    private fun parseTimeOnlyHour(before: String, after: String): List<String> {
        val result = mutableListOf<String>()
        for (hour in before.toInt() until after.toInt())
            result.add("%02d:00".format(hour))
        return result
    }

    private fun parseTimeWithMinutes(
        beforeHour: String,
        beforeMinute: String,
        afterHour: String,
        afterMinute: String
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