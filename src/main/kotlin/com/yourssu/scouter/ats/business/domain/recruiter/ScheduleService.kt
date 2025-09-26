package com.yourssu.scouter.ats.business.domain.recruiter

import com.yourssu.scouter.ats.implement.domain.applicant.ApplicantReader
import com.yourssu.scouter.ats.implement.domain.recruiter.Schedule
import com.yourssu.scouter.ats.implement.domain.recruiter.ScheduleReader
import com.yourssu.scouter.ats.implement.domain.recruiter.ScheduleWriter
import com.yourssu.scouter.ats.implement.support.exception.ApplicantNotFoundException
import com.yourssu.scouter.common.implement.domain.part.PartReader
import com.yourssu.scouter.common.implement.support.exception.PartNotFoundException
import jakarta.transaction.Transactional
import org.springframework.stereotype.Service

@Service
class ScheduleService(
    private val scheduleWriter: ScheduleWriter,
    private val scheduleReader: ScheduleReader,
    private val partReader: PartReader,
    private val applicantReader: ApplicantReader,
    private val scheduleValidator: ScheduleValidator
) {

    @Transactional
    fun createSchedules(scheduleCommands: List<CreateScheduleCommand>) {
        val schedules = commandsToInterviewSchedules(scheduleCommands)
        scheduleValidator.validateNoDuplicates(schedules)
        scheduleWriter.writeAll(schedules)
    }

    fun readSchedulesByPartId(partId: Long): List<ScheduleDto> {
        val schedules = scheduleReader.readAllByPartId(partId)
        return ScheduleDto.fromDomainList(schedules)
    }

    private fun commandsToInterviewSchedules(commands: List<CreateScheduleCommand>): List<Schedule> {
        val partIds = commands.map { it.partId }.distinct()
        val applicantIds = commands.map { it.applicantId }.distinct()

        val partsMap = partReader.readAllByIds(partIds).associateBy { it.id }
        val applicantsMap = applicantReader.readByIds(applicantIds).associateBy { it.id }

        return commands.map { command ->
            Schedule.create(
                applicant = applicantsMap[command.applicantId]
                    ?: throw ApplicantNotFoundException("지원자 정보를 찾을 수 없습니다: ${command.applicantId}"),
                interviewTime = command.interviewTime,
                part = partsMap[command.partId]
                    ?: throw PartNotFoundException("파트를 찾을 수 없습니다: ${command.partId}"),
            )
        }
    }
}