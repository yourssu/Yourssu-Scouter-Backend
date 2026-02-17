package com.yourssu.scouter.ats.business.support.utils

import com.yourssu.scouter.ats.implement.domain.recruiter.ScheduleLocationType

object ScheduleLocationTypeConverter {
    private val locationTypeToString =
        mapOf(
            ScheduleLocationType.CLUB_ROOM to "동방",
            ScheduleLocationType.CLASS_ROOM to "강의실",
            ScheduleLocationType.ONLINE to "비대면",
            ScheduleLocationType.ETC to "기타",
        )

    private val stringToLocationType =
        locationTypeToString.entries.associate {
            it.value.replace(" ", "") to it.key
        }

    fun convertToString(locationType: ScheduleLocationType): String =
        locationTypeToString[locationType]
            ?: throw IllegalArgumentException("Unknown location type: $locationType")

    fun convertToEnum(locationType: String): ScheduleLocationType {
        val blankRemovedLocationType = locationType.replace(" ", "")

        return stringToLocationType[blankRemovedLocationType]
            ?: throw IllegalArgumentException("Unknown schedule location type: $locationType")
    }
}
